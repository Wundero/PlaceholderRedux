package tech.wundero.placeholderredux.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation to indicate to the generator that this method
 *  should be converted into a placeholder.
 *
 * The provided {@code value} may not be unique to generate a placeholder, but
 *  may fail to register should an existing placeholder exist with said ID.
 */
// TODO rename?
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Replacer {

    /**
     * A unique ID for which to represent the placeholder.
     *
     * Format is one alphanumeric, with underscores and dashes.
     *  The string must not start or end with an _ or a -, and must be at least
     *  one character long.
     *  Valid examples: placeholder_name, placeholder, placeholder-name
     *  Invalid examples: _placeholder, placeholder:name, placeholder|name, etc.
     *
     * @return The placeholder's ID
     */
    String value();

}
