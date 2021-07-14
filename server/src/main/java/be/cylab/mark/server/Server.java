package be.cylab.mark.server;

import com.google.inject.Inject;
import be.cylab.mark.datastore.Datastore;
import be.cylab.mark.core.DataAgentProfile;
import java.net.MalformedURLException;
import be.cylab.mark.activation.ActivationController;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.ServerInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 * Represents a MARK server. It is composed of:
 * <ul>
 * <li>a webserver</li>
 * <li>an activation controller</li>
 * <li>a datastore (json-rpc server)</li>
 * <li>a monitor threat to save status</li>
 * <li>optionally: some data agents</li>
 * </ul>
 *
 * @author Thibault Debatty
 */
public class Server {

    private static final org.slf4j.Logger LOGGER
            = LoggerFactory.getLogger(Server.class);

    private final Datastore datastore;
    private final DataSourcesController sources;
    private final ActivationController activation_controller;
    private final Config config;
    private final Thread monitor;

    private boolean stopping = false;

    /**
     * Initialize a server with default configuration, dummy subject adapter, no
     * data agents and no detection agents.
     *
     * @param activation_controller
     * @param sources
     * @param datastore
     * @param config
     * @throws java.lang.Throwable on any error
     */
    @Inject
    public Server(
            final ActivationController activation_controller,
            final DataSourcesController sources,
            final Datastore datastore,
            final Config config) throws Throwable {

        this.activation_controller = activation_controller;
        this.datastore = datastore;
        this.sources = sources;
        this.config = config;

        this.monitor = new Thread(new Monitor(datastore));
    }

    /**
     * Non-blocking start the datastore and data agents (sources) in separate
     * threads. This method returns when the server and agents are started. You
     * can use server.stop()
     *
     * @throws java.net.MalformedURLException if the URL specified by config is
     * invalid
     * @throws Exception if Jetty caused an exception
     */
    public final void start()
            throws MalformedURLException, Exception {

        sources.loadAgentsFromModulesDirectory();

        activation_controller.reload();
        activation_controller.start();
        datastore.start();
        monitor.start();
        sources.start();

        LOGGER.info("=======================================");
        LOGGER.info(" __  __          _____  _    ");
        LOGGER.info("|  \\/  |   /\\   |  __ \\| |   ");
        LOGGER.info("| \\  / |  /  \\  | |__) | | __");
        LOGGER.info("| |\\/| | / /\\ \\ |  _  /| |/ /");
        LOGGER.info("| |  | |/ ____ \\| | \\ \\|   < ");
        LOGGER.info("|_|  |_/_/    \\_\\_|  \\_\\_|\\_\\");
        LOGGER.info("");
        LOGGER.info(getClass().getPackage().getImplementationVersion());
        LOGGER.info("=======================================");
    }

    /**
     * Run the server in batch mode.
     * <ol>
     * <li>start the server</li>
     * <li>wait for data sources to complete</li>
     * <li>save resulting ranking lists</li>
     * <li>stop the server</li>
     * </ol>
     * @throws java.lang.Exception if Jetty caused an exception
     */
    public final void batch() throws Exception {
        start();

        sources.awaitTermination();
        LOGGER.info("All data sources completed!");

        // the stop method is already running in another thread
        // (probably the result of ctrl+c)
        if (stopping) {
            return;
        }
        stopping = true;

        LOGGER.info(
                "Wait for activation controller to finish running tasks...");
        activation_controller.awaitTermination();

        LOGGER.info("Ask activation controller to stop...");
        activation_controller.interrupt();
        activation_controller.join();


        LOGGER.info("Save rankings...");
        saveRankings();

        LOGGER.info("Ask monitor to stop...");
        monitor.interrupt();
        monitor.join();

        LOGGER.info("Ask datastore to stop...");
        datastore.stop();

        LOGGER.info("Server stopped!");
    }

    /**
     * Stop the data agents, wait for all detection agents to complete and
     * eventually stop the datastore.
     * @throws java.lang.Exception on any error
     */
    public final void stop() throws Exception {

        // already stopping in another thread
        // probably running in batch mode, and data sources have finished
        if (stopping) {
            return;
        }
        stopping = true;

        LOGGER.info("Stopping server...");
        LOGGER.info("Ask data agents to stop...");
        sources.stop();

        LOGGER.info(
                "Wait for activation controller to finish running tasks...");
        activation_controller.awaitTermination();

        LOGGER.info("Ask activation controller to stop...");
        activation_controller.interrupt();
        activation_controller.join();

        LOGGER.info("Ask monitor recorder to stop...");
        monitor.interrupt();
        monitor.join();

        LOGGER.info("Ask datastore to stop...");
        datastore.stop();

        LOGGER.info("Server stopped!");
    }

    /**
     * Allows to programmatically add a data agent to the server.
     *
     * Used for integration tests for example.
     *
     * @param profile
     */
    public final void addDataAgentProfile(final DataAgentProfile profile) {
        sources.add(profile);
    }

    private static final Charset ENCODING = StandardCharsets.UTF_8;

    private void saveRankings() {
        Map<String, List> rankings = new HashMap<>();
        ServerInterface rq = datastore.getRequestHandler();
        List<DetectionAgentProfile> profiles =
                activation_controller.getProfiles();
        for (DetectionAgentProfile profile : profiles) {
            String label = profile.getLabel();
            try {
                Evidence[] evidences = rq.findEvidence(label);
                rankings.put(label, Arrays.asList(evidences));
            } catch (Throwable ex) {
                LOGGER.error("Failed to get ranking for label", label);
            }
        }

        ObjectMapper mapper = new ObjectMapper();

        String json = "";
        try {
            json = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(rankings);
        } catch (JsonProcessingException ex) {
            LOGGER.warn("Failed to build json from rankings");
            return;
        }

        String rankings_file = config.getLogDirectory() + "/rankings.json";

        Path path = Paths.get(rankings_file);
        try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)) {
            writer.write(json);
        } catch (IOException ex) {
            LOGGER.warn("Failed to write ranking to " + rankings_file);
        }

        LOGGER.info("Rankings saved to " + rankings_file);
    }
}
