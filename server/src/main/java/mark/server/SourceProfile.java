package mark.server;

import java.util.Map;

/**
 *
 * @author Thibault Debatty
 */
public class SourceProfile {

    /**
     * Name of the class to instantiate (must implement DataAgentInterface).
     */
    public String class_name;

    /**
     * Additional parameters to pass to the agent (e.g time range).
     */
    public Map<String, String> parameters;
}
