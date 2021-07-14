package be.cylab.mark.server;

import com.google.inject.Inject;
import be.cylab.mark.datastore.Datastore;
import be.cylab.mark.core.DataAgentProfile;
import java.net.MalformedURLException;
import be.cylab.mark.activation.ActivationController;
import org.slf4j.LoggerFactory;

/**
 * Represents a MARK server. It is composed of: - a webserver - an activation
 * controller - a datastore (json-rpc server) - optionally: some data agents
 *
 * @author Thibault Debatty
 */
public class Server {

    private static final org.slf4j.Logger LOGGER
            = LoggerFactory.getLogger(Server.class);

    private final Datastore datastore;
    private final DataSourcesController sources;
    private final ActivationController activation_controller;
    private final Thread monitor;

    private boolean stopping = false;

    /**
     * Initialize a server with default configuration, dummy subject adapter, no
     * data agents and no detection agents.
     *
     * @param activation_controller
     * @param sources
     * @param datastore
     * @throws java.lang.Throwable on any error
     */
    @Inject
    public Server(
            final ActivationController activation_controller,
            final DataSourcesController sources,
            final Datastore datastore) throws Throwable {

        this.activation_controller = activation_controller;
        this.datastore = datastore;
        this.sources = sources;

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

        LOGGER.info("Starting server...");
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
        LOGGER.info("BATCH mode");
        LOGGER.info("==========");
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

        // Save ranking lists !!

        LOGGER.info("Ask monitor recorder to stop...");
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
        // probably batch mode + data sources have finished
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
}
