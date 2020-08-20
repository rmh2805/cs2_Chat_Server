package common.Exceptions;

public class ParseException extends Exception {
    public ParseException (String message) {
        super ("Unable to parse line:" +message);
    }
}
