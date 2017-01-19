package mark.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mark.activation.DetectionAgentProfile;
import mark.client.Client;
import mark.core.SubjectAdapter;

/**
 * Represents a MARK server, composed of a datastore + some data agents + a
 * file server.
 * @author Thibault Debatty
 */
public class Server {

    private static final int START_WAIT_MS = 1000;

    private Config config;
    private Datastore datastore;
    private FileServer file_server;
    private URL server_url;

    private final LinkedList<DataAgentProfile> data_agent_profiles;
    private LinkedList<DataAgentInterface> data_agents;
    private LinkedList<Thread> data_agent_threads;

    private final LinkedList<DetectionAgentProfile> detection_agent_profiles;
    private SubjectAdapter adapter;


    /**
     * Initialize a server with default configuration, dummy subject adapter,
     * no data agents and no detection agents.
     */
    public Server() {

        // Create an empty list of source profiles
        data_agent_profiles = new LinkedList<DataAgentProfile>();
        detection_agent_profiles = new LinkedList<DetectionAgentProfile>();
    }

    /**
     * Non-blocking start the datastore and data agents (sources) in separate
     * threads.
     * This method returns when the server and agents are started.
     * You can use server.stop()
     * @throws java.net.MalformedURLException if the URL specified by config is
     * invalid
     * @throws Exception if Jetty caused an exception
     */
    public final void start()
            throws MalformedURLException, Exception {

        parseConfig();
        parseModulesDirectory();

        // Now we can try to instantiate the adapter, according to config
        // No adapter has been provided programmatically => read from config
        if (adapter == null) {
            adapter = (SubjectAdapter) Class.forName(config.adapter_class)
                    .newInstance();
        }

        startFileServer();
        startDatastore();
        startDataAgents();
        System.out.println("Server started!");
    }

    /**
     * Stop the data agents, wait for all detection agents to complete and
     * eventually stop the datastore.
     * @throws java.lang.Exception if stopping jetty caused an exception
     */
    public final void stop() throws Exception {
        System.out.println("Stopping server...");
        System.out.println("Ask data agents (sources) to finish");
        for (DataAgentInterface source : data_agents) {
            source.stop();
        }

        System.out.println("Wait for data agents (sources) to finish");
        // this cannot be interrupted...
        boolean interrupted = false;
        try {
            for (Thread thread : data_agent_threads) {
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

        System.out.println("Stop file server...");
        file_server.stop();

    }

    /**
     * Wait for data agents to complete, but does NOT stop the datastore.
     * Attention: if your data agent is a network sink, it might never complete.
     * This method is mainly useful for testing with file data sources.
     * @throws InterruptedException if current thread gets en interruption
     */
    public final void awaitTermination() throws InterruptedException {
        System.out.println("Wait for data agents to finish...");
        for (DataAgentInterface source : data_agents) {
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
     * Set configuration before starting the server.
     * @param config
     */
    public final void setConfiguration(final Config config) {
        this.config = config;
    }

    /**
     * Add a data source before starting the server.
     * @param profile
     */
    public final void addDataAgentProfile(final DataAgentProfile profile) {
        data_agent_profiles.add(profile);
    }

    /**
     * Analyze the module folder.
     * - modify the class path
     * - parse data agent profiles
     * - parse detection agent profiles
     * @throws MalformedURLException
     */
    private void parseModulesDirectory()
            throws MalformedURLException, FileNotFoundException,
            ClassNotFoundException, InstantiationException,
            IllegalAccessException, NoSuchMethodException,
            IllegalArgumentException, InvocationTargetException {


        String modules_dir_path = config.getModulesDirectory();
        if (modules_dir_path == null) {
            System.err.println("Modules directory is not valid, skipping...");
            return;
        }

        File modules_dir = new File(modules_dir_path);
        System.out.println("Parsing modules directory "
                + modules_dir.getAbsolutePath());

        if (!modules_dir.isDirectory()) {
            System.err.println("Not a directory, skipping...");
            return;
        }

        // Parse *.jar and update the class path
        // this is a hack that allows to modify the global (system) class
        // loader.
        URLClassLoader class_loader =
                (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method method = URLClassLoader.class.getDeclaredMethod(
                "addURL", URL.class);
        method.setAccessible(true);

        File[] jar_files = modules_dir.listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".jar");
            }
        });

        for (File jar_file : jar_files) {
            method.invoke(class_loader, jar_file.toURI().toURL());
        }


        // Parse *.data.yml files
        File[] data_agent_files = modules_dir.listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".data.yml");
            }
        });

        for (File file : data_agent_files) {
            addDataAgentProfile(DataAgentProfile.fromFile(file));
        }

        // Parse *.detection.yml files
        File[] detection_agent_files =
                modules_dir.listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".detection.yml");
            }
        });

        for (File file : detection_agent_files) {
            addDetectionAgentProfile(DetectionAgentProfile.fromFile(file));
        }
    }

    private void startFileServer() {
        file_server = new FileServer(config);

        // Start the file server in a separate thread
        new Thread(new Runnable() {
            public void run() {
                try {
                    file_server.start();
                } catch (Exception ex) {
                    Logger.getLogger(Server.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    private void startDatastore()
            throws MalformedURLException, InterruptedException, Exception {

        datastore = new Datastore(adapter);
        datastore.setConfiguration(config);
        datastore.setActivationProfiles(detection_agent_profiles);

        // Start the datastore
        new Thread(datastore).start();

        // Wait for Jetty server to start...
        while (!datastore.isStarted()) {

            Thread.sleep(START_WAIT_MS);
        }
    }

    private void startDataAgents() throws Exception {
        // Start the data agents (sources)
        data_agents = new LinkedList<DataAgentInterface>();
        data_agent_threads = new LinkedList<Thread>();

        for (DataAgentProfile profile : data_agent_profiles) {
            try {
                DataAgentInterface source = (DataAgentInterface)
                        Class.forName(profile.class_name).newInstance();

                source.setProfile(profile);
                source.setDatastore(new Client(server_url, adapter));
                Thread source_thread = new Thread(source);
                source_thread.start();

                data_agent_threads.add(source_thread);
                data_agents.add(source);

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

            } catch (Exception ex) {
                System.err.println(ex.getMessage());
                stop();
            }
        }
    }

    /**
     * Add the profile for a detection agent.
     * @param detection_agent_profile
     */
    public final void addDetectionAgentProfile(
            final DetectionAgentProfile detection_agent_profile) {
        detection_agent_profiles.add(detection_agent_profile);
    }

    /**
     * Set the subject adapter.
     * Normally the subject adapter is defined in the configuration file. This
     * method is useful for writing tests.
     * @param adapter
     */
    public final void setSubjectAdapter(final SubjectAdapter adapter) {
        this.adapter = adapter;
    }

    private void parseConfig()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, MalformedURLException {

        // No configuration has been provided => use default config.
        if (config == null) {
            config = new Config();
        }

        server_url = new URL(
                "http://" + config.server_host + ":" + config.server_port);
    }
}
