package be.cylab.mark.activation;

import java.io.Serializable;
import java.net.URL;
import be.cylab.mark.client.Client;
import be.cylab.mark.core.DetectionAgentInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
import be.cylab.mark.core.Subject;
import be.cylab.mark.core.ServerInterface;
import be.cylab.mark.core.SubjectAdapter;
import org.slf4j.LoggerFactory;

/**
 * Container for running a detection agent.
 *
 * @author Thibault Debatty
 * @param <T> The type of Subject that this detection agent can deal with
 */
public class DetectionAgentContainer <T extends Subject>
                        implements Serializable, Runnable {

    // Things that are provided by the activation logic engine:
    private final URL datastore_url;
    private final SubjectAdapter<T> subject_adapter;
    private final DetectionAgentProfile profile;
    private final DetectionAgentInterface<T> agent;
    private final Event<T> event;


    /**
     *
     * @param ev
     * @param datastore_url
     * @param subject_adapter
     * @param profile
     * @param agent
     */
    public DetectionAgentContainer(
            final Event<T> ev,
            final URL datastore_url,
            final SubjectAdapter<T> subject_adapter,
            final DetectionAgentProfile profile,
            final DetectionAgentInterface<T> agent) {

        this.event = ev;
        this.profile = profile;
        this.agent = agent;
        this.subject_adapter = subject_adapter;
        this.datastore_url = datastore_url;
    }

    /**
     *
     */
    @Override
    public final void run() {
        ServerInterface<T> datastore = new Client<T>(
                datastore_url, subject_adapter);

        try {
            agent.analyze(event, profile, datastore);
        } catch (Throwable ex) {
            LoggerFactory.getLogger(this.getClass().getName()).error(
                    "Detector failed with exception!", ex);
        }
    }


}
