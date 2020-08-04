package tech.wundero.placeholderredux.data;

import com.google.common.base.MoreObjects;
import tech.wundero.placeholderredux.api.Placeholder;
import tech.wundero.placeholderredux.api.PlaceholderArgs;
import tech.wundero.placeholderredux.api.PlaceholderBuilder;
import tech.wundero.placeholderredux.api.function.PlaceholderFunction;
import tech.wundero.placeholderredux.api.data.PlaceholderData;
import tech.wundero.placeholderredux.api.data.PlaceholderDataBuilder;
import tech.wundero.placeholderredux.api.exceptions.PlaceholderException;

import javax.annotation.Nonnull;
import java.util.Objects;

/*
 * Builder class for placeholders. Accepts a placeholder function and a placeholder metadata.
 */
public class FluidBuilder<T> implements PlaceholderBuilder<T, FluidBuilder<T>> {

    private Meta meta;
    private PlaceholderFunction<T> function;

    /*
     * Semi-hidden constructor, use static alternative.
     */
    FluidBuilder() {
        reset();
    }

    /**
     * Create a new placeholder builder.
     * @param <T> The return type of the placeholder.
     * @return The new placeholder builder.
     */
    public static <T> FluidBuilder<T> builder() {
        return new FluidBuilder<>();
    }

    /**
     * Create the new placeholder.
     * @return The new placeholder.
     */
    @Override
    public Placeholder<T> build() {
        // Enforce non-null. I could use Objects.requireNonNull or something, but this is good enough.
        if (meta == null) {
            throw new IllegalArgumentException("ID cannot be null!");
        }
        if (function == null) {
            throw new IllegalArgumentException("Placeholder cannot be null!");
        }
        return new FluidPlaceholder<>(meta, function);
    }

    /*
     * Class implementing necessary parts.
     */
    private static class FluidPlaceholder<T> implements Placeholder<T> {
        private final Meta m; // metadata
        private final PlaceholderFunction<T> fn; // function

        public FluidPlaceholder(Meta m, PlaceholderFunction<T> fn) {
            this.m = m;
            this.fn = fn;
        }

        @Override
        public boolean equals(Object o) { // id comparison only
            if (this == o) return true;
            if (!(o instanceof Placeholder)) {
                return false;
            }
            Placeholder<?> p = (Placeholder<?>) o;
            return p.id().equals(this.id());
        }

        @Override
        public String toString() { // simply the id
            return MoreObjects.toStringHelper(this).add("id", id()).toString();
        }

        @Override
        public int hashCode() { // id comparison only
            return Objects.hash(id());
        }

        @Nonnull
        @Override
        public PlaceholderData data() { // produce metadata
            return m;
        }

        @Override
        public T parse(Object source, Object target, PlaceholderArgs args) throws PlaceholderException {
            // fn delegation
            return fn.parse(source, target, args);
        }
    }

    /*
     * Reset the builder to the newly created state.
     */
    @Override
    public FluidBuilder<T> reset() {
        this.meta = null;
        this.function = null;
        return this;
    }

    /*
     * Return a placeholder data builder.
     */
    @Override
    public PlaceholderDataBuilder<? extends PlaceholderData, ? extends PlaceholderDataBuilder<?, ?>> meta() {
        return new MetaBuilder((m) -> {
            this.meta = m;
        });
    }

    @Override
    public FluidBuilder<T> placeholderFunction(PlaceholderFunction<T> function) {
        this.function = function;
        return this;
    }
}
