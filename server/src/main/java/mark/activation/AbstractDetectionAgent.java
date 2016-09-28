package mark.activation;

/**
 *
 * @author Thibault Debatty
 */
public abstract class AbstractDetectionAgent implements DetectionAgentInterface {
    protected String type;
    protected String client;
    protected String server;

    public AbstractDetectionAgent(
            String type, String client, String server) {
        this.type = type;
        this.client = client;
        this.server = server;
    }

}
