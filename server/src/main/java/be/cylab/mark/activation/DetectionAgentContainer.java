package be.cylab.mark.activation;

import be.cylab.mark.core.ClientWrapperInterface;
import java.io.Serializable;
import be.cylab.mark.core.DetectionAgentInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
import org.slf4j.LoggerFactory;

/**
 * Container for running a detection agent.
 *
 * @author Thibault Debatty
 */
public class DetectionAgentContainer implements Serializable, Runnable {

    // Things that are provided by the activation logic engine:
    private final DetectionAgentProfile profile;
    private final DetectionAgentInterface agent;
    private final Event event;
    private final DetectionAgentConfig config;


    /**
     *
     * @param ev
     * @param config
     * @param profile
     * @param agent
     */
    public DetectionAgentContainer(
            final Event ev,
            final DetectionAgentConfig config,
            final DetectionAgentProfile profile,
            final DetectionAgentInterface agent) {

        this.event = ev;
        this.profile = profile;
        this.agent = agent;
        this.config = config;
    }

    /**
     *
     */
    @Override
    public final void run() {
        ClientWrapperInterface datastore = new ClientWrapper(
                config, profile);

        try {
            agent.analyze(event, profile, datastore);
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
            LoggerFactory.getLogger(this.getClass().getName()).error(
                    "Detector failed with exception!", ex);
        }
    }


}
