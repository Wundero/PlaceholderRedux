package tech.wundero.placeholderredux.format;

import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.text.Text;
import tech.wundero.placeholderredux.api.format.PlaceholderFormat;
import tech.wundero.placeholderredux.format.ClassicFormat;
import tech.wundero.placeholderredux.format.JsonFormat;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * A catalog of all {@link PlaceholderFormat}s.
 */
public class PlaceholderFormats {

    /**
     * JSON Placeholder format.
     *
     * This formatter will parse incoming json from a text type, and convert that into placeholder data.
     *
     * This is the recommended route for any somewhat complex placeholder scheme, as it will provide proper
     *  object representation.
     *
     * This format also supports nested placeholders, allowing you to use the results of one placeholder as
     *  arguments to another, recursively. Please bear in mind that this may have a noticeable performance impact
     *  on the call to {@link PlaceholderFormat}'s 'parse' function with larger depth recursive placeholders. Ideally,
     *  the depth of the recursion will not negatively impact the performance of a {@link tech.wundero.placeholderredux.api.PlaceholderTemplate}'s
     *  'apply' functions quite as much, though the memory footprint will definitely not be ideal, as each nested placeholder
     *  also contains it's own set of arguments, which will create some large objects if not carefully constructed.
     *
     * Note that, as mentioned in {@link tech.wundero.placeholderredux.api.PlaceholderTemplate}, if you intend on using a
     *  template more than once, you should almost definitely cache it. With my implementation, parsing takes almost 20x longer
     *  than simply applying (this was for a complex placeholder test). While the parse times are still acceptable (0.5 ms on semi-complex parse),
     *  the times to use apply was closer to 25000 ns (or 0.025 ms), for a semi complex recursive template.
     *
     * This class fully supports asynchronous calls: simply call the functions from another thread. Any internal state
     *  is protected via a {@link java.util.concurrent.locks.ReentrantReadWriteLock}.
     */
    public static final JsonFormat JSON = new JsonFormat();

    // TODO comments for this bad boy right here
    public static final ClassicFormat CLASSIC = new ClassicFormat();

    public static final CatalogRegistryModule<PlaceholderFormat> VALUES = new CatalogRegistryModule<PlaceholderFormat>() {
        @Override
        @Nonnull
        public Optional<PlaceholderFormat> getById(String id) {
            switch (id) {
                case "placeholderredux:classic":
                    return Optional.of(CLASSIC);
                case "placeholderredux:json":
                    return Optional.of(JSON);
                default:
                    return Optional.empty();
            }
        }

        @Override
        @Nonnull
        public Collection<PlaceholderFormat> getAll() {
            return Arrays.asList(JSON, CLASSIC);
        }
    };
}
