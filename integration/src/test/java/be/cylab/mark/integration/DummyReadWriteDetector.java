package be.cylab.mark.integration;

import java.util.Random;
import be.cylab.mark.core.DetectionAgentInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import be.cylab.mark.core.ServerInterface;

/**
 * Dummy detection agent that reads some data from datastore and writes
 * two evidences.
 * @author Thibault Debatty
 */
public class DummyReadWriteDetector implements DetectionAgentInterface {


    @Override
    public final void analyze(
            final Event ev,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        RawData[] data = datastore.findRawData(
                ev.getLabel(),
                ev.getSubject(), 0, System.currentTimeMillis());

        if (data.length < 1) {
            return;
        }

        // Process data
        Random rand = new Random();

        // Add evidences to datastore
        Evidence evidence = new Evidence();
        evidence.setLabel(profile.getLabel());
        evidence.setSubject(ev.getSubject());
        evidence.setReport("Some report...");
        evidence.setScore(rand.nextDouble());
        evidence.setTime(data[data.length - 1].getTime());
        datastore.addEvidence(evidence);

        evidence.setScore(rand.nextDouble());
        datastore.addEvidence(evidence);
    }
}
