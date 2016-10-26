package mark.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import mark.activation.ActivationProfile;
import mark.activation.InvalidProfileException;
import mark.client.Client;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Represents a MARK server, composed of a datastore + some data agents.
 * @author Thibault Debatty
 */
public class Server {

    private static final int START_WAIT_MS = 1000;

    private Config config;
    private final Datastore datastore;

    private LinkedList<SourceProfile> source_profiles;
    private LinkedList<DataAgentInterface> source_agents;
    private LinkedList<Thread> source_threads;


    /**
     * Initialize a server with default configuration, no data agents
     * and no detection agents.
     */
    public Server() throws MalformedURLException {
        config = new Config();
        datastore = new Datastore();

        // Create an empty list of source profiles
        source_profiles = new LinkedList<SourceProfile>();
    }

    /**
     * Non-blocking start the datastore and data agents (sources) in separate
     * threads.
     * This method returns when the server and agents are started.
     * You can use server.stop()
     */
    public final void start() throws MalformedURLException {

        URL server_url = new URL(
                "http://" + config.server_host + ":" + config.server_port);

        // Set the plugins directory
        ClassLoader original_classloader =
                Thread.currentThread().getContextClassLoader();
        File plugins_file = new File(config.plugins_directory);
        try {
            URL plugins_url = plugins_file.toURI().toURL();
            // Create class loader using given codebase
            // Use prevCl as parent to maintain current visibility
            ClassLoader url_classloader = URLClassLoader.newInstance(
                    new URL[]{plugins_url},
                    original_classloader);

            Thread.currentThread().setContextClassLoader(url_classloader);

        } catch (MalformedURLException ex) {
            System.err.println("Failed to configure plugins folder: "
                    + ex.getMessage());
            return;
        }

        // Start the datastore
        new Thread(datastore).start();

        // Wait for Jetty server to start...
        while (!datastore.isStarted()) {
            try {
                Thread.sleep(START_WAIT_MS);

            } catch (InterruptedException ex) {
                // Something is trying to stop the main thread

                datastore.stop();
                return;
            }
        }

        // Start the data agents (sources)
        source_agents = new LinkedList<DataAgentInterface>();
        source_threads = new LinkedList<Thread>();

        for (SourceProfile profile : source_profiles) {
            try {
                DataAgentInterface source = (DataAgentInterface)
                        Class.forName(profile.class_name).newInstance();

                source.setParameters(profile.parameters);
                source.setDatastore(new Client(server_url));
                Thread source_thread = new Thread(source);
                source_thread.start();

                source_threads.add(source_thread);
                source_agents.add(source);

            } catch (ClassNotFoundException ex) {
                // If any of the data agents fail to start,
                // we stop the server in a correct way
                System.err.println("Failed to initialize agent "
                        + profile.class_name);
                System.err.println(ex.getMessage());
                stop();

            } catch (IllegalAccessException ex) {
                System.err.println("Failed to initialize agent "
                        + profile.class_name);
                System.err.println(ex.getMessage());
                stop();

            } catch (InstantiationException ex) {
                System.err.println("Failed to initialize agent "
                        + profile.class_name);
                System.err.println(ex.getMessage());
                stop();
            }
        }

        System.out.println("Server started!");
    }

    /**
     * Stop the data agents, wait for all detection agents to complete and
     * eventually stop the datastore.
     */
    public final void stop() {
        System.out.println("Stopping server...");
        System.out.println("Ask data agents (sources) to finish");
        for (DataAgentInterface source : source_agents) {
            source.stop();
        }

        System.out.println("Wait for data agents (sources) to finish");
        // this cannot be interrupted...
        boolean interrupted = false;
        try {
            for (Thread thread : source_threads) {
                while (thread.isAlive()) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        interrupted = true;
                        // fall through and retry
                    }
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }


        System.out.println("Wait for detection tasks to complete...");
        datastore.stop();
    }

    /**
     * Wait for data agents to complete, but does NOT stop the datastore.
     * Attention: if your data agent is a network sink, it might never complete.
     * This method is mainly useful for testing with file data sources.
     * @throws InterruptedException
     */
    public final void awaitTermination() throws InterruptedException {
        System.out.println("Wait for data agents to finish...");
        for (DataAgentInterface source : source_agents) {
            source.stop();
        }
    }

    /**
     *
     * @return
     */
    public final Config getConfiguration() {
        return config;
    }

    /**
     * Set configuration from a file before starting the server.
     * @param config_file
     * @throws FileNotFoundException if the config file does not exist
     */
    public final void setConfiguration(final InputStream config_file)
            throws FileNotFoundException {

        Yaml yaml = new Yaml(new Constructor(Config.class));
        this.setConfiguration(yaml.loadAs(
                config_file, Config.class));
    }

    /**
     * Set configuration before starting the server.
     * @param config
     */
    public final void setConfiguration(final Config config) {
        this.config = config;
        this.datastore.setConfiguration(config);
    }


    /**
     * Set activation profiles before starting the server.
     * @param profiles
     * @throws Exception if the profiles are corrupted (misspelled class name?)
     */
    public final void setActivationProfiles(
            final Iterable<ActivationProfile> profiles)
            throws Exception {

        datastore.setActivationProfiles(profiles);
    }

    /**
     * Set activation profiles from YAML file before starting the server.
     * @param profiles file
     * @throws java.io.FileNotFoundException
     * @throws mark.activation.InvalidProfileException
     */
    public final void setActivationProfiles(final InputStream profiles)
            throws FileNotFoundException, InvalidProfileException {

        datastore.setActivationProfiles(profiles);
    }

    /**
     * Set the data agent profiles before starting the server.
     * @param profiles
     */
    public final void setSourceProfiles(
            final LinkedList<SourceProfile> profiles) {
        this.source_profiles = profiles;
    }

    /**
     * Set the data agent profiles from YAML file before starting the server.
     * @param profiles_config
     */
    public final void setSourceProfiles(final InputStream profiles_config) {
        Yaml yaml = new Yaml(new Constructor(SourceProfile.class));
        Iterable<Object> all = yaml.loadAll(profiles_config);
        LinkedList<SourceProfile> all_profiles =
        new LinkedList<SourceProfile>();
        for (Object profile_object : all) {
            all_profiles.add((SourceProfile) profile_object);
        }

        setSourceProfiles(all_profiles);
    }
}
