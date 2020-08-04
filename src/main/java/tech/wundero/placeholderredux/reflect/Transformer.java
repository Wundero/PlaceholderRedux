package tech.wundero.placeholderredux.reflect;

import org.spongepowered.api.util.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Transformer {

    private static AtomicInteger id = new AtomicInteger(0);

    private static String freshName(String name) {
        return name + id.getAndIncrement();
    }


    static Tuple<String, String> transformSource(GenArg source) {
        String name = freshName("source");
        String orElse = ".orElse";
        String thro = "Throw(tech.wundero.placeholderredux.api.exceptions.MissingArgsException::new)";
        String nul = "(null)";
        if (source.isNullable()) {
            orElse += nul;
        } else {
            orElse += thro;
        }
        if (source.getClazz().isAssignableFrom(String.class)) {
            return new Tuple<>(Generator.sanitizeArrays(source.getClazz(), false) + " " + name + " = java.util.Optional.ofNullable(source).map(java.lang.Object::toString)" + orElse + ";\n", name);
        }
        String appendage = "";
        String appendix = "";
        if (source.getClazz().isPrimitive()) {
            if (source.getClazz().equals(boolean.class)) { // all other prims can be = 0
                appendage = " " + name + " = false;\n";
            } else {
                appendage = " " + name + " = 0;\n";
            }
            appendage += "if (source != null) {\n   ";
            appendix = "\n}";
            if (!source.isNullable()) {
                appendix += " else {\n" +
                        "    throw new tech.wundero.placeholderredux.api.exceptions.MissingArgsException();\n}";
            }
        }
        return new Tuple<>(Generator.sanitizeArrays(source.getClazz(), false) + appendage + " " + name + " = (" + Generator.sanitizeArrays(source.getClazz(), false) + ") source;" + appendix + "\n", name);
    }


    static Tuple<String, String> transformTarget(GenArg target) {
        String name = freshName("target");

        String orElse = ".orElse";
        String thro = "Throw(tech.wundero.placeholderredux.api.exceptions.MissingArgsException::new)";
        String nul = "(null)";
        if (target.isNullable()) {
            orElse += nul;
        } else {
            orElse += thro;
        }
        if (target.getClazz().isAssignableFrom(String.class)) {
            return new Tuple<>(Generator.sanitizeArrays(target.getClazz(), false) + " " + name + " = java.util.Optional.ofNullable(target).map(java.lang.Object::toString)" + orElse + ";\n", name);
        }
        String appendage = "";
        String appendix = "";
        if (target.getClazz().isPrimitive()) {
            if (target.getClazz().equals(boolean.class)) { // all other prims can be = 0
                appendage = " " + name + " = false;\n";
            } else {
                appendage = " " + name + " = 0;\n";
            }
            appendage += "if (target != null) {\n   ";
            appendix = "\n}";
            if (!target.isNullable()) {
                appendix += " else {\n" +
                        "    throw new tech.wundero.placeholderredux.api.exceptions.MissingArgsException();\n}";
            }
        }
        return new Tuple<>(Generator.sanitizeArrays(target.getClazz(), false) + appendage + " " + name + " = (" + Generator.sanitizeArrays(target.getClazz(), false) + ") target;" + appendix + "\n", name);
    }


    static Map<String, Tuple<String, String>> transformArgs(Map<String, GenArg> args) {
        Map<String, Tuple<String, String>> out = new HashMap<>();
        for (Map.Entry<String, GenArg> e : args.entrySet()) {
            out.put(e.getKey(), transformArg(e.getKey(), e.getValue().getClazz(), e.getValue().isNullable()));
        }
        return out;
    }

    private static Tuple<String, String> transformArg(String arg, Class<?> type, boolean nullable) {
        String ogarg = arg;
        if (arg.equals("source") || arg.equals("target") || arg.equals("args")) {
            arg = freshName(arg);
        }
        String orElse = ".orElse";
        String thro = "Throw(tech.wundero.placeholderredux.api.exceptions.MissingArgsException::new)";
        String nul = "(null)";
        if (nullable) {
            orElse += nul;
        } else {
            orElse += thro;
        }
        if (type.isAssignableFrom(String.class)) {
            return new Tuple<>(Generator.sanitizeArrays(type, false) + " " + arg + " = args.getArg(\"" + ogarg + "\")" +
                    ".map(java.lang.Object::toString)" +
                    orElse + ";\n", arg);
        }
        return new Tuple<>(Generator.sanitizeArrays(type, false) + " " + arg + " = (" + Generator.sanitizeArrays(type, false) + ") args.getArg(\"" + ogarg + "\")" +
                orElse + ";\n", arg);
    }

}
