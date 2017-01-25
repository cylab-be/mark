package mark.data;

import mark.core.ServerInterface;
import mark.server.SafeThread;

/**
 * The server will try to stop data agents using agent.interrupt() then
 * agent.wait(). Hence data agents should handle this one properly...
 *
 * @author Thibault Debatty
 */
public abstract class AbstractDataAgent extends SafeThread {

    public abstract void setDatastore(ServerInterface datastore);

    public abstract void setProfile(DataAgentProfile profile);

}
