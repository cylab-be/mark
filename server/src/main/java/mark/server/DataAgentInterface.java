package mark.server;

import java.util.Map;

/**
 *
 * @author Thibault Debatty
 */
public interface DataAgentInterface extends Runnable {

    /**
     *
     * @param parameters
     */
    void setParameters(Map<String, Object> parameters);
}
