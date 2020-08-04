package tech.wundero.placeholderredux.api.data;

import org.spongepowered.api.text.Text;
import tech.wundero.placeholderredux.api.function.FunctionArg;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PlaceholderDataBuilder<T extends PlaceholderData, B extends PlaceholderDataBuilder<T, B>> {

    T build();

    B reset();

    B id(String id);

    String getId();

    B description(String description);

    String getDescription();

    B url(String url);

    String getURL();

    B authors(List<String> authors);

    List<String> getAuthors();

    B authors(String... authors);

    B addAuthors(String... authors);

    B addAuthors(Collection<String> authors);

    B helpFunction(Supplier<List<Text>> helpTextFunction);

    B expectedArgumentTypes(Map<String, FunctionArg<?>> types);

    B argumentSequencer(Function<String, List<String>> argumentNames);
}
