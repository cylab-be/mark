package be.cylab.mark.server;

import com.google.inject.Inject;
import be.cylab.mark.datastore.Datastore;
import be.cylab.mark.core.DataAgentProfile;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import be.cylab.mark.activation.ActivationController;
import be.cylab.mark.data.DataAgentContainer;
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

    private final Config config;
    private final Datastore datastore;
    private final LinkedList<DataAgentContainer> data_agents;
    private final ActivationController activation_controller;
    private final Thread monitor;

    /**
     * Initialize a server with default configuration, dummy subject adapter, no
     * data agents and no detection agents.
     *
     * @param config
     * @param activation_controller
     * @param datastore
     * @throws java.lang.Throwable on any error
     */
    @Inject
    public Server(final Config config,
            final ActivationController activation_controller,
            final Datastore datastore) throws Throwable {

        this.config = config;
        this.activation_controller = activation_controller;
        this.datastore = datastore;
        this.data_agents = new LinkedList<>();

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
        LOGGER.debug(System.getProperty("java.class.path"));
        this.parseModules();

        activation_controller.reload();
        activation_controller.start();
        datastore.start();
        monitor.start();


        // Start data agents...
        for (DataAgentContainer agent : data_agents) {
            agent.start();
        }

        LOGGER.info("=======================================");
        LOGGER.info("MARk");
        LOGGER.info(getClass().getPackage().getImplementationVersion());
        LOGGER.info("=======================================");
    }

    /**
     * Stop the data agents, wait for all detection agents to complete and
     * eventually stop the datastore.
     * @throws java.lang.Exception on any error
     */
    public final void stop() throws Exception {
        LOGGER.info("Stopping server...");
        LOGGER.info("Ask data agents to stop...");
        for (DataAgentContainer agent : data_agents) {
            agent.interrupt();
        }

        awaitTermination();

        LOGGER.info("Ask activation controller to stop...");
        activation_controller.interrupt();
        activation_controller.join();

        LOGGER.info("Ask datastore to stop...");
        datastore.stop();

        LOGGER.info("Server stopped!");
    }

    /**
     *
     * @throws InterruptedException if thread is interrupted while waiting
     */
    public final void awaitTermination() throws InterruptedException {
        LOGGER.info("Wait for data agents to finish...");
        for (DataAgentContainer agent : data_agents) {
            agent.join();
        }

        LOGGER.info(
                "Wait for activation controller to finish running tasks...");
        activation_controller.awaitTermination();
    }

    /**
     * Allows to programmatically add a data agent to the server.
     *
     * Used for integration tests for example.
     *
     * @param profile
     */
    public final void addDataAgentProfile(final DataAgentProfile profile) {
        data_agents.add(new DataAgentContainer(profile, config));
    }


    private void parseModules() throws FileNotFoundException {
        LOGGER.info("Parsing modules directory ");
        File modules_dir;
        try {
            modules_dir = config.getModulesDirectory();
        } catch (FileNotFoundException ex) {
            LOGGER.warn(ex.getMessage());
            LOGGER.warn("Skipping modules parsing ...");
            return;
        }

        LOGGER.info(modules_dir.getAbsolutePath());
        this.loadDataAgents(modules_dir);
    }

    private void loadDataAgents(final File modules_dir)
            throws FileNotFoundException {

        // Parse *.data.yml files
        File[] data_agent_files = modules_dir.listFiles(
                (final File dir, final String name) ->
                        name.endsWith(".data.yml"));

        //Instanciate DataAgentProfiles for each previously parsed files.
        for (File file : data_agent_files) {
            data_agents.add(
                    new DataAgentContainer(
                            DataAgentProfile.fromFile(file),
                            config));
        }
        LOGGER.info("Found " + data_agents.size() + " data agents ...");
    }
}
