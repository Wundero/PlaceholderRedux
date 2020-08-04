package tech.wundero.placeholderredux.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter annotation to indicate that the "Source" object provided by
 *  callers of "apply(source, target)" onto a {@link tech.wundero.placeholderredux.api.PlaceholderTemplate}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Source {
}
