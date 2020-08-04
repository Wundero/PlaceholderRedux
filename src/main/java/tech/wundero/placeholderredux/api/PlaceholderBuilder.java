package tech.wundero.placeholderredux.api;

import tech.wundero.placeholderredux.api.data.PlaceholderData;
import tech.wundero.placeholderredux.api.data.PlaceholderDataBuilder;
import tech.wundero.placeholderredux.api.function.PlaceholderFunction;

import java.util.function.*;

public interface PlaceholderBuilder<T, B extends PlaceholderBuilder<T, B>> {

    Placeholder<T> build();

    B reset();

    PlaceholderDataBuilder<? extends PlaceholderData, ? extends PlaceholderDataBuilder<?, ?>> meta();

    // function type handling here

    default B supplierFunction(Supplier<T> supplier) {
        return placeholderFunction((s, t, a) -> supplier.get());
    }

    default B sourceFunction(Function<Object, T> function) {
        return placeholderFunction((s, t, a) -> function.apply(s));
    }

    default B sourceTargetBiFunction(BiFunction<Object, Object, T> function) {
        return placeholderFunction((s, t, a) -> function.apply(s, t));
    }

    B placeholderFunction(PlaceholderFunction<T> function);

}
