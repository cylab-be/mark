package be.cylab.mark.example;

import be.cylab.mark.core.ClientWrapperInterface;
import java.util.Random;
import be.cylab.mark.core.DetectionAgentInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;

/**
 * Dummy detection agent that reads some data from datastore and writes
 * one evidence.
 * @author Thibault Debatty
 */
public class ExampleDetector implements DetectionAgentInterface {


    @Override
    public final void analyze(
            final Event ev,
            final DetectionAgentProfile profile,
            final ClientWrapperInterface datastore) throws Throwable {

        long now = System.currentTimeMillis();
        long since = now - 1000 * 300;

        RawData[] data = datastore.findRawData(
                ev.getLabel(),
                ev.getSubject(),
                since, now);

        // Process data
        Random rand = new Random();

        // Add evidences to datastore
        Evidence evidence = new Evidence();
        evidence.setLabel(profile.getLabel());
        evidence.setSubject(ev.getSubject());
        evidence.setReport("Found " + data.length + " data records with label "
            + ev.getLabel());
        evidence.setScore(rand.nextDouble());
        evidence.setTime(ev.getTimestamp());
        datastore.addEvidence(evidence);
    }
}
