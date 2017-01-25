package mark.detection;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import mark.core.Subject;
import mark.core.Evidence;
import mark.core.ServerInterface;
import mark.core.SubjectAdapter;

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
    private String label;
    private String input_label;
    private T subject;
    private Map<String, String> parameters;
    private URL datastore_url;
    private SubjectAdapter<T> subject_adapter;

    // Lazy instantiated fields:
    private transient ServerInterface<T> datastore;

    /**
     *
     */
    public AbstractDetectionAgent() {

    }

    protected final String getInputLabel() {
        return input_label;
    }

    public final void setInputLabel(final String input_label) {
        this.input_label = input_label;
    }

    public final void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    protected final String getLabel() {
        return label;
    }

    public final void setLabel(final String type) {
        this.label = type;
    }


    protected T getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = (T) subject;
    }

    /**
     * Return a connection to the server.
     * @return
     */
    protected final ServerInterface<T> getDatastore() throws MalformedURLException {
        // Lazy initialization...
        if (datastore == null) {
            datastore = subject_adapter.getDatastore(datastore_url);
        }

        return datastore;
    }

    public final void setDatastoreUrl(final URL datastore_url) {
        this.datastore_url = datastore_url;
    }

    /**
     * Get the value for parameter name, or null if this parameter was not
     * provided.
     * @param name
     * @return
     */
    protected final String getParameter(final String name) {
        return parameters.get(name);
    }

    /**
     * Create the basic Evidence object, with fields that were provided by
     * the activation logic: client, server and label.
     * @return
     */
    protected Evidence createEvidenceTemplate() {
        Evidence evidence = new Evidence();
        evidence.subject = getSubject();
        evidence.label = getLabel();
        return evidence;
    }

    public void setSubjectAdapter(SubjectAdapter subject_adapter) {
        this.subject_adapter = subject_adapter;
    }
}
