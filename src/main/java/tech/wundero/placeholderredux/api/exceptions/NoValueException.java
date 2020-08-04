package tech.wundero.placeholderredux.api.exceptions;

public class NoValueException extends PlaceholderException {

    public NoValueException() {
        super("No value available.");
    }


    public NoValueException(String message) {
        super(message);
    }
}
