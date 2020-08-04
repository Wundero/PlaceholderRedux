package tech.wundero.placeholderredux.api.exceptions;

public class InvalidSourceException extends PlaceholderException {

    public InvalidSourceException() {
        super("Invalid source or target.");
    }


    public InvalidSourceException(String message) {
        super(message);
    }

}
