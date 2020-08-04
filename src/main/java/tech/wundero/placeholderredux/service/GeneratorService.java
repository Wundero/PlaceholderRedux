package tech.wundero.placeholderredux.service;

import tech.wundero.placeholderredux.api.PlaceholderBuilder;
import tech.wundero.placeholderredux.api.service.PlaceholderGeneratorService;
import tech.wundero.placeholderredux.data.FluidBuilder;
import tech.wundero.placeholderredux.reflect.Generator;

import java.util.Optional;
import java.util.stream.Stream;

public class GeneratorService implements PlaceholderGeneratorService {

    @Override
    public Optional<PlaceholderBuilder<?, ? extends PlaceholderBuilder<?, ?>>> generateBuilder(Object object, String id) {
        try {
            return Optional.ofNullable(Generator.create(object, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public <T> PlaceholderBuilder<T, ? extends PlaceholderBuilder<T, ?>> builder() {
        return FluidBuilder.builder();
    }

    @Override
    public Stream<PlaceholderBuilder<?, ? extends PlaceholderBuilder<?, ?>>> generateBuilders(Object object) {
        return Generator.createAll(object);
    }
}
