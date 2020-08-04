package tech.wundero.placeholderredux.data;

import org.spongepowered.api.text.Text;
import tech.wundero.placeholderredux.api.data.PlaceholderDataBuilder;
import tech.wundero.placeholderredux.api.function.FunctionArg;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Placeholder metadata builder class.
 */
public class MetaBuilder implements PlaceholderDataBuilder<Meta, MetaBuilder> {

    private String id = null;
    private String desc = null;
    private String url = null;
    private List<String> auth = null;
    private Supplier<List<Text>> help = null;
    private Map<String, FunctionArg<?>> types = null;
    private Function<String, List<String>> argumentNames = null;
    private Consumer<Meta> onBuild = m -> {};

    MetaBuilder() {
        reset();
    }

    @Override
    public Meta build() {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null!");
        }
        Meta meta = new Meta(id, desc, auth, url, help, types, argumentNames);
        onBuild.accept(meta);
        return meta;
    }

    MetaBuilder(Consumer<Meta> onBuild) {
        reset();
        this.onBuild = onBuild;
    }

    @Override
    public MetaBuilder reset() {
        id = null;
        desc = null;
        url = null;
        auth = null;
        help = null;
        types = null;
        argumentNames = null;
        return this;
    }

    @Override
    public MetaBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public MetaBuilder description(String description) {
        this.desc = description;
        return this;
    }

    @Override
    public String getDescription() {
        return this.desc;
    }

    @Override
    public MetaBuilder url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String getURL() {
        return this.url;
    }

    @Override
    public MetaBuilder authors(List<String> authors) {
        this.auth = authors;
        return this;
    }

    @Override
    public List<String> getAuthors() {
        return this.auth;
    }

    @Override
    public MetaBuilder authors(String... authors) {
        return authors(Arrays.asList(authors));
    }

    @Override
    public MetaBuilder addAuthors(String... authors) {
        return addAuthors(Arrays.asList(authors));
    }

    @Override
    public MetaBuilder addAuthors(Collection<String> authors) {
        if (this.auth == null) {
            this.auth = new ArrayList<>(authors);
            return this;
        }
        this.auth.addAll(authors);
        return this;
    }

    @Override
    public MetaBuilder helpFunction(Supplier<List<Text>> helpTextFunction) {
        this.help = helpTextFunction;
        return this;
    }

    @Override
    public MetaBuilder expectedArgumentTypes(Map<String, FunctionArg<?>> types) {
        this.types = types;
        return this;
    }

    @Override
    public MetaBuilder argumentSequencer(Function<String, List<String>> argumentNames) {
        this.argumentNames = argumentNames;
        return this;
    }
}
