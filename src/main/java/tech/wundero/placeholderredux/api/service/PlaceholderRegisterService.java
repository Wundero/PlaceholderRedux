package tech.wundero.placeholderredux.api.service;

import tech.wundero.placeholderredux.api.PlaceholderArgs;
import tech.wundero.placeholderredux.api.exceptions.MissingPlaceholderException;
import tech.wundero.placeholderredux.api.Placeholder;
import tech.wundero.placeholderredux.api.exceptions.PlaceholderException;

import java.util.Optional;
import java.util.Set;

public interface PlaceholderRegisterService {

    boolean register(Placeholder<?> placeholder);

    default boolean hasPlaceholder(String id) {
        return getPlaceholder(id).isPresent();
    }

    default <T> Optional<Placeholder<T>> getPlaceholder(String id) {
        return getPlaceholders().stream().filter(p -> p.id().equals(id)).findAny().map(p -> (Placeholder<T>) p);
    }

    Set<Placeholder<?>> getPlaceholders();

    default <T> T parse(String id, Object source, Object target, PlaceholderArgs args) throws PlaceholderException {
        return (T) getPlaceholder(id).orElseThrow(MissingPlaceholderException::new).parse(source, target, args);
    }

}
