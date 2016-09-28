package mark.agent.detection.dummy;

import java.util.Map;
import mark.activation.AbstractDetectionAgent;
import mark.client.Client;
import mark.core.Evidence;
import mark.core.RawData;

/**
 * Dummy detection agent that reads some raw data from datastore and writes
 * two evidences. Requires a running masfad2 server.
 * @author Thibault Debatty
 */
public class ReadWrite extends AbstractDetectionAgent {

    public ReadWrite(String type, String client, String server) {
        super(type, client, server);
    }

    public void run() {

        // Read data from datastore
        Client datastore = new Client();
        RawData[] data = datastore.findRawData(type, client, server);
        System.out.println("Found " + data.length + " elements");
        System.out.println(data[data.length - 1]);

        // Process data

        // Add evidences to datastore
        Evidence evidence = new Evidence();
        evidence.agent = getClass().getName();
        evidence.client = client;
        evidence.report = "Some report...";
        evidence.score = 0.6;
        evidence.server = server;
        evidence.time = data[0].time;
        datastore.addEvidence(evidence);

        evidence.score = 0.3;
        datastore.addEvidence(evidence);

    }

    public void setParameters(Map<String, Object> parameters) {

    }
}
