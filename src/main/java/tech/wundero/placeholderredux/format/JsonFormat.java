package tech.wundero.placeholderredux.format;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.*;
import org.spongepowered.api.util.Tuple;
import tech.wundero.placeholderredux.PlaceholderRedux;
import tech.wundero.placeholderredux.api.Placeholder;
import tech.wundero.placeholderredux.api.PlaceholderArgs;
import tech.wundero.placeholderredux.api.PlaceholderTemplate;
import tech.wundero.placeholderredux.api.exceptions.PlaceholderException;
import tech.wundero.placeholderredux.api.format.PlaceholderFormat;
import tech.wundero.placeholderredux.api.function.FunctionArg;
import tech.wundero.placeholderredux.api.service.PlaceholderRegisterService;
import tech.wundero.placeholderredux.service.HashArgs;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * JsonFormat for placeholders. like: {id:'player','type':'time_played','format':'HH:mm:ss'}
 *
 * This format is preferred since it is order agnostic and supports named (and thus easily parsed) args.
 */
public class JsonFormat implements PlaceholderFormat {

    private Gson g = new GsonBuilder() // custom type adapters for colors, since why not.
            .registerTypeAdapter(TextColor.class, tca)
            .registerTypeAdapter(TextStyle.class, tsa)
            .registerTypeAdapter(TextFormat.class, tfa)
            .create();

    // thread safe atomic int
    private static final AtomicInteger aid = new AtomicInteger(0);

    private final Map<Class<?>, TypeAdapter<?>> adapters = new HashMap<Class<?>, TypeAdapter<?>>() {{
        put(TextColor.class, tca);
        put(TextStyle.class, tsa);
        put(TextFormat.class, tfa);
    }};

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public <T> boolean addTypeParser(Class<? extends T> type, TypeAdapter<? extends T> parser) {
        lock.writeLock().lock();
        adapters.put(type, parser);
        GsonBuilder builder = new GsonBuilder();
        adapters.forEach(builder::registerTypeAdapter);
        g = builder.create(); // we rebuild gson for every new adapter used.
        // locked cause we need synchrony. I want to support async (faster) calls as much as possible.
        lock.writeLock().unlock();
        return true;
    }

    private static final TextColorAdapter tca = new TextColorAdapter();
    private static final TextStyleAdapter tsa = new TextStyleAdapter();
    private static final TextFormatAdapter tfa = new TextFormatAdapter();

    // simple parsers for color style and format.

    private static class TextColorAdapter extends TypeAdapter<TextColor> {

        @Override
        public void write(JsonWriter out, TextColor value) throws IOException {
            out.value(value.getName());
        }

        @Override
        public TextColor read(JsonReader in) throws IOException {
            return colorString(in.nextString());
        }
    }

    private static class TextFormatAdapter extends TypeAdapter<TextFormat> {

        @Override
        public void write(JsonWriter out, TextFormat value) throws IOException {
            out.beginObject();
            if (!value.getColor().equals(TextColors.NONE)) {
                out.name("color");
                tca.write(out, value.getColor());
            }
            if (!value.getStyle().equals(TextStyles.NONE)) {
                out.name("style");
                tsa.write(out, value.getStyle());
            }
            out.endObject();
        }

