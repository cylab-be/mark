package mark.server;

/**
 *
 * @author Thibault Debatty
 */
public class InvalidProfileException extends Exception {

    public InvalidProfileException(String message) {
        super(message);
    }

    public InvalidProfileException(String message, Exception ex) {
        super(message, ex);
    }
}