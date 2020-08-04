package tech.wundero.placeholderredux.api;

import tech.wundero.placeholderredux.api.data.PlaceholderData;
import tech.wundero.placeholderredux.api.function.PlaceholderFunction;

import javax.annotation.Nonnull;

/**
 * A placeholder that will accept data and produce a result. Effectively a Function of 3 parameters -> one return
 *
 * Every placeholder has an ID. The ID is guaranteed to be unique only for the {@link tech.wundero.placeholderredux.api.service.PlaceholderRegisterService}
 *  that it is registered to.
 *
 * Interface serves as an explicit way to create placeholders. Prefer using {@link tech.wundero.placeholderredux.api.service.PlaceholderGeneratorService}
 *  to create placeholders from annotated methods, as type checking, parsing, and boilerplate are all generated
 *  automatically.
 *
 * @param <T> The return type that the 'parse' function produces.
 */
public interface Placeholder<T> extends PlaceholderFunction<T> {

    @Nonnull
    PlaceholderData data();

    @Nonnull
    default String id() {
        return data().placeholderID();
    }

}
