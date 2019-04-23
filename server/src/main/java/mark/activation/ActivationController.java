package mark.activation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import mark.core.DetectionAgentProfile;
import java.net.MalformedURLException;
import java.util.Arrays;
import mark.core.InvalidProfileException;
import mark.core.DetectionAgentInterface;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
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
import org.apache.ignite.cluster.ClusterMetrics;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.collision.fifoqueue.FifoQueueCollisionSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The activation controller uses the micro batching principle:
 * https://streaml.io/resources/tutorials/concepts/understanding-batch-microbatch-streaming
 * Events are continuously collected (with notifyRawData and notifyEvidence). In
 * a separate thread, every few secondes (defined by Config.update_interval),
 * analysis jobs are triggered. These jobs are executed by an Apache Ignite
 * Compute Grid:
 * https://apacheignite.readme.io/docs/compute-grid#section-ignitecompute
 *
 * @author Thibault Debatty
 */
@Singleton
public class ActivationController<T extends Subject> extends SafeThread implements ActivationControllerInterface<T> {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(ActivationController.class);

    private final LinkedList<DetectionAgentProfile> profiles;
    private final Ignite ignite;
    private final ExecutorService executor_service;

    // events is a table of label => subjects
    private volatile Map<String, Set<T>> events;
    private final Config config;

    /**
     *
     * @param config
     * @throws InvalidProfileException
     */
    @Inject
    public ActivationController(final Config config)
            throws InvalidProfileException {
        this.config = config;
        this.profiles = new LinkedList<>();

        IgniteConfiguration ignite_config = new IgniteConfiguration();
        ignite_config.setPeerClassLoadingEnabled(true);
        ignite_config.setClientMode(!config.ignite_start_server);

        ignite_config.setCollisionSpi(new FifoQueueCollisionSpi());
        
        // Changing total RAM size to be used by Ignite Node.
        DataStorageConfiguration storage_config =
                new DataStorageConfiguration();
        // Setting the size of the default memory region to 
        storage_config.getDefaultDataRegionConfiguration().setMaxSize(
            12L * 1024 * 1024 * 1024);
        ignite_config.setDataStorageConfiguration(storage_config);

        if (!config.ignite_autodiscovery) {
            // Disable autodiscovery
            TcpDiscoverySpi spi = new TcpDiscoverySpi();
            TcpDiscoveryVmIpFinder ip_finder = new TcpDiscoveryVmIpFinder();
            ip_finder.setAddresses(Arrays.asList("127.0.0.1"));
            spi.setIpFinder(ip_finder);
            ignite_config.setDiscoverySpi(spi);
        }

        // Start Ignite framework..
        if (Ignition.state() == IgniteState.STARTED) {
            ignite = Ignition.ignite();
        } else {
            ignite = Ignition.start(ignite_config);
        }

        executor_service = ignite.executorService();
    }

    /**
     * Ask ignite executor to shutdown then when for tasks to finish.
     *
     * @throws InterruptedException
     */
    public final void awaitTermination() throws InterruptedException {
        Thread.sleep(2 * 1000 * config.update_interval);
        executor_service.shutdown();
        executor_service.awaitTermination(1, TimeUnit.DAYS);
    }

    @Override
    public final void doRun() throws Throwable {

        Map<String, Set<T>> local_events;
        this.events = new HashMap<>();
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

            // Keep track of triggered detectors, to avoid triggering
            // same detector multiple times
            Map<String, Set<T>> triggered_detectors = new HashMap<>();

            // process the events:
            // for each received label find the agents that must be triggered
            // then spawn one agent for each subject
            for (Map.Entry<String, Set<T>> entry : local_events.entrySet()) {
                String label = entry.getKey();

                for (DetectionAgentProfile profile : profiles) {
                    if (!profile.match(label)) {
                        continue;
                    }

                    for (T subject : entry.getValue()) {
                        String detector_label = profile.label;
                        Set<T> triggered_subjects = triggered_detectors.get(
                                detector_label);
                        if (triggered_subjects == null) {
                            triggered_subjects = new HashSet<>();
                            triggered_detectors.put(
                                    detector_label, triggered_subjects);
                        }

                        if (triggered_subjects.contains(subject)) {
                            continue;
                        }

                        triggered_subjects.add(subject);

                        try {
                            LOGGER.debug(
                                    "Trigger detector {} for subject {}",
                                    detector_label,
                                    subject.toString());
                            executor_service.submit(
                                    new DetectionAgentContainer(
                                            subject,
                                            config.getDatastoreUrl(),
                                            config.getSubjectAdapter(),
                                            label,
                                            profile,
                                            profile.createInstance()));

                        } catch (MalformedURLException
                                | InvalidProfileException ex) {
                            LOGGER.error(
                                    "Cannot start agent "
                                    + profile.class_name,
                                    ex);
                        }
                    }
                }
            }

            LOGGER.debug("Executed " + getTaskCount() + " tasks");
        }

    }

    /**
     * Get the total number of detection tasks that were activated.
     *
     * @return
     */
    public final int getTaskCount() {
        return ignite.cluster().metrics().getTotalExecutedJobs();
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

        Set<T> set = events.get(data.label);

        if (set == null) {
            set = new HashSet<>();
            events.put(data.label, set);
        }

        set.add(data.subject);
    }

    /**
     *
     * @param evidence
     */
    @Override
    public final synchronized void notifyEvidence(final Evidence<T> evidence) {
        Set<T> set = events.get(evidence.label);

        if (set == null) {
            set = new HashSet<>();
            events.put(evidence.label, set);
        }

        set.add(evidence.subject);
    }

    public ClusterMetrics getIgniteMetrics() {
        ignite.cluster().nodes().iterator().next().addresses();
        ignite.cluster().nodes().iterator().next().hostNames();
        return ignite.cluster().metrics();
    }

}
