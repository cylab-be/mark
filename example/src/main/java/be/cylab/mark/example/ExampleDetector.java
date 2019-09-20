package be.cylab.mark.example;

import java.util.Random;
import be.cylab.mark.core.DetectionAgentInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
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
public class ExampleDetector<T extends Subject>
        implements DetectionAgentInterface<T> {


    @Override
    public final void analyze(
            final Event<T> ev,
            final DetectionAgentProfile profile,
            final ServerInterface<T> datastore) throws Throwable {

        RawData[] data = datastore.findRawData(
                ev.getLabel(),
                ev.getSubject());

        if (data.length < 1) {
            return;
        }

        // Process data
        Random rand = new Random();

        // Add evidences to datastore
        Evidence<T> evidence = new Evidence<>();
        evidence.setLabel(profile.getLabel());
        evidence.setSubject(ev.getSubject());
        evidence.setReport("Some report...");
        evidence.setScore(rand.nextDouble());
        evidence.setTime(data[data.length - 1].getTime());
        datastore.addEvidence(evidence);
    }
}