package mark.activation;

import java.util.Map;
import mark.core.AnalysisUnit;
import mark.core.Link;
import mark.core.ServerInterface;

/**
 * The minimum interface for implementing a detection agent.
 * The most simple way to create a detection agent is to extend
 * AbstractDetectionAgent, which provides a lot of helpers.
 * @author Thibault Debatty
 */
interface DetectionAgentInterface extends Runnable {
    void setParameters(Map<String, String> parameters);
    void setLabel(String label);
    void setSubject(Link subject);
    void setDatastore(ServerInterface datastore);

}
