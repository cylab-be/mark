package mark.agent.detection.dummy;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mark.activation.AbstractDetectionAgent;

/**
 * Dummy detection agent, which does not try to read or write to the datastore.
 * Can be used to test activation, without starting a complete masfad2 server.
 * @author Thibault Debatty
 */
public class Dummy extends AbstractDetectionAgent {

    public Dummy(String type, String client, String server) {
        super(type, client, server);
    }

    public final void run() {

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(Dummy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setParameters(Map<String, Object> parameters) {

    }

}
