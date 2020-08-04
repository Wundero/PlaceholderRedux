package tech.wundero.placeholderredux.api.function;

import tech.wundero.placeholderredux.api.PlaceholderArgs;
import tech.wundero.placeholderredux.api.exceptions.PlaceholderException;

public interface PlaceholderFunction<T> {

    T parse(Object source, Object target, PlaceholderArgs args) throws PlaceholderException;

}
