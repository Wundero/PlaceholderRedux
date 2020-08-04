package tech.wundero.placeholderredux.format;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import tech.wundero.placeholderredux.PlaceholderRedux;
import tech.wundero.placeholderredux.api.exceptions.MissingPlaceholderException;
import tech.wundero.placeholderredux.api.Placeholder;
import tech.wundero.placeholderredux.api.PlaceholderArgs;
import tech.wundero.placeholderredux.api.PlaceholderTemplate;
import tech.wundero.placeholderredux.api.format.PlaceholderFormat;
import tech.wundero.placeholderredux.api.function.FunctionArg;
import tech.wundero.placeholderredux.service.HashArgs;
import tech.wundero.placeholderredux.util.FluidMatcher;
import tech.wundero.placeholderredux.util.TypeUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Classic %placeholderid:param1:param2% style. Non-named, sequential parameters.
 *
 * Recursion here would likely fail to parse. Please don't try it.
 *
 * This is honestly really not a good system, and mostly legacy. Kept since it is small.
 * {@link JsonFormat} is preferable in all situations.
 */
public class ClassicFormat implements PlaceholderFormat {

    // TODO config these
    private static final Pattern PATTERN = Pattern.compile("[%]([^ %]+)[%]");
    private static final Pattern SPLIT_PATTERN = Pattern.compile(":");

    private static AtomicInteger aid = new AtomicInteger(0);

    public <T> boolean addTypeParser(Class<? extends T> type, Function<String, ? extends T> parser) {
        return TypeUtils.addParser(type, parser);
    }

    // This is a monstrosity and should be burned in a fire.
    private Templated parseFmt(String string, Text formatSource) {
        FluidMatcher m = new FluidMatcher(PATTERN, string); // custom regex pattern matcher
        // Mostly using this matcher cause streams are nice
        Map<String, PlaceholderArgs> plxmap = new HashMap<>(); // map of placeholder id -> placeholder args
        TextTemplate tmp = m.streamAllParts().sequential().map(t -> {
            // for all parts of the string (t.getFirst() is part, t.getSecond() is if it is a match)
            if (!t.getSecond()) {
                // if not a match, just preserve the string
                return t.getFirst();
            }
            String ps = t.getFirst();
            // remove percentages
            ps = ps.substring(1, ps.length() - 1);
            // id only pattern
            if (!SPLIT_PATTERN.matcher(ps).find()) {
                try {
                    // find placeholder
                    PlaceholderRedux.getRegistrar().getPlaceholder(ps).orElseThrow(MissingPlaceholderException::new);
                } catch (Exception e) {
                    // if not found
                    return t.getFirst(); // ignore :)
                }
                plxmap.put(ps, new HashArgs()); // even if it exists, it matters not
                return TextTemplate.arg(ps).build(); // return from map function
            }
            // split out args
            String[] parts = SPLIT_PATTERN.split(ps);
            String id = parts[0];
            Placeholder<?> p;
            try {
                // find placeholder given id
                p = PlaceholderRedux.getRegistrar().getPlaceholder(id).orElseThrow(MissingPlaceholderException::new);
            } catch (Exception e) {
                return t.getFirst(); // ignore :)
            }
            String id0 = id; // produce unique id (cause args can be different but plxmap needs to work)
            while (plxmap.containsKey(id)) {
                // unique integer should do the trick ;)
                id = id0 + "_" + aid.getAndIncrement();
            }
            // produce argmap
            HashArgs args = new HashArgs();
            // argument sequencer function
            Function<String, List<String>> orderedArgNames = p.data().argumentSequence();
            // data type expectations -> helps us parse
            Map<String, FunctionArg<?>> argTypes = p.data().expectedArgumentTypes();
            Objects.requireNonNull(orderedArgNames);
            Objects.requireNonNull(argTypes);
            // TODO change this
            /*
             * This system needs to be updated to support optional (missing) args
             *
             * => for example, player_time_hms could specify format for a player time placeholder, and
             *                 player_option_prefix could specify the prefix of the player
             *              => the option "hms" would be an arg called "time_played_format", for example, while
             *                            "prefix" would be an arg called "option_name", for example
             *              => this obviously conflicts with ordering.
             * ====
             * SOLUTION (ish)
             * Use sequential args depending on previous elements
             * => for example, if arg[0] is EXACTLY 'time' (value as string, no parsing done), arg[1] would be given arg name "time_played_format" to parse
             * => effectively need to provide a functional sequencer, which would be weird -> could be hash map of sanitized string -> resulting arg name
             * => No issues here for how arg[0] is parsed, though arg[0] cannot be functionally sequenced
             */
            // produce args
            for (int i = 1; i < parts.length; i++) {
                // get ordered arg possibilities
                List<String> argNames = orderedArgNames.apply(parts[i - 1]);
                for (String arg : argNames) {
                    // try to parse the args
                    FunctionArg<?> typ = argTypes.get(arg);
                    Optional<?> v = TypeUtils.tryParse(parts[i], typ);
                    if (v.isPresent()) {
                        // if we successfully parse we don't care anymore about the rest
                        args.putArg(arg, v.get());
                        break;
                    }
                }
            }
            // put id -> args map into placeholder setup, return arg with new id
            plxmap.put(id, args);
            return TextTemplate.arg(id).build();
        }).reduce(TextTemplate.of(), (t, r) -> t.concat(TextTemplate.of(r)), TextTemplate::concat); // compound
        return new Templated(tmp, plxmap, formatSource); // return template + placeholder mapper
    }

    @Override
    public PlaceholderTemplate parse(String string) {
        return parseFmt(string, null);
    }

    private Templated concat(Templated... templates) {
        // concat several templates
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

    private Templated parseText(Text text) { // ignores cross-children placeholders, cuz those suck
        return text.getChildren().stream().map(this::parseText).reduce(parseFmt(text.toPlainSingle(), text), this::concat);
    }

    @Override
    public PlaceholderTemplate parse(Text text) {
        return parseText(text);
    }

    private Templated parseTemplate(TextTemplate temp) {
        return temp.getElements().stream()
                .map(o -> {
                    if (o instanceof Text) {
                        return parseText((Text) o);
                    }
                    if (o instanceof TextTemplate) {
                        return parseTemplate((TextTemplate) o);
                    }
                    if (o instanceof String) {
                        return parseFmt((String) o, null);
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
    public PlaceholderTemplate parse(TextTemplate template) {
        return parseTemplate(template);
    }

    @Override
    @Nonnull
    public String getId() {
        return "placeholderredux:classic";
    }

    @Override
    @Nonnull
    public String getName() {
        return "Classic Format";
    }
}
