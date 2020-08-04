package tech.wundero.placeholderredux.api;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.TextTemplate;

import javax.annotation.Nonnull;

/**
 * A template class that will call {@link Placeholder#parse(Object, Object, PlaceholderArgs)} for all placeholders in the
 *  template it holds.
 *
 * This class serves as a placeholder-aware extension of the {@link TextTemplate} class, intending to populate some
 *  text representation (a {@link TextTemplate}, {@link Text}, or {@link String} object, usually) with placeholder data.
 *
 * The 'apply' function and the 'partialApply' function can be called any number of times, as the references held are not
 *  modified. This will *drastically* save performance when constant templates with placeholders need to be used many times.
 *  For example, a scoreboard plugin may choose to cache all the templates it intends on using, and then calling 'apply'
 *  or 'partialApply' in order to produce values that align with updated game state. Recursive placeholders will also be
 *  updated.
 */
public interface PlaceholderTemplate extends TextRepresentable {

    /**
     * Apply the given parameters to the template.
     *
     * @param source The source object to use.
     * @param target The target object to use.
     * @return The final text, populated with placeholders.
     */
    @Nonnull
    Text apply(Object source, Object target);

}
