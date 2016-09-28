package mark.activation;

/**
 *
 * @author Thibault Debatty
 */
public class InvalidProfileException extends Exception {

    public InvalidProfileException(String message) {
        super(message);
    }

    InvalidProfileException(String message, Exception ex) {
        super(message, ex);
    }
}