package tech.wundero.placeholderredux.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Parameter annotation to indicate that the "Target" object provided by
 *  callers of "apply(source, target)" onto a {@link tech.wundero.placeholderredux.api.PlaceholderTemplate}.
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.PARAMETER)
public @interface Target {
}
