package tech.wundero.placeholderredux.reflect;

import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.Tuple;
import tech.wundero.placeholderredux.api.annotations.*;
import tech.wundero.placeholderredux.api.function.FunctionArg;
import tech.wundero.placeholderredux.function.FunctionArgs;
import tech.wundero.placeholderredux.service.RegistryService;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Analyzer {

    private Object o;
    private String pid;
    private String mname;
    private Method mtd;
    private Class<?> ret;
    private Optional<GenArg> src = Optional.empty();
    private int srcind = -1;
    private Optional<GenArg> target = Optional.empty();
    private int trgind = -1;
    private final Map<String, GenArg> args;
    private boolean stat = false;
    private boolean valid = false;

    Analyzer(Object o, String p) {
        this.o = o;
        this.pid = p;
        args = new HashMap<>();
        setup();
    }

    Analyzer(Object o, Method m, String p) {
        this.o = o;
        this.pid = p;
        args = new HashMap<>();
        setup(m);
    }

    private Tristate setup(Method m) {
        int mod = m.getModifiers();
        if (!Modifier.isPublic(mod)) {
            return Tristate.UNDEFINED;
        }
        if (!m.isAnnotationPresent(Replacer.class)) {
            return Tristate.UNDEFINED;
        }
        Replacer r = m.getAnnotation(Replacer.class);
        if (!r.value().equals(pid)) {
            return Tristate.UNDEFINED;
        }
        stat = Modifier.isStatic(mod);
        mname = m.getName();
        Parameter[] paramnames = m.getParameters();
        for (int i = 0; i < paramnames.length; i++) {
            Parameter param = paramnames[i];
            boolean f = false;
            if (param.isAnnotationPresent(Source.class)) {
                if (src.isPresent()) {
                    valid = false;
                    return Tristate.FALSE;
                }
                f = true;
                src = Optional.of(new GenArg(FunctionArgs.createFromParam(param), -1, param.isAnnotationPresent(Nullable.class)));
                srcind = i;
            }
            if (param.isAnnotationPresent(Target.class)) {
                if (target.isPresent() || f) {
                    valid = false;
                    return Tristate.FALSE;
                }
                f = true;
                target = Optional.of(new GenArg(FunctionArgs.createFromParam(param), -1, param.isAnnotationPresent(Nullable.class)));
                trgind = i;
            }
            if (param.isAnnotationPresent(Arg.class)) {
                String name = param.getAnnotation(Arg.class).value();
                if (args.containsKey(name) || f) {
                    valid = false;
                    return Tristate.FALSE;
                }
                if (!RegistryService.matchesIDPattern(name)) {
                    valid = false;
                    return Tristate.FALSE;
                }
                f = true;
                args.put(name, new GenArg(FunctionArgs.createFromParam(param), i, param.isAnnotationPresent(Nullable.class)));
            }
            if (!f) {
                valid = false;
                return Tristate.FALSE;
            }
        }
        ret = m.getReturnType();
        valid = true;
        mtd = m;
        return Tristate.TRUE;
    }

    int srcind() {
        return srcind;
    }

    int trgind() {
        return trgind;
    }

    int argind(String arg) {
        return Optional.ofNullable(args.get(arg)).map(GenArg::getPos).orElse(-1);
    }

    private void setup() {
        Class<?> clazz = o.getClass();
        for (Method m : clazz.getDeclaredMethods()) {
            Tristate res = setup(m);
            if (res.equals(Tristate.FALSE) || res.equals(Tristate.TRUE)) {
                return;
            }
        }
        valid = false;
    }

    Method getMethod() {
        return mtd;
    }

    String getName() {
        return mname;
    }

    Class<?> ret() {
        return ret;
    }

    boolean isStatic() {
        return stat;
    }

    boolean valid() {
        return valid;
    }

    Optional<GenArg> source() {
        return src;
    }

    Optional<GenArg> target() {
        return target;
    }

    Map<String, GenArg> args() {
        return args;
    }

}
