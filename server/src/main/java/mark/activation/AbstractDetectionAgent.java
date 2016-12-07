package mark.activation;

import java.util.Map;
import mark.core.Evidence;
import mark.core.ServerInterface;

/**
 *
 * @author Thibault Debatty
 */
public abstract class AbstractDetectionAgent implements DetectionAgentInterface {

    // Things that are provided by the activation logic engine:
    private String label;
    private String client;
    private String server;
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

    public final String getClient() {
        return client;
    }

    public final void setClient(final String client) {
        this.client = client;
    }

    public final String getServer() {
        return server;
    }

    public final void setServer(final String server) {
        this.server = server;
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
        evidence.client = getClient();
        evidence.server = getServer();
        evidence.label = getLabel();
        return evidence;
    }
}