        @Override
        public TextFormat read(JsonReader in) throws IOException {
            TextColor cl = TextColors.NONE;
            TextStyle st = TextStyles.NONE;
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals("color")) {
                    cl = tca.read(in);
                }
                if (name.equals("style")) {
                    st = tsa.read(in);
                }
            }
            return TextFormat.of(cl, st);
        }
    }

    private static class TextStyleAdapter extends TypeAdapter<TextStyle> {

        @Override
        public void write(JsonWriter out, TextStyle value) throws IOException {
            out.beginObject();
            if (value.isBold().isPresent()) {
                out.name("bold");
                out.value(value.isBold().get());
            }
            if (value.isItalic().isPresent()) {
                out.name("italic");
                out.value(value.isItalic().get());
            }
            if (value.hasStrikethrough().isPresent()) {
                out.name("strikethrough");
                out.value(value.hasStrikethrough().get());
            }
            if (value.isObfuscated().isPresent()) {
                out.name("obfuscated");
                out.value(value.isObfuscated().get());
            }
            if (value.hasUnderline().isPresent()) {
                out.name("underline");
                out.value(value.hasUnderline().get());
            }
            out.endObject();
        }

        @Override
        public TextStyle read(JsonReader in) throws IOException {
            in.beginObject();
            TextStyle st = null;
            while (in.hasNext()) { // find all styles and combine them
                TextStyle nw = styleString(in.nextName());
                if (!in.nextBoolean()) {
                    nw = nw.negate();
                }
                if (st == null) {
                    st = nw;
                } else {
                    st = TextStyles.of(st, nw);
                }
            }
            in.endObject();
            return st;
        }
    }

    // Monstrosity part 2, but this one is worse. Fear the recursion!
    private Templated parseFromFormat(String string, Text text) {
        int br = 0;
        int start = 0; // basically pattern matching but without regex cuz regex is slow
        List<String> remainingSparse = new ArrayList<>();
        List<String> orderedArgs = new ArrayList<>();
        // same args map as in classic, placeholder id -> it's args
        Map<String, PlaceholderArgs> args = new HashMap<>();
        int last = 0;
        char[] starr = string.toCharArray();
        boolean quoted = false; // pass over quotes and single quotes. '"' ignores the ", and vice versa.
        boolean singlequoted = false;
        for (int i = 0; i < starr.length; i++) {
            char c = starr[i];
            if (c == '\\') {
                i++;
                continue;
            }
            if (c == '\"' && !singlequoted) {
                quoted = !quoted;
                continue;
            }
            if (c == '\'' && !quoted) {
                singlequoted = !singlequoted;
                continue;
            }
            if (quoted || singlequoted) {
                continue; // quoted text ignored, this suck
            }
            // We don't parse JSON Arrays in this house -> placeholders are base json objects only.
            if (c == '{') { // starting new json bracket
                if (br == 0) {
                    start = i;
                }
                br++;
            }
            if (c == '}') {
                br--;
                if (br == 0) { // ayy we found a complete placeholder json string.
                    String json = string.substring(start, i + 1); // get that string
                    Tuple<String, PlaceholderArgs> parsed = parseJSON(json, args); // parse that string
                    if (parsed == null) { // ops failed give up
                        continue;
                    } // get in-between text for last placeholder and this one, don't omit anything
                    remainingSparse.add(string.substring(last, start));
                    last = i + 1; // shift frame of lsat
                    remainingSparse.add(null); // add null to take 'slot' of placeholder, for iteration's sake.
                    args.put(parsed.getFirst(), parsed.getSecond()); // put args yay
                    orderedArgs.add(parsed.getFirst()); // put ordered args? i suppose this is nice
                }
            }
        }
        remainingSparse.add(string.substring(last)); // add last part of string (could be empty)
        TextTemplate t = TextTemplate.of(); // build template
        for (String s : remainingSparse) {
            if (s == null) { // if placeholder
                String arg = orderedArgs.remove(0); // remove next arg -> that's why this is nice (no hashmap)
                t = t.concat(TextTemplate.of(buildArg(arg, args.get(arg)))); // -> new arg in template
            } else {
                t = t.concat(TextTemplate.of(s)); // just string thanks :)
            }
        }
        return new Templated(t, args, text); // all done
    }

    private Templated parseFormatted(String string, TextFormat fmt) {
        return parseFromFormat(string, Text.of(fmt));
    }

    @Override
    public PlaceholderTemplate parse(String string) {
        return parseFormatted(string, TextFormat.NONE);
    }

    private TextTemplate.Arg buildArg(String arg, PlaceholderArgs args) {
        TextTemplate.Arg.Builder b = TextTemplate.arg(arg);
        // allow 'color':'...' etc. json to customize formatting. Overridden by arg return i think maybe
        args.getArg("color").ifPresent(c -> {
            if (c instanceof TextColor) {
                b.color((TextColor) c);
            }
        });
        args.getArg("style").ifPresent(c -> {
            if (c instanceof TextStyle) {
                b.style((TextStyle) c);
            }
        });
        args.getArg("format").ifPresent(c -> {
            if (c instanceof TextFormat) {
                b.format((TextFormat) c);
            }
        });
        return b.build();
    }

    private static TextStyle styleString(String color) {
        // return stylized strings. ill keep the '&X' codes here cause why not
        switch (color.toLowerCase().trim()) {
            case "bold":
            case "&l":
                return TextStyles.BOLD;
            case "italic":
            case "&o":
                return TextStyles.ITALIC;
            case "random":
            case "obfuscated":
            case "magic":
            case "&k":
                return TextStyles.OBFUSCATED;
            case "strike":
            case "strikethru":
            case "strikethrough":
            case "&m":
                return TextStyles.STRIKETHROUGH;
            case "reset":
            case "&r":
                return TextStyles.RESET;
            case "underline":
            case "&n":
                return TextStyles.UNDERLINE;
            default:
                return TextStyles.NONE;
        }
    }

    private static TextColor colorString(String color) {
        // ill prolly keep the '&X' codes here too.
        // TODO hex support '#ffffff' etc. -> only once Spang does it i guess
        if (color.toLowerCase().trim().startsWith("#")) {
            // pass this for now.
        }
        switch (color.toLowerCase().trim()) {
            case "blue":
            case "&9":
                return TextColors.BLUE;
            case "dark_blue":
            case "dark blue":
            case "&1":
                return TextColors.DARK_BLUE;
            case "dark red":
            case "dark_red":
            case "&4":
                return TextColors.DARK_RED;
            case "red":
            case "&c":
                return TextColors.RED;
            case "reset":
            case "&r":
                return TextColors.RESET;
            case "gold":
            case "&6":
                return TextColors.GOLD;
            case "yellow":
            case "&e":
                return TextColors.YELLOW;
            case "dark_green":
            case "dark green":
            case "&2":
                return TextColors.DARK_GREEN;
            case "green":
            case "lime":
            case "&a":
                return TextColors.GREEN;
            case "aqua":
            case "&b":
                return TextColors.AQUA;
            case "dark_aqua":
            case "dark aqua":
            case "&3":
                return TextColors.DARK_AQUA;
            case "light_purple":
            case "light purple":
            case "pink":
            case "%d":
                return TextColors.LIGHT_PURPLE;
            case "dark_purple":
            case "dark purple":
            case "purple":
            case "magenta":
            case "&5":
                return TextColors.DARK_PURPLE;
            case "white":
            case "&f":
                return TextColors.WHITE;
            case "gray":
            case "grey":
            case "&7":
                return TextColors.GRAY;
            case "dark_grey":
            case "dark_gray":
            case "dark gray":
            case "dark grey":
            case "&8":
                return TextColors.DARK_GRAY;
            case "black":
            case "&0":
                return TextColors.BLACK;
            default:
                return TextColors.NONE;
        }
    }

    private String sanitize(String json, Map<String, PlaceholderArgs> existing, HashArgs args) {
        if (!json.contains("$")) { // nesting recursive placeholders be like
            return json;
        }
        int br = 0;
        char[] starr = json.toCharArray(); // my naming sense is impeccable
        boolean quoted = false;
        boolean singlequoted = false;
        boolean dollar = false;
        int start = -1;
        for (int i = 0; i < starr.length; i++) {
            char c = starr[i];
            if (c == '\\') {
                i++;
                continue;
            }
            if (c == '\"' && !singlequoted) {
                quoted = !quoted;
                continue;
            }
            if (c == '\'' && !quoted) {
                singlequoted = !singlequoted;
                continue;
            }
            if (quoted || singlequoted) {
                continue;
            }
            if (c == '$' && !dollar) {
                dollar = true;
                start = i;
                continue;
            }
            if (!dollar) {
                continue;
            }
            if (c == '{') {
                br++;
            }
            if (c == '}') {
                br--;
                if (br == 0) {
                    String subjson = json.substring(start + 1, i + 1); // skip dollar sign
                    Tuple<String, PlaceholderArgs> parsed = parseJSON(subjson, existing);
                    if (parsed != null) {
                        String parg = "$PLACEHOLDER_" + parsed.getFirst();
                        while (args.getArgs().containsKey(parg)) {
                            parg = "$PLACEHOLDER_" + parsed.getFirst() + "_" + aid.getAndIncrement();
                        }
                        args.putArg(parg, parsed);
                        json = json.substring(0, start) + "'" + parg + "'" + json.substring(i + 1);
                        i = start + parg.length() + 2;
                        starr = json.toCharArray();
                    }
                    dollar = false;
                    start = -1;
                }
            }
            if (br == 0) {
                // not a bracket immediately after dollar sign, reset and ignore
                dollar = false;
                start = -1;
            }
        }
        return json;
    }

    private Tuple<String, PlaceholderArgs> parseJSON(String json, Map<String, PlaceholderArgs> existing) {
        // convert json into usable info

        HashArgs args = new HashArgs();
        // recursively sanitize
        json = sanitize(json, existing, args);

        JsonParser parser = new JsonParser();
        JsonElement el = parser.parse(json); // use actual json parsers cause only schmucks write their own
        // except google isnt a schmuck. its google.
        if (!el.isJsonObject()) {
            return null;
        }
        JsonObject obj = el.getAsJsonObject();
        if (!obj.has("id")) { // invalid json, we don't care just leave it in the text.
            return null;
        }
        String id = obj.get("id").getAsString();
        Tuple<String, Placeholder<?>> plx = tryGetPlaceholder(id, existing);
        if (plx == null) {
            return null; // didnt find a valid placeholder -> leave it
        }
        // get placeholder to parse for
        Placeholder<?> pl = plx.getSecond();
        id = plx.getFirst();
        for (Map.Entry<String, JsonElement> e : obj.entrySet()) { // parse json to put into args
            parseJsonValue(e.getKey(), e.getValue(), pl, args, existing);
        }
        return Tuple.of(id, args);
    }

    private Tuple<String, Placeholder<?>> tryGetPlaceholder(String id, Map<String, PlaceholderArgs> existing) {
        Placeholder<?> pl;
        try { // try to get placeholder, rename id if necessary
            PlaceholderRegisterService rg = PlaceholderRedux.getRegistrar();
            pl = rg.getPlaceholder(id).orElse(null);
            if (pl == null) {
                return null;
            }
            if (existing.containsKey(id)) {
                String idn = id + "_" + aid.getAndIncrement();
                while (existing.containsKey(idn) || rg.hasPlaceholder(idn)) {
                    idn = id + "_" + aid.getAndIncrement();
                }
                id = idn;
            }
        } catch (PlaceholderException e) {
            return null;
        }
        return Tuple.of(id, pl);
    }

    private void parseJsonValue(String key, JsonElement e, Placeholder<?> pl, HashArgs args, Map<String, PlaceholderArgs> existing) {
        // where real gamer hours occurs
        Objects.requireNonNull(pl.data().expectedArgumentTypes());
        FunctionArg<?> fa = pl.data().expectedArgumentTypes().get(key);
        Class<?> t = fa.type();
        if (key.equals("color") && t == null) {
            t = TextColor.class;
        }
        if (key.equals("id") && t == null) { // extensions not necessarily present in the placeholder's args types
            t = String.class;
        }
        if (key.equals("style") && t == null) {
            t = TextStyle.class;
        }
        if (key.equals("format") && t == null) {
            t = TextFormat.class;
        }
        if (t == null) {
            args.putArg(key, null);
            return;
        }
        String vstr = e.toString();
        if (vstr.startsWith("\"") && vstr.endsWith("\"")) {
            // TBH im not even sure why i did this
            vstr = vstr.substring(1, vstr.length() - 1); // unquote to find nesteds
            if (vstr.startsWith("$PLACEHOLDER_")) {
                Object nv = args.getArg(vstr).orElse(null); // yank out them args
                if (nv instanceof Tuple) { // is actually a placeholder -> we don't parse into tuples i hope
                    try {
                        // we castin here
                        Tuple<String, PlaceholderArgs> nva = (Tuple<String, PlaceholderArgs>) nv;
                        Tuple<String, Placeholder<?>> pln = tryGetPlaceholder(nva.getFirst(), existing);

                        if (pln != null) {
                            Class<?> clz = pln.getSecond().getClass();
                            Method m = clz.getDeclaredMethod("parse", Object.class, Object.class, PlaceholderArgs.class);
                            m.setAccessible(true);
                            Class<?> h = m.getReturnType();
                            if (t.isAssignableFrom(h) || t.equals(String.class)) {
                                args.putArg(key, nv);
                            }
                        }
                        args.removeArg(vstr); // remove old value
                        return;
                    } catch(ClassCastException | NoSuchMethodException ignored) {
                        // failed to cast -> somehow someone parsed into a tuple, what a god
                    }
                }
            }
        }
        lock.readLock().lock();
        Object v = g.fromJson(e, t); // unlock that gson boyos
        if (fa.isValid(v)) {
            args.putArg(key, v);
        } else {
            args.putArg(key, "null"); // TODO exception of some sort?
        }
        lock.readLock().unlock();
    }

    private Templated concat(Templated... templates) { // concat templates together
        TextTemplate tmp = null;
        Text fsrc = null;
        Map<String, PlaceholderArgs> argsMap = new HashMap<>();
        for (Templated t : templates) {
            if (tmp == null) {
                tmp = t.getTemplate();
            } else {
                tmp = tmp.concat(t.getTemplate());
            }
            if (fsrc == null) {
                fsrc = t.getFmtsource();
            }
            argsMap.putAll(t.getArgs());
        }
        return new Templated(tmp, argsMap, fsrc);
    }

    @Override
    public PlaceholderTemplate parse(Text text) {
        return text.getChildren().stream().map(this::parse).map(t -> (Templated) t).reduce(parseFromFormat(text.toPlainSingle(), text), this::concat);
    }

    @Override
    public PlaceholderTemplate parse(TextTemplate template) {
        return template.getElements().stream()
                .map(o -> {
                    if (o instanceof Text) {
                        return parse((Text) o);
                    }
                    if (o instanceof TextTemplate) {
                        return parse((TextTemplate) o);
                    }
                    if (o instanceof String) {
                        return parse((String) o);
                    }
                    return o;
                }).reduce(new Templated(TextTemplate.of(), new HashMap<>(), null), (r, v) -> {
                    if (v instanceof Templated) {
                        return concat(r, (Templated) v);
                    }
                    return new Templated(r.getTemplate().concat(TextTemplate.of(v)), r.getArgs(), r.getFmtsource());
                }, this::concat);

    }

    @Override
    public String getId() {
        return "placeholderredux:json";
    }

    @Override
    public String getName() {
        return "Json Format";
    }
}
