package mark.detection;

/**
 * Dummy detection agent, which does not try to read or write to the datastore.
 * Can be used to test activation, without starting a complete server.
 * @author Thibault Debatty
 */
public class Dummy extends AbstractDetectionAgent {

    private static final int SLEEP_TIME = 500;

    /**
     * {@inheritDoc}
     */
    public final void run() {

        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException ex) {

        }
    }
}
