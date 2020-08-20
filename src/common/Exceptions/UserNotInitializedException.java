package common.Exceptions;

public class UserNotInitializedException extends Exception{
    public UserNotInitializedException () {
        super("Error encountered: attempted communication without establishing an alias");
    }
}
