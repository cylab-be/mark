package mark.activation;

import java.io.Serializable;
import java.net.URL;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Subject;
import mark.core.ServerInterface;
import mark.core.SubjectAdapter;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of a detection agent.
 * It implements the DetectionAgentInterface so it can be instantiated and
 * started by the server.
 *
 * The datastore field is lazy instantiated and transient such that, if we run
 * over an Ignite cluster, the field is not serialized and it will be
 * instantiated when it is run on the distant compute node.
 *
 * The datastore_factory is responsible for instantiating the correct
 * datastore implementation (real datastore or dummy datastore for testing).
 *
 * Some detection agents may be generic, or they can be designed for one type
 * of Subject, hence they should extend AbstractDetectionAgent<RealSubject>.
 *
 * @author Thibault Debatty
 * @param <T> The type of Subject that this detection agent can deal with
 */
public class DetectionAgentContainer <T extends Subject>
                        implements Serializable, Runnable {

    // Things that are provided by the activation logic engine:
    private final T subject;
    private final URL datastore_url;
    private final SubjectAdapter<T> subject_adapter;
    private final String actual_trigger_label;
    private final DetectionAgentProfile profile;
    private final DetectionAgentInterface<T> agent;


    /**
     *
     */
    public DetectionAgentContainer(T subject, URL datastore_url,
            SubjectAdapter<T> subject_adapter, String actual_trigger_label,
            DetectionAgentProfile profile, DetectionAgentInterface<T> agent) {
        this.profile = profile;
        this.agent = agent;
        this.subject_adapter = subject_adapter;
        this.actual_trigger_label = actual_trigger_label;
        this.subject = subject;
        this.datastore_url = datastore_url;

    }

    /**
     *
     */
    public final void run() {
        ServerInterface<T> datastore = subject_adapter.getDatastore(
                datastore_url);

        try {
            agent.analyze(subject, actual_trigger_label, profile, datastore);
        } catch (Throwable ex) {
            LoggerFactory.getLogger(this.getClass().getName()).error(
                    "Detector failed with exception!", ex);
        }
    }


}
