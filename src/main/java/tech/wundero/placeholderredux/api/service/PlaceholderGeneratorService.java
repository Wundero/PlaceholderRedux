package tech.wundero.placeholderredux.api.service;

import tech.wundero.placeholderredux.api.PlaceholderBuilder;

import java.util.Optional;
import java.util.stream.Stream;

public interface PlaceholderGeneratorService {

    Optional<PlaceholderBuilder<?, ? extends PlaceholderBuilder<?, ?>>> generateBuilder(Object object, String id);

    <T> PlaceholderBuilder<T, ? extends PlaceholderBuilder<T, ?>> builder();

    Stream<PlaceholderBuilder<?, ? extends PlaceholderBuilder<?, ?>>> generateBuilders(Object object);

}
