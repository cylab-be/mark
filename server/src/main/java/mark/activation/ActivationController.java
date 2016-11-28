package mark.activation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import mark.client.Client;
import mark.core.Evidence;
import mark.core.RawData;
import mark.server.AnalysisUnit;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;

/**
 *
 * @author Thibault Debatty
 */
public class ActivationController {
    /**
     * Time to wait for the Ignite framework to start (in ms).
     */
    private static final int STARTUP_DELAY = 3000;

    private Iterable<DetectionAgentProfile> profiles;
    private int task_count;

    /**
     * The address on which the server is bound. Will be provided to every
     * analysis task, as they will need it!
     */
    private URL server_url;
    private ExecutorService executor_service;
    private final Map<String, HashSet<AnalysisUnit>> events;

    public ActivationController() {
        events = Collections.synchronizedMap(new HashMap<String, HashSet<AnalysisUnit>>());
    }



    public void start() {

        // Start Ignite framework..
        Ignite ignite;
        if (Ignition.state() == IgniteState.STARTED) {
            ignite = Ignition.ignite();
        } else {
            ignite = Ignition.start();
        }
        executor_service = ignite.executorService();

        // Wait for Ignite to start...
        try {
            Thread.sleep(STARTUP_DELAY);
        } catch (InterruptedException ex) {
            // Something is trying to stop this thread
            // TODO: handle this correctly

        }

        // Start the scheduled activation task
        Timer timer = new Timer();
        timer.schedule(new ScheduledTask(), 1000, 1000);


    }

    class ScheduledTask extends TimerTask {

        @Override
        public void run() {

            // Clone the list of events and clear
            HashSet<Map.Entry<String, HashSet<AnalysisUnit>>> local_events = new HashSet<Map.Entry<String, HashSet<AnalysisUnit>>>(events.entrySet());
            events.clear();

            for (Map.Entry<String, HashSet<AnalysisUnit>> entry : local_events) {
                String label = entry.getKey();

                for (DetectionAgentProfile profile : profiles) {
                    if (profile.match(label)) {
                        for (AnalysisUnit link : entry.getValue()) {
                            try {
                                DetectionAgentInterface new_task = profile.getTaskFor(link);
                                new_task.setDatastore(new Client(server_url));
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


        }
    }

    public final void setServerAddress(String server_address)
            throws MalformedURLException {
        this.server_url = new URL(server_address);
    }

    /**
     * Get the total number of detection tasks that were activated.
     * @return
     */
    public final int getTaskCount() {
        return task_count;
    }

    /**
     * Set the activation profiles to be used by the ActivationController.
     * @param profiles
     * @throws InvalidProfileException if the profiles are not correct
     */
    public final void setProfiles(final Iterable<DetectionAgentProfile> profiles)
            throws InvalidProfileException {

        testProfiles(profiles);
        this.profiles = profiles;
    }

    /**
     * Test the profiles: instantiate (without running) one of each task defined
     * in the profiles.
     * @param profiles
     * @throws InvalidProfileException if one of the profiles is corrupted
     */
    protected final void testProfiles(
            final Iterable<DetectionAgentProfile> profiles)
            throws InvalidProfileException {

        for (DetectionAgentProfile profile : profiles) {
            try {
                DetectionAgentInterface new_task = profile.getTaskFor(new AnalysisUnit("1.2.3.4", "www.google.be"));

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
    public final void notifyRawData(final RawData data) {

        HashSet<AnalysisUnit> set = events.get(data.label);

        if (set == null) {
            set = new HashSet<AnalysisUnit>();
            events.put(data.label, set);
        }

        set.add(new AnalysisUnit(data.client, data.server));
    }

    /**
     * Wait for all pending analysis tasks to finish.
     */
    public final void awaitTermination() {
        try {
            executor_service.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            System.err.println("Activation controller was interrupted!");
        }
    }
}
