package mark.server;

import mark.core.ServerInterface;

/**
 *
 * @author Thibault Debatty
 */
public interface DataAgentInterface extends Runnable {

    /**
     * Ask the task to stop, used by the server to stop data agent (sources)
     * tasks.
     * This method should return immediately, the server will wait for threads
     * to finish.
     * For file data sources, this does nothing.
     */
    void stop();

    /**
     * Ask the task to stop immediately, used by the server to stop data agent
     * (sources) tasks.
     * This method should return immediately, the server will wait for threads
     * to finish.
     * For file data sources, the agent should stop ASAP.
     */
    void kill();

    void setDatastore(ServerInterface datastore);

    public void setProfile(DataAgentProfile profile) throws Exception;

}
