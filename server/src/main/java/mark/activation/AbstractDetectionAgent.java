package mark.activation;

import java.util.Map;
import mark.core.AnalysisUnit;
import mark.core.Evidence;
import mark.core.Link;
import mark.core.ServerInterface;

/**
 *
 * @author Thibault Debatty
 */
public abstract class AbstractDetectionAgent implements DetectionAgentInterface {

    // Things that are provided by the activation logic engine:
    private String label;
    private Link subject;
    private Map<String, String> parameters;
    private ServerInterface datastore;

    /**
     *
     */
    public AbstractDetectionAgent() {

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


    public Link getSubject() {
        return subject;
    }

    public void setSubject(Link subject) {
        this.subject = subject;
    }



    /**
     * Return a connection to the server.
     * @return
     */
    public final ServerInterface getDatastore() {
        return datastore;
    }

    public final void setDatastore(final ServerInterface datastore) {
        this.datastore = datastore;
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
}
