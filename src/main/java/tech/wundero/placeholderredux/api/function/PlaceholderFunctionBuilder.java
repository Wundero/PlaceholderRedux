package tech.wundero.placeholderredux.api.function;

import tech.wundero.placeholderredux.api.PlaceholderBuilder;

public interface PlaceholderFunctionBuilder<T, B extends PlaceholderFunctionBuilder<T, B>> {

    PlaceholderFunction<T> build();

    PlaceholderBuilder<T, ? extends PlaceholderBuilder<T, ?>> applyTo(PlaceholderBuilder<T, ? extends PlaceholderBuilder<T, ?>> builder);

    B reset();

    B target(FunctionArg<?> arg);

    B source(FunctionArg<?> arg);

    B arg(String name, FunctionArg<?> arg);

}
