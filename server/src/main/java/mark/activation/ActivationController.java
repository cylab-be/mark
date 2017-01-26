package mark.activation;

import java.net.MalformedURLException;
import mark.server.InvalidProfileException;
import mark.detection.DetectionAgentInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.Subject;
import mark.server.Config;
import mark.server.SafeThread;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thibault Debatty
 */
public class ActivationController<T extends Subject> extends SafeThread {

    private static final int ACTIVATION_INTERVAL = 1000;
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ActivationController.class);

    private final LinkedList<DetectionAgentProfile> profiles;
    private final Ignite ignite;
    private final ExecutorService executor_service;

    // events is a table of label => subjects
    private final Map<String, HashSet<T>> events;
    private final Config config;

    /**
     *
     * @param config
     * @throws InvalidProfileException
     */
    public ActivationController(final Config config)
            throws InvalidProfileException {

        this.config = config;
        this.profiles = new LinkedList<DetectionAgentProfile>();

        // Synchronized map has poor performance! should be replaced by
        // a concurrent hashmpap...
        this.events = Collections.synchronizedMap(
                new HashMap<String, HashSet<T>>());

        testProfiles();

        // Start Ignite framework..
        if (Ignition.state() == IgniteState.STARTED) {
            ignite = Ignition.ignite();
        } else {
            ignite = Ignition.start();
        }

        executor_service = ignite.executorService();
    }

    /**
     * Ask ignite executor to shutdown then when for tasks to finish.
     * @throws InterruptedException
     */
    public final void awaitTermination() throws InterruptedException {
        while (executor_service == null) {
            Thread.sleep(ACTIVATION_INTERVAL);
        }

        executor_service.shutdown();
        executor_service.awaitTermination(1, TimeUnit.DAYS);
    }

    @Override
    public final void doRun() throws Throwable {

        while (true) {
            Thread.sleep(ACTIVATION_INTERVAL);

            if (isInterrupted()) {
                return;
            }

            // Clone the list of events and clear
            HashSet<Map.Entry<String, HashSet<T>>> local_events =
                    new HashSet<Map.Entry<String, HashSet<T>>>(
                            events.entrySet());
            events.clear();

            // process the events:
            // for each received label find the agents that must be triggered
            // then spawn one agent for each subject
            for (Map.Entry<String, HashSet<T>> entry : local_events) {
                String label = entry.getKey();

                for (DetectionAgentProfile profile : profiles) {
                    if (profile.match(label)) {
                        for (T link : entry.getValue()) {
                            try {
                                DetectionAgentInterface new_task =
                                        profile.getTaskFor(link);
                                new_task.setDatastoreUrl(
                                        config.getDatastoreUrl());
                                new_task.setSubjectAdapter(
                                        config.getSubjectAdapter());
                                executor_service.submit(new_task);

                            } catch (ClassNotFoundException ex) {
                                LOGGER.error(
                                        "Cannot start agent "
                                                + profile.class_name,
                                        ex);
                            } catch (IllegalAccessException ex) {
                                LOGGER.error(
                                        "Cannot start agent "
                                                + profile.class_name,
                                        ex);
                            } catch (InstantiationException ex) {
                                LOGGER.error(
                                        "Cannot start agent "
                                                + profile.class_name,
                                        ex);
                            } catch (MalformedURLException ex) {
                                LOGGER.error(
                                        "Cannot start agent "
                                                + profile.class_name,
                                        ex);
                            } catch (InvalidProfileException ex) {
                                LOGGER.error(
                                        "Cannot start agent "
                                                + profile.class_name,
                                        ex);
                            }
                        }
                    }
                }
            }

            LOGGER.debug("Executed " + getTaskCount() + " tasks");
        }

    }

    /**
     * Get the total number of detection tasks that were activated.
     * @return
     */
    public final int getTaskCount() {
        return ignite.cluster().metrics().getTotalExecutedJobs();
    }

    /**
     * Test the profiles: instantiate (without running) one of each task defined
     * in the profiles.
     * @param profiles
     * @throws InvalidProfileException if one of the profiles is corrupted
     */
    private void testProfiles()
            throws InvalidProfileException {

        for (DetectionAgentProfile profile : profiles) {
            try {
                DetectionAgentInterface new_task = profile.getTaskFor(
                        config.getSubjectAdapter().getInstance());

            } catch (ClassNotFoundException ex) {
                throw new InvalidProfileException(
                        "Invalid profile: " + profile.toString()
                        + " : " + ex.getMessage(), ex);
            } catch (IllegalAccessException ex) {
                throw new InvalidProfileException(
                        "Invalid profile: " + profile.toString()
                                + " : " + ex.getMessage(), ex);
            } catch (IllegalArgumentException ex) {
                throw new InvalidProfileException(
                        "Invalid profile: " + profile.toString()
                                + " : " + ex.getMessage(), ex);
            } catch (InstantiationException ex) {
                throw new InvalidProfileException(
                        "Invalid profile: " + profile.toString()
                                + " : " + ex.getMessage(), ex);
            } catch (SecurityException ex) {
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
     * @param data
     */
    public final void notifyRawData(final RawData<T> data) {

        HashSet<T> set = events.get(data.label);

        if (set == null) {
            set = new HashSet<T>();
            events.put(data.label, set);
        }

        set.add(data.subject);
    }

    /**
     *
     * @param evidence
     */
    public final void notifyEvidence(final Evidence<T> evidence) {
        HashSet<T> set = events.get(evidence.label);

        if (set == null) {
            set = new HashSet<T>();
            events.put(evidence.label, set);
        }

        set.add(evidence.subject);
    }
}
