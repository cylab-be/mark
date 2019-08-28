package be.cylab.mark.integration;

import java.util.Random;
import be.cylab.mark.core.DetectionAgentInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import be.cylab.mark.core.ServerInterface;
import be.cylab.mark.core.Subject;

/**
 * Dummy detection agent that reads some data from datastore and writes
 * two evidences.
 * @author Thibault Debatty
 * @param <T> The type of subject we deal with
 */
public class DummyReadWriteDetector<T extends Subject>
        implements DetectionAgentInterface<T> {


    @Override
    public final void analyze(
            final T subject,
            final long timestamp,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface<T> datastore) throws Throwable {

        RawData[] data = datastore.findRawData(actual_trigger_label, subject);

        if (data.length < 1) {
            return;
        }

        // Process data
        Random rand = new Random();

        // Add evidences to datastore
        Evidence<T> evidence = new Evidence<>();
        evidence.setLabel(profile.getLabel());
        evidence.setSubject(subject);
        evidence.setReport("Some report...");
        evidence.setScore(rand.nextDouble());
        evidence.setTime(data[data.length - 1].getTime());
        datastore.addEvidence(evidence);

        evidence.setScore(rand.nextDouble());
        datastore.addEvidence(evidence);
    }
}