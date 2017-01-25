package mark.activation;

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
    private final ExecutorService executor_service;
    private final Map<String, HashSet<T>> events;
    private final Config config;
    private int task_count;

    public ActivationController(Config config) throws InvalidProfileException {
        this.config = config;
        this.profiles = new LinkedList<DetectionAgentProfile>();
        this.events = Collections.synchronizedMap(new HashMap<String, HashSet<T>>());

        testProfiles();

        // Start Ignite framework..
        Ignite ignite;
        if (Ignition.state() == IgniteState.STARTED) {
            ignite = Ignition.ignite();
        } else {
            ignite = Ignition.start();
        }

        executor_service = ignite.executorService();
    }

    public void awaitTermination() throws InterruptedException {
        while (executor_service == null) {
            Thread.sleep(ACTIVATION_INTERVAL);
        }

        executor_service.shutdown();
        executor_service.awaitTermination(1, TimeUnit.DAYS);
    }

    @Override
    public void doRun() throws Throwable {

        while (true) {
            Thread.sleep(ACTIVATION_INTERVAL);

            if (isInterrupted()) {
                return;
            }

            // Clone the list of events and clear
            HashSet<Map.Entry<String, HashSet<T>>> local_events = new HashSet<Map.Entry<String, HashSet<T>>>(events.entrySet());
            events.clear();

            for (Map.Entry<String, HashSet<T>> entry : local_events) {
                String label = entry.getKey();

                for (DetectionAgentProfile profile : profiles) {
                    if (profile.match(label)) {
                        for (T link : entry.getValue()) {
                            try {
                                DetectionAgentInterface new_task = profile.getTaskFor(link);
                                new_task.setDatastoreUrl(config.getDatastoreUrl());
                                new_task.setSubjectAdapter(config.getSubjectAdapter());
                                // new Client<T>(server_url, adapter)
                                executor_service.submit(new_task);
                                task_count++;

                            } catch (Exception ex) {
                                System.err.println("Oups!");
                                System.err.println(ex.getMessage());
                            }
                        }
                    }
                }
            }

            LOGGER.debug("Executed " + task_count + " tasks");
        }

    }

    /**
     * Get the total number of detection tasks that were activated.
     * @return
     */
    public final int getTaskCount() {
        return task_count;
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

    public final Iterable<DetectionAgentProfile> getProfiles() {
        return profiles;
    }

    public void addAgent(DetectionAgentProfile profile) {
        profiles.add(profile);
    }

    /**
     * Available collections: RAW_DATA and EVIDENCE.
     */
    public enum Collection {

        /**
         * for RawData.
         */
        RAW_DATA,

        /**
         * For Evidence.
         */
        EVIDENCE
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

    public void notifyEvidence(Evidence<T> evidence) {
        HashSet<T> set = events.get(evidence.label);

        if (set == null) {
            set = new HashSet<T>();
            events.put(evidence.label, set);
        }

        set.add(evidence.subject);
    }
}
