package Exception;

/**
 * This exception will be thrown if your username breaks these rules: 1. It mustn't be 'server'. 2. It must be longer than 3 characters.
 * @author AutumnSpark1226
 * @version 2020.12.5
 */
public class FJSCAPIUsernameException extends Exception {
    String error;

    public FJSCAPIUsernameException(String error) {
        this.error = error;
    }

    public String toString() {
        return error;
    }
}
