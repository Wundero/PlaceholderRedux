package tech.wundero.placeholderredux.api;

import java.util.Optional;

/**
 * A data class that represents the arguments parsed by the parser.
 *
 * These arguments are provided to a placeholder via the {@link Placeholder#parse(Object, Object, PlaceholderArgs)} function.
 */
public interface PlaceholderArgs {

    /**
     * Get an argument given a specific type. This method will call {@link PlaceholderArgs#getArg(String)}, and then attempt
     *  to cast the result to the expected class. If the result is empty or the cast fails, {@link Optional#empty()} will be
     *  returned.
     *
     * @param value The argument key.
     * @param expected The type used to cast the resultant argument.
     * @param <T> The type parameter.
     * @return The argument, if it exists and is of the correct type.
     */
    default <T> Optional<T> getArg(String value, Class<T> expected) {
        return getArg(value).flatMap(o -> {
            if (expected.isInstance(o)) {
                return Optional.of(expected.cast(o));
            }
            return Optional.empty();
        });
    }

    /**
     * Get an argument given its key.
     *
     * If the argument is not contained in the map, this will return {@link Optional#empty()}.
     *
     * @param value The argument key.
     * @return The argument, if it exists.
     */
    Optional<Object> getArg(String value);
}
