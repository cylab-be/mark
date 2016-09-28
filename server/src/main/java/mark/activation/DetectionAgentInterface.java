package mark.activation;

import java.util.Map;

/**
 * The minimum interface for implementing a detection agent.
 * The most simple way to create a detection agent is to extend
 * AbstractDetectionAgent, which provides a lot of helpers.
 * @author Thibault Debatty
 */
interface DetectionAgentInterface extends Runnable {
    void setParameters(Map<String, String> parameters);
    void setType(String type);
    void setClient(String client);
    void setServer(String server);
    void setServerAddress(String address);

}
