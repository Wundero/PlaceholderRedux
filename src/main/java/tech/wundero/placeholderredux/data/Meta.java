package tech.wundero.placeholderredux.data;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.text.Text;
import tech.wundero.placeholderredux.api.data.PlaceholderData;
import tech.wundero.placeholderredux.api.function.FunctionArg;
import tech.wundero.placeholderredux.util.Utils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
// TODO comment
public class Meta implements PlaceholderData {

    private final String id;
    private final String description;
    private final List<String> authors;
    private final String url;
    private final Supplier<List<Text>> help;
    private final Map<String, FunctionArg<?>> types;
    private final Function<String, List<String>> args;

    public Meta(String id, String description, List<String> authors, String url, Supplier<List<Text>> help, Map<String, FunctionArg<?>> types, Function<String, List<String>> args) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(description); // TODO what? These can be null.
        Objects.requireNonNull(authors);
        Objects.requireNonNull(types);
        Objects.requireNonNull(args);
        if (help == null) {
            help = () -> Utils.genHelp(this);
        }
        this.id = id;
        this.description = description;
        this.authors = authors;
        this.url = url;
        this.help = help;
        this.types = Collections.unmodifiableMap(types);
        this.args = args;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("description", description)
                .add("authors", authors)
                .add("url", url)
                .add("types", types)
                .add("args", args)
                .toString();
    }

    @Override
    public String placeholderID() {
        return id;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public List<String> authors() {
        return authors;
    }

    @Override
    public Optional<String> url() {
        return Optional.ofNullable(url);
    }

    @Override
    public List<Text> helpText() {
        return help.get();
    }

    @Override
    public Map<String, FunctionArg<?>> expectedArgumentTypes() {
        return types;
    }

    @Override
    public Function<String, List<String>> argumentSequence() {
        return args;
    }

    public static MetaBuilder builder() {
        return new MetaBuilder();
    }
}
