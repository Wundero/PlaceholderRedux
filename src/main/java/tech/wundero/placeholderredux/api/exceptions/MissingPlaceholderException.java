package tech.wundero.placeholderredux.api.exceptions;

public class MissingPlaceholderException extends PlaceholderException {

    public MissingPlaceholderException() {
        super("Missing placeholder.");
    }


    public MissingPlaceholderException(String message) {
        super(message);
    }
}
