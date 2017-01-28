package mark.detection;

import java.net.URL;
import mark.activation.DetectionAgentProfile;
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
public abstract class AbstractDetectionAgent<T extends Subject>
        implements DetectionAgentInterface {

    // Things that are provided by the activation logic engine:
    private T subject;
    private URL datastore_url;
    private SubjectAdapter<T> subject_adapter;
    private String actual_trigger_label;
    private DetectionAgentProfile profile;


    /**
     *
     */
    public AbstractDetectionAgent() {

    }

    public void setDetectionAgentProfile(DetectionAgentProfile profile) {
        this.profile = profile;
    }


    public final void setActualTriggerLabel(final String actual_trigger_label) {
        this.actual_trigger_label = actual_trigger_label;
    }

    /**
     * Setters are used by the activation controller to configure the
     * detection agent.
     * @param subject
     */
    public void setSubject(final Subject subject) {
        this.subject = (T) subject;
    }

    /**
     * Setters are used by the activation controller to configure the
     * detection agent.
     * @param datastore_url
     */
    public final void setDatastoreUrl(final URL datastore_url) {
        this.datastore_url = datastore_url;
    }

    public void setSubjectAdapter(final SubjectAdapter subject_adapter) {
        this.subject_adapter = subject_adapter;
    }

    /**
     *
     */
    public final void run() {
        ServerInterface<T> datastore = subject_adapter.getDatastore(
                datastore_url);

        try {
            analyze(subject, actual_trigger_label, profile, datastore);
        } catch (Throwable ex) {
            LoggerFactory.getLogger(this.getClass().getName()).error(
                    "Detector failed with exception!", ex);
        }
    }

    public abstract void analyze(
            T subject,
            String actual_trigger_label,
            DetectionAgentProfile profile,
            ServerInterface<T> datastore) throws Throwable;
}
