package be.cylab.mark.activation;

import java.io.Serializable;
import java.net.URL;
import be.cylab.mark.core.DetectionAgentInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
import be.cylab.mark.core.ServerInterface;
import org.slf4j.LoggerFactory;

/**
 * Container for running a detection agent.
 *
 * @author Thibault Debatty
 */
public class DetectionAgentContainer implements Serializable, Runnable {

    // Things that are provided by the activation logic engine:
    private final URL datastore_url;
    private final DetectionAgentProfile profile;
    private final DetectionAgentInterface agent;
    private final Event event;


    /**
     *
     * @param ev
     * @param datastore_url
     * @param profile
     * @param agent
     */
    public DetectionAgentContainer(
            final Event ev,
            final URL datastore_url,
            final DetectionAgentProfile profile,
            final DetectionAgentInterface agent) {

        this.event = ev;
        this.profile = profile;
        this.agent = agent;
        this.datastore_url = datastore_url;
    }

    /**
     *
     */
    @Override
    public final void run() {
        ServerInterface datastore = new ClientWrapper(
                datastore_url, profile);

        try {
            agent.analyze(event, profile, datastore);
        } catch (Throwable ex) {
            LoggerFactory.getLogger(this.getClass().getName()).error(
                    "Detector failed with exception!", ex);
        }
    }


}
