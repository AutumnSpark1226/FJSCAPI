package Exception;

/**
 * This exception will be thrown if your password is wrong.
 * @author AutumnSpark1226
 * @version 2020.12.5
 */
public class FJSCAPIPasswordException extends Exception {
    String error;

    public FJSCAPIPasswordException(String error) {
        this.error = error;
    }

    public String toString() {
        return error;
    }
}
