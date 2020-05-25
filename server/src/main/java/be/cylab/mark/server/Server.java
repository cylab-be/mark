package be.cylab.mark.server;

import com.google.inject.Inject;
import be.cylab.mark.core.InvalidProfileException;
import be.cylab.mark.datastore.Datastore;
import be.cylab.mark.core.DataAgentProfile;
import be.cylab.mark.webserver.WebServer;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import be.cylab.mark.activation.ActivationController;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.data.DataAgentContainer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
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
    private final WebServer web_server;
    private final LinkedList<DataAgentContainer> data_agents;
    private final ActivationController activation_controller;
    private final Thread monitor;

    /**
     * Initialize a server with default configuration, dummy subject adapter, no
     * data agents and no detection agents.
     *
     * @param config
     * @param web_server
     * @param activation_controller
     * @param datastore
     * @throws java.lang.Throwable on any error
     */
    @Inject
    public Server(final Config config, final WebServer web_server,
            final ActivationController activation_controller,
            final Datastore datastore) throws Throwable {

        this.config = config;
        this.web_server = web_server;
        this.activation_controller = activation_controller;
        this.datastore = datastore;
        this.data_agents = new LinkedList<>();

        this.monitor = new Thread(new Monitor(datastore));

        this.startLogging();
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

        this.parseModules();

        web_server.start();
        activation_controller.testProfiles();
        activation_controller.start();
        datastore.start();
        monitor.start();


        // Start data agents...
        for (DataAgentContainer agent : data_agents) {
            agent.start();
        }

        LOGGER.info("Server started!");
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

        LOGGER.info("Ask webserver to stop...");
        web_server.stop();

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
     * Add the profile for a detection agent.
     *
     * @param profile
     */
    public final void addDetectionAgent(final DetectionAgentProfile profile) {
        activation_controller.addAgent(profile);
    }

    /**
     *
     * @param profile
     * @throws InvalidProfileException if config is not correct
     * @throws MalformedURLException if URL of server is incorrect
     */
    public final void addDataAgentProfile(final DataAgentProfile profile)
            throws InvalidProfileException, MalformedURLException {
        data_agents.add(new DataAgentContainer(profile, config));
    }


    private static final String LOG_PATTERN = "%d [%p] [%t] %c %m%n";

    private void startLogging() {

        ConfigurationBuilder<BuiltConfiguration> builder =
                ConfigurationBuilderFactory.newConfigurationBuilder();

        // log internal errors from log4j
        builder.setStatusLevel(Level.ERROR);



        /*
        LogManager.getRootLogger().getLoggerRepository().resetConfiguration();

        ConsoleAppender console = new ConsoleAppender();
        console.setLayout(new PatternLayout(LOG_PATTERN));
        console.setThreshold(Level.FATAL);
        console.activateOptions();
        LogManager.getRootLogger().addAppender(console);

        console = new ConsoleAppender();
        console.setLayout(new PatternLayout(LOG_PATTERN));
        console.setThreshold(Level.INFO);
        console.activateOptions();
        LogManager.getLogger("be.cylab.mark").addAppender(console);

        try {
            LogManager.getRootLogger().addAppender(
                    getFileAppender("mark.log", Level.INFO));
            LogManager.getLogger("be.cylab.mark.server").addAppender(
                    getFileAppender("mark-server.log", Level.INFO));
            LogManager.getLogger("org.apache.ignite").addAppender(
                    getFileAppender("mark-ignite.log", Level.INFO));
            LogManager.getLogger("org.eclipse.jetty").addAppender(
                    getFileAppender("mark-jetty.log", Level.INFO));
            LogManager.getLogger("be.cylab.mark.activation.ActivationController")
                    .addAppender(getFileAppender(
                            "mark-activationctonroller.log", Level.DEBUG));

        } catch (FileNotFoundException ex) {
            System.err.println(
                    "Logs will not be written to files: " + ex.getMessage());
        }*/

    }

    /*
    private FileAppender getFileAppender(
            final String filename, final Level level)
            throws FileNotFoundException {
        FileAppender fa = new FileAppender();
        fa.setName(filename);
        fa.setFile(
                config.getLogDiretory().getPath() + File.separator + filename);
        fa.setLayout(new PatternLayout("%d [%p] [%t] %c %m%n"));
        fa.setThreshold(level);
        fa.setAppend(true);
        fa.activateOptions();

        return fa;
    }*/

    private void parseModules() throws FileNotFoundException {
        LOGGER.info("Parsing modules directory ");
        File modules_dir;
        try {
            modules_dir = config.getModulesDirectory();
        } catch (FileNotFoundException ex) {
            LOGGER.warn(ex.getMessage());
            return;
        }

        LOGGER.info(modules_dir.getAbsolutePath());
        this.loadJars(modules_dir);
        this.loadDataAgents(modules_dir);
        this.loadDetectionAgents(modules_dir);
    }


    /**
     * Load jars from specified directory.
     *
     * @param directory
     */
    public final void loadJars(final File directory) {

        LOGGER.info("Load jars...");

        try {
            // List *.jar and update the class path
            // this is a hack that allows to modify the global (system) class
            // loader.
            URLClassLoader class_loader
                    = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod(
                    "addURL", URL.class);
            method.setAccessible(true);

            File[] jar_files = directory
                    .listFiles(
                            (final File dir, final String name) ->
                            name.endsWith(".jar"));

            for (File jar_file : jar_files) {
                LOGGER.info(jar_file.getAbsolutePath());
                method.invoke(class_loader, jar_file.toURI().toURL());
            }
        } catch (IllegalAccessException | IllegalArgumentException
                | NoSuchMethodException | SecurityException
                | InvocationTargetException | MalformedURLException ex) {

            LOGGER.warn("Unable to load jar: " + ex.getMessage());

        }

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

    private void loadDetectionAgents(final File modules_dir)
            throws FileNotFoundException {

        // Parse *.detection.yml files
        File[] detection_agent_files
                = modules_dir.listFiles(
                        (final File dir, final String name) ->
                                name.endsWith(".detection.yml"));

        for (File file : detection_agent_files) {
            activation_controller.addAgent(
                    DetectionAgentProfile.fromFile(file));
        }
        LOGGER.info(
                "Found " + activation_controller.getProfiles().size()
                        + " detection agents ...");
    }
}
