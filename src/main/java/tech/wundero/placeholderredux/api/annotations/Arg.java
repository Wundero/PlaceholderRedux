package tech.wundero.placeholderredux.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter annotation to indicate that the annotated parameter
 *  should be parsed from the placeholder string as an argument.
 *
 * Argument names MUST be unique, or else the generator will fail
 *  to create the placeholder.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Arg {

    /**
     * A unique name ("id") for the annotated argument.
     *
     * @return The name of the argument
     */
    String value();

}
