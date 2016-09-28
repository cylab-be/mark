package mark.activation;

import java.util.Map;

/**
 *
 * @author Thibault Debatty
 */
interface DetectionAgentInterface extends Runnable {
    void setParameters(Map<String, Object> parameters);
}
