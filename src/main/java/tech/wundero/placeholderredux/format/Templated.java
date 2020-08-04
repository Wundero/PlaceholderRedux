package tech.wundero.placeholderredux.format;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.util.Tuple;
import tech.wundero.placeholderredux.PlaceholderRedux;
import tech.wundero.placeholderredux.api.Placeholder;
import tech.wundero.placeholderredux.api.PlaceholderArgs;
import tech.wundero.placeholderredux.api.PlaceholderTemplate;
import tech.wundero.placeholderredux.api.exceptions.MissingPlaceholderException;
import tech.wundero.placeholderredux.api.exceptions.NoValueException;
import tech.wundero.placeholderredux.api.exceptions.PlaceholderException;
import tech.wundero.placeholderredux.service.HashArgs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO comment
public class Templated implements PlaceholderTemplate {

    // this class is a true bruh moment
    // immutable so inherently async

    private static final Pattern DUPE_FINDER_PATTERN = Pattern.compile("(.+)_\\d+");

    private final TextTemplate template;
    private final Text fmtsource;
    private final Map<String, PlaceholderArgs> args;

    public Templated(TextTemplate template, Map<String, PlaceholderArgs> args, Text fmtsource) {
        this.template = template;
        this.args = Collections.unmodifiableMap(args);
        this.fmtsource = fmtsource;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("template", template)
                .add("format source", fmtsource)
                .add("args", args)
                .toString();
    }

    public Text getFmtsource() {
        return fmtsource;
    }

    public TextTemplate getTemplate() {
        return template;
    }

    public Map<String, PlaceholderArgs> getArgs() {
        return args;
    }

    @Override
    @Nonnull
    public Text apply(@Nullable Object source, @Nullable Object target) {
        return partialApply(source, target).apply().build();
    }

    private PlaceholderArgs sanitizeArgs(Object source, Object target, PlaceholderArgs args) {
        HashArgs hac = (HashArgs) args;
        Map<String, Object> am = hac.getArgs();
        List<String> keys = new ArrayList<>(am.keySet());
        for (String key : keys) {
            Object value = am.get(key);
            if (value instanceof Tuple) {
                try {
                    Tuple<String, PlaceholderArgs> nested = (Tuple<String, PlaceholderArgs>) value;
                    tryApply(source, target, nested.getFirst(), nested.getSecond(), key, am);
                } catch (ClassCastException ignored) {
                }
            }
        }
        return hac;
    }

    private Placeholder<?> tryGetPlaceholder(String k, PlaceholderArgs a) throws PlaceholderException {
        Placeholder<?> p = PlaceholderRedux.getRegistrar().getPlaceholder(k).orElse(null);
        if (p == null) {
            Matcher m = DUPE_FINDER_PATTERN.matcher(k);
            if (m.matches()) {
                String nid = m.group(1);
                p = PlaceholderRedux.getRegistrar().getPlaceholder(nid).orElse(null);
                if (p == null) {
                    if (a.getArg("id").isPresent()) {
                        p = PlaceholderRedux.getRegistrar().getPlaceholder(a.getArg("id").get().toString()).orElse(null);
                    }
                }
            } else {
                if (a.getArg("id").isPresent()) {
                    p = PlaceholderRedux.getRegistrar().getPlaceholder(a.getArg("id").get().toString()).orElse(null);
                }
            }
        }
        if (p == null) {
            throw new MissingPlaceholderException("Missing placeholder " + k + ".");
        }
        return p;
    }

    private void tryApply(Object source, Object target, String k, PlaceholderArgs a, String key, Map<String, Object> toApply) {
        try {
            a = sanitizeArgs(source, target, a);
            Placeholder<?> p = tryGetPlaceholder(k, a);
            Object v = p.parse(source, target, a);
            if (v == null) {
                v = "null"; // TODO prefer empty?
            }
            if (v instanceof Optional) {
                Optional<?> ov = ((Optional<?>) v);
                if (ov.isPresent()) {
                    v = ov.get();
                } else {
                    v = "null"; // TODO again empty?
                }
            }
            toApply.put(key, v);
        } catch (NoValueException e) {
            toApply.put(key, "");
        } catch (Throwable e) {
            toApply.put(key, e.getMessage());
            if (PlaceholderRedux.isVerbose()) {
                e.printStackTrace();
            }
        }
    }

    @Nonnull
    public TextTemplate partialApply(Object source, Object target) {
        Map<String, Object> toApply = new HashMap<>();
        args.forEach((k, a) -> {
            tryApply(source, target, k, a, k, toApply);
        });
        List<Object> objs = new ArrayList<>();
        if (fmtsource != null) {
            objs.add(fmtsource.getFormat());
            fmtsource.getClickAction().ifPresent(objs::add);
            fmtsource.getHoverAction().ifPresent(objs::add);
            fmtsource.getShiftClickAction().ifPresent(objs::add);
        }
        for (Object o : template.getElements()) {
            if (o instanceof TextTemplate.Arg) {
                TextTemplate.Arg arg = (TextTemplate.Arg) o;
                objs.add(toApply.getOrDefault(arg.getName(), o));
            } else {
                objs.add(o);
            }
        }
        return TextTemplate.of(template.getOpenArgString(), template.getCloseArgString(), objs.toArray());
    }

    @Override
    @Nonnull
    public Text toText() {
        return apply(null, null);
    }
}
