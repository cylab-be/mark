package mark.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import mark.activation.ActivationProfile;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Represents a masfad server, composed of a datastore + some data agents.
 * @author Thibault Debatty
 */
public class Server {

    private static final int START_WAIT_MS = 1000;

    private MasfadConfig config;
    private final Datastore datastore;
    private Iterable<SourceProfile> source_profiles;
    private ArrayList<Thread> source_threads;


    /**
     * Initialize a masfad server with default configuration, no data agents
     * and no detection agents.
     */
    public Server() {
        config = new MasfadConfig();
        datastore = new Datastore();

        // Create an empty list of source profiles
        source_profiles = new LinkedList<SourceProfile>();
    }

    /**
     * Start the datastore and data agents (sources) in separate threads.
     * This method returns when the server and agents are started.
     * You can use server.stop()
     */
    public final void start() {

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
            }
        }

        // Start the data agents (sources)
        source_threads = new ArrayList<Thread>();

        for (SourceProfile profile : source_profiles) {
            try {
                DataAgentInterface source = (DataAgentInterface)
                        Class.forName(profile.class_name).newInstance();

                source.setParameters(profile.parameters);
                Thread source_thread = new Thread(source);
                source_thread.start();
                source_threads.add(source_thread);

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
    }

    /**
     * Kill the data agents, wait for all detection agents to complete and
     * eventually stop the datastore.
     */
    public final void stop() {
        System.out.println("Stopping server...");
        System.out.println("Kill data agents (sources)");
        for (Thread source_thread : source_threads) {
            source_thread.stop();
        }

        System.out.println("Wait for tasks to complete...");
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
        for (Thread source_thread : source_threads) {
            source_thread.join();
        }
    }

    /**
     *
     * @return
     */
    public final MasfadConfig getConfiguration() {
        return config;
    }

    /**
     * Set configuration from a file before starting the server.
     * @param config_file
     * @throws FileNotFoundException if the config file does not exist
     */
    public final void setConfiguration(final InputStream config_file)
            throws FileNotFoundException {

        Yaml yaml = new Yaml(new Constructor(MasfadConfig.class));
        this.setConfiguration(yaml.loadAs(
                config_file, MasfadConfig.class));
    }

    /**
     * Set configuration before starting the server.
     * @param config
     */
    public final void setConfiguration(final MasfadConfig config) {
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
     * @param profiles
     * @throws Exception if the file does not exist or cannot be parsed.
     */
    public final void setActivationProfiles(final InputStream profiles)
            throws Exception {

        datastore.setActivationProfiles(profiles);
    }

    /**
     * Set the data agent profiles before starting the server.
     * @param profiles
     */
    public final void setSourceProfiles(
            final Iterable<SourceProfile> profiles) {
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
