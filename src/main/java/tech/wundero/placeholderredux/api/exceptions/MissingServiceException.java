package tech.wundero.placeholderredux.api.exceptions;

public class MissingServiceException extends PlaceholderException{

    public MissingServiceException() {
        super("Missing service.");
    }


    public MissingServiceException(String message) {
        super(message);
    }
}
