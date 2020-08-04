package tech.wundero.placeholderredux.function;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import tech.wundero.placeholderredux.api.function.FunctionArg;
import tech.wundero.placeholderredux.util.TypeUtils;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class FunctionArgs {

    public static <T> FunctionArg<T> or(FunctionArg<T> a, FunctionArg<T> b) {
        if (!a.type().equals(b.type())) {
            throw new IllegalArgumentException("Args must be matching types!");
        }
        return new OrArg<>(a, b);
    }

    public static FunctionArg<?> createFromParam(Parameter param) {
        Class<?> clazz = param.getType();
        // TODO annotation parsing
        return new TypeArg<>(clazz);
    }

    private static class OrArg<T> extends TypeArg<T> {

        private final FunctionArg<T> a, b;

        OrArg(FunctionArg<T> a, FunctionArg<T> b) {
            super(a.type());
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean isValid(Object value) {
            return super.isValid(value) && (a.isValid(value) || b.isValid(value));
        }
    }

    public static <T> FunctionArg<T> and(FunctionArg<T> a, FunctionArg<T> b) {
        if (!a.type().equals(b.type())) {
            throw new IllegalArgumentException("Args must be matching types!");
        }
        return new AndArg<>(a, b);
    }

    private static class AndArg<T> extends TypeArg<T> {

        private final FunctionArg<T> a, b;

        AndArg(FunctionArg<T> a, FunctionArg<T> b) {
            super(a.type());
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean isValid(Object value) {
            return super.isValid(value) && (a.isValid(value) || b.isValid(value));
        }
    }

    public static <T> FunctionArg<T> type(Class<T> type) {
        return new TypeArg<>(type);
    }

    private static class TypeArg<T> implements FunctionArg<T> {
        private final Class<T> type;

        TypeArg(Class<T> type) {
            this.type = type;
        }

        @Override
        public Class<T> type() {
            return type;
        }

        @Override
        public boolean isValid(Object value) {
            return type.isInstance(value);
        }
    }

    public static <T> FunctionArg<T> exact(T value) {
        return new ExactArg<>((Class<T>) value.getClass(), value);
    }

    private static class ExactArg<T> extends TypeArg<T> {

        ExactArg(Class<T> type, T value) {
            super(type);
            this.value = value;
        }

        private final T value;

        @Override
        public boolean isValid(Object value) {
            return super.isValid(value) && value.equals(this.value);
        }
    }

    @SafeVarargs
    public static <T> FunctionArg<T> choices(T... values) {
        Class<T> clazz = TypeUtils.getArrayClass(values);
        if (values.length == 0) {
            return new TypeArg<T>(clazz) {
                @Override
                public boolean isValid(Object value) {
                    return false;
                }
            };
        }
        return new ChoiceArg<>(clazz, Sets.newHashSet(values));
    }

//    @SafeVarargs
//    public static <T> FunctionArg<T> choices(Class<T> type, T... values) {
//        return new ChoiceArg<>(type, new HashSet<>(Arrays.asList(values)));
//    }

    private static class ChoiceArg<T> extends TypeArg<T> {

        ChoiceArg(Class<T> type, Set<T> vals) {
            super(type);
            this.values = vals;
        }

        private final Set<T> values;

        @Override
        public boolean isValid(Object value) {
            return super.isValid(value) && values.contains((T) value);
        }
    }

}
