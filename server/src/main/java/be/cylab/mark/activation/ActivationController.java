package be.cylab.mark.activation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.InvalidProfileException;
import be.cylab.mark.core.DetectionAgentInterface;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import be.cylab.mark.core.Subject;
import be.cylab.mark.server.Config;
import be.cylab.mark.server.SafeThread;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The activation controller uses the micro batching principle:
 * https://streaml.io/resources/tutorials/concepts/understanding-batch-microbatch-streaming
 * Events are continuously collected (with notifyRawData and notifyEvidence).In
 a separate thread, every few secondes (defined by Config.update_interval),
 analysis jobs are triggered. These jobs are executed by an Apache Ignite
 Compute Grid:
 https://apacheignite.readme.io/docs/compute-grid#section-ignitecompute
 *
 * @author Thibault Debatty
 * @param <T>
 */
@Singleton
public class ActivationController<T extends Subject> extends SafeThread
                                implements ActivationControllerInterface<T> {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(ActivationController.class);

    private final LinkedList<DetectionAgentProfile> profiles;
    private final ExecutorInterface executor;

    // events is a table of label => subjects => last timestamp
    private volatile Map<String, Map<T, Long>> events;
    private final Config config;

    /**
     *
     * @param config
     * @param executor
     * @throws InvalidProfileException
     */
    @Inject
    public ActivationController(
            final Config config, final ExecutorInterface executor)
            throws InvalidProfileException {
        this.config = config;
        this.profiles = new LinkedList<>();
        this.executor = executor;
        this.events = new HashMap<>();
    }

    /**
     * Ask executor to shutdown then wait for tasks to finish.
     *
     * @throws InterruptedException
     */
    public final void awaitTermination() throws InterruptedException {
        this.executor.shutdown();
    }

    @Override
    public final void doRun() throws Throwable {

        Map<String, Map<T, Long>> local_events;

        while (true) {
            Thread.sleep(1000 * config.update_interval);

            if (isInterrupted()) {
                return;
            }

            // Clone the list of events and clear
            synchronized (this) {
                local_events = this.events;
                this.events = new HashMap<>();
            }

            // process the events:
            // for each received label find the agents that must be triggered
            // then spawn one agent for each subject
            for (String event_label : local_events.keySet()) {

                for (DetectionAgentProfile profile : profiles) {
                    if (! this.checkLabelsMatch(
                            profile.getTriggerLabel(), event_label)) {
                        continue;
                    }

                    for (Map.Entry<T, Long> subject_time :
                            local_events.get(event_label).entrySet()) {

                        T subject = (T) subject_time.getKey();
                        long timestamp = subject_time.getValue();
                        String detector_label = profile.getLabel();

                        try {
                            LOGGER.debug(
                                    "Trigger detector {} for subject {}",
                                    detector_label,
                                    subject.toString());
                            executor.submit(
                                    new DetectionAgentContainer(
                                            subject,
                                            timestamp,
                                            config.getDatastoreUrl(),
                                            config.getSubjectAdapter(),
                                            event_label,
                                            profile,
                                            profile.createInstance()));

                        } catch (MalformedURLException
                                | InvalidProfileException ex) {
                            LOGGER.error(
                                    "Cannot start agent "
                                    + profile.getClassName(),
                                    ex);
                        }
                    }
                }
            }

            // LOGGER.debug("Executed " + getTaskCount() + " tasks");
        }
    }

    /**
     * Check if the trigger label defined in a detection profile matches the
     * label of an event (and hence wether we should run this detector).
     *
     * Uses pattern matching...
     *
     * @param trigger_label
     * @param event_label
     * @return
     */
    boolean checkLabelsMatch(
            final String trigger_label, final String event_label) {

        return Pattern.compile(trigger_label).matcher(event_label).find();
    }

    /**
     * Get the number of executed jobs.
     * @return
     */
    @Override
    public int getTaskCount() {
        return executor.taskCount();
    }


    /**
     * Test the profiles: instantiate (without running) one of each task defined
     * in the profiles.
     *
     * @throws InvalidProfileException if one of the profiles is corrupted
     */
    public final void testProfiles()
            throws InvalidProfileException {

        for (DetectionAgentProfile profile : profiles) {
            try {
                DetectionAgentInterface new_task = profile.createInstance();

            } catch (IllegalArgumentException
                    | SecurityException ex) {
                throw new InvalidProfileException(
                        "Invalid profile: " + profile.toString()
                        + " : " + ex.getMessage(), ex);
            }
        }
    }

    /**
     *
     * @return
     */
    @Override
    public final Iterable<DetectionAgentProfile> getProfiles() {
        return profiles;
    }

    /**
     *
     * @param profile
     */
    public final void addAgent(final DetectionAgentProfile profile) {
        profiles.add(profile);
    }

    /**
     * Trigger required tasks for this new RawData.
     *
     * @param data
     */
    @Override
    public final synchronized void notifyRawData(final RawData<T> data) {

        Map<T, Long> hashmap = events.get(data.getLabel());
        if (hashmap == null) {
            hashmap = new HashMap<>();
            events.put(data.getLabel(), hashmap);
            hashmap.put(data.getSubject(), data.getTime());
        }

        if (hashmap.get(data.getSubject()) == null) {
            hashmap.put(data.getSubject(), data.getTime());
        } else if (hashmap.get(data.getSubject()) < data.getTime()) {
            hashmap.replace(data.getSubject(), data.getTime());
        }

    }

    /**
     *
     * @param evidence
     */
    @Override
    public final synchronized void notifyEvidence(final Evidence<T> evidence) {
        Map<T, Long> hashmap = events.get(evidence.getLabel());

        if (hashmap == null) {
            hashmap = new HashMap<>();
            events.put(evidence.getLabel(), hashmap);
            hashmap.put(evidence.getSubject(), evidence.getTime());
        }

        if (hashmap.get(evidence.getSubject()) == null) {
            hashmap.put(evidence.getSubject(), evidence.getTime());
        } else if (hashmap.get(evidence.getSubject()) < evidence.getTime()) {
            hashmap.replace(evidence.getSubject(), evidence.getTime());
        }


    }

    /**
     * Get the list of received events (new data or new evidence reports).
     * Used mainly for testing.
     * @return
     */
    Map<String, Map<T, Long>> getEvents() {
        return this.events;
    }
}
