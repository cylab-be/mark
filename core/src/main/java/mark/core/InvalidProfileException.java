package mark.core;

/**
 *
 * @author Thibault Debatty
 */
public class InvalidProfileException extends Exception {

    /**
     *
     * @param message
     */
    public InvalidProfileException(final String message) {
        super(message);
    }

    /**
     *
     * @param message
     * @param ex
     */
    public InvalidProfileException(final String message, final Exception ex) {
        super(message, ex);
    }
}
