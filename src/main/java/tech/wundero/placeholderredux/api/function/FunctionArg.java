package tech.wundero.placeholderredux.api.function;

public interface FunctionArg<T> {

    Class<T> type();

    boolean isValid(Object value);
}
