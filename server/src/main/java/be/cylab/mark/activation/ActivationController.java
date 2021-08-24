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
import be.cylab.mark.server.Config;
import be.cylab.mark.server.SafeThread;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The activation controller uses the micro batching principle:
 * https://streaml.io/resources/tutorials/concepts/understanding-batch-
 * microbatch-streaming
 * Events are continuously collected (with notifyRawData and notifyEvidence).In
 a separate thread, every few secondes (defined by Config.update_interval),
 analysis jobs are triggered. These jobs are executed by an Apache Ignite
 Compute Grid:
 https://apacheignite.readme.io/docs/compute-grid#section-ignitecompute
 *
 * @author Thibault Debatty
 */
@Singleton
public final class ActivationController extends SafeThread
                                implements ActivationControllerInterface {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(ActivationController.class);

    private final HashMap<String, DetectionAgentProfile> profiles =
            new HashMap<>();
    private final ExecutorInterface executor;

    // store the last time an agent has been triggered for a specific Subject,
    // to be able to handle if it needs to be triggered at specific intervals
    private final Map<String, Long> last_time_triggered;

    // events are stored in a table of label => subjects => Event
    // to allow fast lookup
    private volatile Map<String, Map<Map, Event>> events;
    private final Config config;

    private volatile boolean running = true;

    /**
     *
     * @param config
     * @param executor
     */
    @Inject
    public ActivationController(
            final Config config, final ExecutorInterface executor) {

        this.config = config;
        this.executor = executor;
        this.events = new HashMap<>();
        this.last_time_triggered = new HashMap<>();
    }

    /**
     * Trigger required tasks for this new RawData.
     *
     * @param data
     */
    @Override
    public void notifyRawData(final RawData data) {

        this.addEvent(
                new Event(
                        data.getLabel(),
                        data.getSubject(),
                        data.getTime(),
                        data.getId()));
    }

    /**
     *
     * @param evidence
     */
    @Override
    public void notifyEvidence(final Evidence evidence) {

        this.addEvent(
                new Event(
                        evidence.getLabel(),
                        evidence.getSubject(),
                        evidence.getTime(),
                        evidence.getId()));
    }

    /**
     * Add this event to the tree of events (if required).
     * @param new_event
     */
    synchronized void addEvent(final Event new_event) {

        // all subjects that have an event with this label
        Map<Map, Event> subjects = events.get(new_event.getLabel());

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
    public void doRun() throws Throwable {

        Map<String, Map<Map, Event>> copy_of_events;

        while (true) {
            Thread.sleep(1000 * config.getUpdateInterval());

            if (!this.running) {
                continue;
            }

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
    private void processEvents(final Map<String, Map<Map, Event>> events) {

        int scheduled = 0;
        int events_count = 0;

        for (String event_label : events.keySet()) {

            for (DetectionAgentProfile profile : profiles.values()) {
                if (!this.checkLabelsMatch(
                        profile.getTriggerLabel(), event_label)) {
                    continue;
                }

                for (Event event : events.get(event_label).values()) {

                    events_count++;
                    if (!checkTriggerInterval(profile, event)) {
                        LOGGER.debug(
                                "Skip {} for {} because of trigger interval",
                                profile.getLabel(),
                                event.getSubject().toString());
                        continue;
                    }

                    updateLastTimeTriggered(profile, event);
                    this.scheduleDetection(profile, event);
                    scheduled++;

                }
            }
        }
        LOGGER.info("Processed " + events_count + " events against "
                + profiles.size() + " profiles and scheduled " + scheduled
                + " detectors ...");
    }

    /**
     * Check if the trigger label defined in a detection profile matches the
     * label of an event (and hence wether we should run this detector).
     *
     * Uses pattern matching...
     *
     * @param trigger_label
     * @param event_label
     * @return true if event_label matches trigger_label
     */
    boolean checkLabelsMatch(
            final String trigger_label, final String event_label) {

        return Pattern.compile(trigger_label).matcher(event_label).find();
    }

    /**
     * Check if the time between the current triggered event and the last
     * triggered event for a given detection agent is long enough.
     * @param profile
     * @param event
     * @return true if the profile should be triggered (delay was long enough)
     */
    boolean checkTriggerInterval(
            final DetectionAgentProfile profile, final Event event) {

        String key = profile.getClassName() + "-"
                            + event.getSubject().toString();

        if (!last_time_triggered.containsKey(key)) {
            return true;
        }

        long delay = System.currentTimeMillis() - last_time_triggered.get(key);
        // getTriggerInterval is expressed in seconds
        return delay > 1000 * profile.getTriggerInterval();
    }

    /**
     * Updates the LastTimeTriggered map with the new timestamp for the specific
     * Detection Agent-Subject pair.
     * @param profile
     * @param event
     */
    private void updateLastTimeTriggered(
            final DetectionAgentProfile profile, final Event event) {

        String key = profile.getClassName() + "-"
                            + event.getSubject().toString();
        last_time_triggered.put(key, System.currentTimeMillis());
    }

    /**
     * Start the detection algorithm described in this profile for this subject.
     * @param profile
     * @param subject_time
     * @param event_label
     */
    private void scheduleDetection(
            final DetectionAgentProfile profile,
            final Event event) {

        try {
            LOGGER.debug(
                    "Trigger detector {} for {}",
                    profile.getClassName(),
                    event.getSubject().toString());


            DetectionAgentConfig agent_config =
                    DetectionAgentConfig.fromConfig(config);

            executor.submit(
                    new DetectionAgentContainer(
                            event,
                            agent_config,
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
     * @throws InterruptedException if our thread was killed while
     * we were stopping...
     */
    public void awaitTermination() throws InterruptedException {
        this.executor.shutdown();
    }


    /**
     * Test the profiles: instantiate (without running) one of each task defined
     * in the profiles.
     *
     * @throws InvalidProfileException if one of the profiles is corrupted
     */
    public void testProfiles()
            throws InvalidProfileException {

        for (DetectionAgentProfile profile : profiles.values()) {
            try {
                DetectionAgentInterface new_task = profile.createInstance();
                LOGGER.debug(new_task.toString());

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
    public List<DetectionAgentProfile> getProfiles() {
        return new LinkedList<>(profiles.values());
    }

    /**
     * Add the profile for a new detector.
     * This method can be used during execution, to dynamically add detectors.
     * @param profile
     */
    @Override
    public void setAgentProfile(final DetectionAgentProfile profile) {
        this.profiles.put(profile.getLabel(), profile);
    }


    /**
     * Get the list of received events (new data or new evidence reports).
     * Used mainly for testing.
     * @return
     */
    Map<String, Map<Map, Event>> getEvents() {
        return this.events;
    }

    /**
     * Get the map of lastTriggeredAgents.
     * Used for testing.
     * @return
     */
    Map<String, Long> getLastTimeTriggered() {
        return this.last_time_triggered;
    }

    @Override
    public Map<String, Object> getExecutorStatus() {
        return this.executor.getStatus();
    }

    @Override
    public void pauseExecution() {
        LOGGER.info("Pause ...");
        synchronized (this) {
            this.running = false;
        }
    }

    @Override
    public void resumeExecution() {
        LOGGER.info("Resume ...");
        synchronized (this) {
            this.running = true;
        }
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void reload() {
        File modules_dir;
        try {
            modules_dir = config.getModulesDirectory();
        } catch (FileNotFoundException ex) {
            LOGGER.warn(ex.getMessage());
            LOGGER.warn("Skipping modules parsing ...");
            return;
        }


        // Parse *.detection.yml files
        File[] detection_agent_files
                = modules_dir.listFiles(
                        (final File dir, final String name) ->
                                name.endsWith(".detection.yml"));

        profiles.clear();
        for (File file : detection_agent_files) {
            try {
                setAgentProfile(DetectionAgentProfile.fromFile(file));
            } catch (FileNotFoundException ex) {
                LOGGER.warn("File does not exist anymore: "
                        + file.getAbsolutePath());
            }
        }
        LOGGER.info("Found " + profiles.size() + " detection agents ...");
    }
}
