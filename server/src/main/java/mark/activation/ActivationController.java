package mark.activation;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import mark.client.Client;
import mark.core.RawData;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author Thibault Debatty
 */
public class ActivationController {
    private Iterable<ActivationProfile> profiles;
    private int task_count;

    /**
     * The address on which the server is bound. Will be provided to every
     * analysis task, as they will need it!
     */
    private URL server_url;

    /**
     * Time to wait for the Ignite framework to start (in ms).
     */
    private static final int STARTUP_DELAY = 3000;

    private final ExecutorService executor_service;

    /**
     *
     */
    public ActivationController() {

        // Load default activation profiles
        InputStream activation_file = getClass()
                .getResourceAsStream("/activation.yml");
        try {
            setProfiles(activation_file);

        } catch (FileNotFoundException ex) {
            System.err.println("Default activation file was not found!");
            System.err.println(ex.getMessage());
            System.err.println("This should never happen!");
            System.exit(1);

        } catch (InvalidProfileException ex) {
            System.err.println("Default activation file is corrupted!");
            System.err.println(ex.getMessage());
            System.err.println("This should never happen!");
            System.exit(1);
        }

        // Start Ignite framework..

        IgniteConfiguration conf = new IgniteConfiguration();
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
    public final void setProfiles(final Iterable<ActivationProfile> profiles)
            throws InvalidProfileException {

        testProfiles(profiles);
        this.profiles = profiles;
    }

    /**
     * Set the activation profiles to use from Yaml configuation file.
     * @param activation_file
     * @throws FileNotFoundException if Yaml configuration file was not found.
     * @throws InvalidProfileException if the profiles are not correct.
     */
    public final void setProfiles(final InputStream activation_file)
            throws FileNotFoundException, InvalidProfileException {
        setProfiles(parseActivationFile(activation_file));
    }

    /**
     * Test the profiles: instantiate (without running) one of each task defined
     * in the profiles.
     * @param profiles
     * @throws InvalidProfileException if one of the profiles is corrupted
     */
    protected final void testProfiles(
            final Iterable<ActivationProfile> profiles)
            throws InvalidProfileException {

        for (ActivationProfile profile : profiles) {
            try {
                DetectionAgentInterface new_task = (DetectionAgentInterface)
                        Class.forName(profile.class_name).newInstance();

                new_task.setType(profile.type);
                new_task.setClient("1.2.3.4");
                new_task.setServer("www.google.be");

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

    public final Iterable<ActivationProfile> getProfiles() {
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

    protected final List<ActivationProfile> parseActivationFile(
            final InputStream activation_file) throws FileNotFoundException {

        Yaml yaml = new Yaml(new Constructor(ActivationProfile.class));

        Iterable<Object> all = yaml.loadAll(activation_file);
        LinkedList<ActivationProfile> all_profiles =
                new LinkedList<ActivationProfile>();

        for (Object profile_object : all) {
            all_profiles.add((ActivationProfile) profile_object);
        }

        return all_profiles;
    }

    // Activation conditions accounting
    // Data count since last activation
    // Collection_DataType_ClientIP_ActivationProfile
    private static final String DATA_COUNTER_FORMAT = "%s_%s_%s_%s";
    private final HashMap<String, Counter> data_counters =
            new HashMap<String, Counter>();

    // Time of last activation
    // ClientIP_ActivationProfile
    private static final String ACTIVATION_TIME_FORMAT = "%s_%s";
    private final HashMap<String, Long> activation_times =
            new HashMap<String, Long>();

    /**
     * Trigger required tasks for this new RawData.
     * @param data
     */
    public final void notifyRawData(final RawData data) {

        updateCounters(data);

        List<DetectionAgentInterface> tasks = findTasks(
                Collection.RAW_DATA, data.type, data.client, data.server);

        for (Runnable task : tasks) {
            executor_service.submit(task);
            task_count++;
        }
    }

    /**
     * Find the tasks that have to be triggered, based on: collection (RAW_DATA
     * or EVIDENCE), type, client, server and internal counters and timers.
     * @param collection
     * @param type
     * @param client
     * @param server
     * @return
     */
    private List<DetectionAgentInterface> findTasks(
            final Collection collection,
            final String type,
            final String client,
            final String server) {

        LinkedList<DetectionAgentInterface> tasks =
                new LinkedList<DetectionAgentInterface>();

        for (ActivationProfile profile : profiles) {
            if (profile.collection != collection
                    || !profile.type.equals(type)) {
                continue;
            }

            // Check the data and time counter
            String counter_key = String.format(
                    DATA_COUNTER_FORMAT,
                    collection,
                    type,
                    client,
                    profile.toString());

            String time_key = String.format(
                    ACTIVATION_TIME_FORMAT,
                    client,
                    profile.toString()
            );


            long time_last_run = 0;
            if (activation_times.containsKey(time_key)) {
                time_last_run = activation_times.get(time_key);
            }

            int time_since_last_run =
                    (int) (System.currentTimeMillis()
                    - time_last_run);

            if (data_counters.get(counter_key).get() < profile.condition_count
                    && time_since_last_run <= profile.condition_time) {
                continue;
            }

            // OK, we have a task to start
            // Reset the data counter
            data_counters.put(counter_key, new Counter());
            activation_times.put(time_key, System.currentTimeMillis());

            // Create analysis task
            try {
                DetectionAgentInterface new_task =
                        (DetectionAgentInterface)
                        Class.forName(profile.class_name)
                        .newInstance();

                new_task.setClient(client);
                new_task.setServer(server);
                new_task.setType(type);
                new_task.setDatastore(new Client(server_url));

                tasks.add(new_task);

            } catch (ClassNotFoundException ex) {
                System.err.println("Oups :(");
            } catch (SecurityException ex) {
                System.err.println("Oups :(");
            } catch (InstantiationException ex) {
                System.err.println("Oups :(");
            } catch (IllegalAccessException ex) {
                System.err.println("Oups :(");
            } catch (IllegalArgumentException ex) {
                System.err.println("Oups :(");
            }
        }

        return tasks;

    }

    private void updateCounters(final RawData data) {

        // One counter for each activation profile concerned with this data
        for (ActivationProfile profile : profiles) {
            if (
                    profile.collection != Collection.RAW_DATA
                    || !profile.type.equals(data.type)) {

                continue;
            }

            String counter_key = String.format(
                    DATA_COUNTER_FORMAT,
                    Collection.RAW_DATA,
                    data.type,
                    data.client,
                    profile.toString());
            if (!data_counters.containsKey(counter_key)) {
                data_counters.put(counter_key, new Counter());
            }
            data_counters.get(counter_key).increment();
        }
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

/**
 *
 * @author Thibault Debatty
 */
class Counter {
    private int value = 0;

    /**
     * Increment the inner value of this counter.
     */
    public void increment() {
        value++;
    }

    /**
     * Get the inner value of this value.
     * @return
     */
    public int get() {
        return value;
    }
}
