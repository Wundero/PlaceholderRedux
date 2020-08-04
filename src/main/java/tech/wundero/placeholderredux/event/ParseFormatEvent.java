package tech.wundero.placeholderredux.event;

import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import tech.wundero.placeholderredux.api.format.PlaceholderFormat;

// TODO why?
// TODO comments
public class ParseFormatEvent<T> extends AbstractEvent implements Cancellable {

    private Cause cause;
    private T text;
    private boolean cancelled = false;
    private PlaceholderFormat formatter;

    public ParseFormatEvent(Cause cause, T text, PlaceholderFormat format) {
        this.cause = cause;
        this.text = text;
        this.formatter = format;
    }

    public PlaceholderFormat getFormatter() {
        return formatter;
    }

    public T getText() {
        return text;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public Cause getCause() {
        return cause;
    }
}
