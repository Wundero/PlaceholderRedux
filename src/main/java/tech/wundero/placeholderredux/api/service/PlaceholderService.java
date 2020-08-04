package tech.wundero.placeholderredux.api.service;

import org.spongepowered.api.Sponge;
import tech.wundero.placeholderredux.api.Placeholder;
import tech.wundero.placeholderredux.api.PlaceholderBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface PlaceholderService {

    default Optional<PlaceholderGeneratorService> generator() {
        return Sponge.getServiceManager().provide(PlaceholderGeneratorService.class);
    }

    default Optional<PlaceholderReplacerService> replacer() {
        return Sponge.getServiceManager().provide(PlaceholderReplacerService.class);
    }

    default Optional<PlaceholderRegisterService> registry() {
        return Sponge.getServiceManager().provide(PlaceholderRegisterService.class);
    }

    default List<String> generateAndRegister(Object object) {
        return generator().flatMap(g -> registry().map(r ->
                g.generateBuilders(object)
                        .map(PlaceholderBuilder::build)
                        .filter(r::register)
                        .map(Placeholder::id)
                        .collect(Collectors.toList())
        )).orElseGet(ArrayList::new);
    }

}
