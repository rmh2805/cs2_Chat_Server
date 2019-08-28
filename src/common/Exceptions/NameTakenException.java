package common.Exceptions;


public class NameTakenException extends Exception {
    public NameTakenException (String userName){
        super("Specified username ('" + userName + "') already claimed");
    }
}
