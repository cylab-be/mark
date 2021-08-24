package be.cylab.mark.detection;

import be.cylab.mark.core.ClientWrapperInterface;
import be.cylab.mark.core.DetectionAgentInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;

/**
 * Dummy detection agent, which does not try to read or write to the datastore.
 * Can be used to test activation, without starting a complete server.
 * @author Thibault Debatty
 */
public class DummyDetector implements DetectionAgentInterface {

    private static final int SLEEP_TIME = 500;


    @Override
    public void analyze(
            final Event event,
            final DetectionAgentProfile profile,
            final ClientWrapperInterface datastore) throws Throwable {

        Thread.sleep(SLEEP_TIME);
    }
}
