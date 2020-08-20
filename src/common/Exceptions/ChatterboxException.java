package common.Exceptions;

public class ChatterboxException extends Exception {
    public ChatterboxException (String message) {
        super ("Generic Chatterbox Exception: " + message);
    }
}
