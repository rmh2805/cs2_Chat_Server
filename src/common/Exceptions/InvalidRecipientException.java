package common.Exceptions;

public class InvalidRecipientException extends Exception {
    public InvalidRecipientException (String username) {
        super("Specified username ('" + username + "') invalid");
    }
}
