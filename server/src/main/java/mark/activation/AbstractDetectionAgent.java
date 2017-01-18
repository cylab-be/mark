package mark.activation;

import java.net.MalformedURLException;
import java.util.Map;
import mark.core.Subject;
import mark.core.Evidence;
import mark.core.ServerInterface;
import mark.core.SubjectAdapter;

/**
 *
 * @author Thibault Debatty
 */
public abstract class AbstractDetectionAgent<T extends Subject>
        implements DetectionAgentInterface<T> {

    // Things that are provided by the activation logic engine:
    private String label;
    private String input_label;
    private T subject;
    private Map<String, String> parameters;
    private String datastore_url;
    private SubjectAdapter<T> subject_adapter;

    // Lazy initialized fields:
    private ServerInterface<T> datastore;
    private DatastoreFactory datastore_factory;

    /**
     *
     */
    public AbstractDetectionAgent() {

    }

    public final String getInputLabel() {
        return input_label;
    }

    public final void setInputLabel(final String input_label) {
        this.input_label = input_label;
    }

    public final void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public final String getLabel() {
        return label;
    }

    public final void setLabel(final String type) {
        this.label = type;
    }


    public T getSubject() {
        return subject;
    }

    public void setSubject(T subject) {
        this.subject = subject;
    }

    /**
     * Return a connection to the server.
     * @return
     */
    public final ServerInterface<T> getDatastore() throws MalformedURLException {
        // Lazy initialization...
        if (datastore == null) {
            datastore = getDatastoreFactory().getInstance(datastore_url);
        }

        return datastore;
    }

    public final void setDatastoreUrl(final String datastore_url) {
        this.datastore_url = datastore_url;
    }

    /**
     * Get the value for parameter name, or null if this parameter was not
     * provided.
     * @param name
     * @return
     */
    public final String getParameter(final String name) {
        return parameters.get(name);
    }

    /**
     * Create the basic Evidence object, with fields that were provided by
     * the activation logic: client, server and label.
     * @return
     */
    public Evidence createEvidenceTemplate() {
        Evidence evidence = new Evidence();
        evidence.subject = getSubject();
        evidence.label = getLabel();
        return evidence;
    }

    private DatastoreFactory getDatastoreFactory() {
        // Lazy initialization
        if (datastore_factory == null) {
            datastore_factory = new DefaultDatastoreFactory(subject_adapter);
        }

        return datastore_factory;
    }

    /**
     * Allows to use a DummyDatastore for testing, for example.
     * @param datastore_factory
     */
    public void setDatastoreFactory(DatastoreFactory datastore_factory) {
        this.datastore_factory = datastore_factory;
    }

    public void setSubjectAdapter(SubjectAdapter<T> subject_adapter) {
        this.subject_adapter = subject_adapter;
    }
}
