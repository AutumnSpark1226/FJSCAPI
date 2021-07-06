package Exception;

/**
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
