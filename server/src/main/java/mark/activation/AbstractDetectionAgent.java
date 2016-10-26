package mark.activation;

import java.util.Map;
import mark.core.ServerInterface;

/**
 *
 * @author Thibault Debatty
 */
public abstract class AbstractDetectionAgent implements DetectionAgentInterface {

    // Things that are provided by the activation logic engine:
    private String type;
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

    public final String getType() {
        return type;
    }

    public final void setType(final String type) {
        this.type = type;
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
}
