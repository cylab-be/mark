package mark.detection;

import java.util.Random;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import mark.core.Subject;

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
        Evidence<T> evidence = new Evidence<T>();
        evidence.label = profile.label;
        evidence.subject = subject;
        evidence.report = "Some report...";
        evidence.score = rand.nextDouble();
        evidence.time = data[data.length - 1].time;
        datastore.addEvidence(evidence);

        evidence.score = rand.nextDouble();
        datastore.addEvidence(evidence);
    }
}
