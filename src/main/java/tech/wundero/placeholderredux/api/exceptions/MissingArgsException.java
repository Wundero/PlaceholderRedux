package tech.wundero.placeholderredux.api.exceptions;

public class MissingArgsException extends PlaceholderException  {

    public MissingArgsException() {
        super("Missing arguments.");
    }


    public MissingArgsException(String message) {
        super(message);
    }

}
