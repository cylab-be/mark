package mark.activation;

import java.util.Map;
import mark.client.Client;

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
    private String server_address;

    // Internal objects
    private Client datastore;

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

    public final void setServerAddress(String server_address) {
        this.server_address = server_address;
    }

    /**
     * Return a connection to the server, using the address that was provided
     * by the activation logic engine.
     * We use lazy initialization here, hence:
     * - the connection is instantiated and tested by the computer that will
     * really execute the analysis (not necessarily the same as the server)
     * - the Client object is not serialized and transmitted over the network.
     * @return
     * @throws java.net.ConnectException
     */
    public final Client getDatastore() throws Throwable {
        if (datastore == null) {
            datastore = new Client(server_address);
        }
        return datastore;
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
