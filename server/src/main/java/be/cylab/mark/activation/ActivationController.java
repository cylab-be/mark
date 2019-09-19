package be.cylab.mark.activation;

import be.cylab.mark.core.Event;
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
import java.util.List;
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

    // events are stored in a table of label => subjects => Event
    // to allow fast lookup
    private volatile Map<String, Map<T, Event<T>>> events;
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
     * Trigger required tasks for this new RawData.
     *
     * @param data
     */
    @Override
    public final void notifyRawData(final RawData<T> data) {

        this.addEvent(
                new Event(
                        data.getLabel(),
                        data.getSubject(),
                        data.getTime()));
    }

    /**
     *
     * @param evidence
     */
    @Override
    public final void notifyEvidence(final Evidence<T> evidence) {

        this.addEvent(
                new Event(
                        evidence.getLabel(),
                        evidence.getSubject(),
                        evidence.getTime()));
    }

    /**
     * Add this event to the tree of events (if required).
     * @param new_event
     */
    final synchronized void addEvent(Event<T> new_event) {

        // all subjects that have an event with this label
        Map<T, Event<T>> subjects = events.get(new_event.getLabel());

        if (subjects == null) {
            subjects = new HashMap<>();
            events.put(new_event.getLabel(), subjects);
        }

        Event saved_event = subjects.get(new_event.getSubject());

        // until now there was no such event (label) for this subject
        if (saved_event == null) {
            subjects.put(new_event.getSubject(), new_event);
            return;
        }

        // the new event is more recent then the one we have
        if (saved_event.getTimestamp() < new_event.getTimestamp()) {
            subjects.replace(new_event.getSubject(), new_event);
        }
    }

    @Override
    public final void doRun() throws Throwable {

        Map<String, Map<T, Event<T>>> copy_of_events;

        while (true) {
            Thread.sleep(1000 * config.update_interval);

            if (isInterrupted()) {
                return;
            }

            // Clone the list of events and clear
            synchronized (this) {
                copy_of_events = this.events;
                this.events = new HashMap<>();
            }

            this.processEvents(copy_of_events);
        }
    }

    /**
     * Process the events: for each received label find the agents that must be
     * triggered then spawn one agent for each subject.
     *
     * @param events
     */
    private void processEvents(final Map<String, Map<T, Event<T>>> events) {


        int scheduled = 0;
        for (String event_label : events.keySet()) {

            for (DetectionAgentProfile profile : profiles) {
                if (!this.checkLabelsMatch(
                        profile.getTriggerLabel(), event_label)) {
                    continue;
                }

                for (Event<T> event : events.get(event_label).values()) {
                    this.scheduleDetection(profile, event);
                    scheduled++;
                }
            }
        }

        LOGGER.info("Scheduled " + scheduled + " detectors...");
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
     * Start the detection algorithm described in this profile for this subject.
     * @param profile
     * @param subject_time
     * @param event_label
     */
    private void scheduleDetection(
            final DetectionAgentProfile profile,
            final Event<T> event) {

        try {
            LOGGER.debug(
                    "Trigger detector {} for {}",
                    profile.getClassName(),
                    event.getSubject().toString());
            executor.submit(
                    new DetectionAgentContainer<>(
                            event,
                            config.getDatastoreUrl(),
                            config.getSubjectAdapter(),
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

    /**
     * Ask executor to shutdown then wait for tasks to finish.
     *
     * @throws InterruptedException
     */
    public final void awaitTermination() throws InterruptedException {
        this.executor.shutdown();
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
    public final List<DetectionAgentProfile> getProfiles() {
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
     * Get the list of received events (new data or new evidence reports).
     * Used mainly for testing.
     * @return
     */
    Map<String, Map<T, Event<T>>> getEvents() {
        return this.events;
    }

    @Override
    public Map<String, Object> getExecutorStatus() {
        return this.executor.getStatus();
    }
}
