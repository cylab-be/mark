package mark.server;

import mark.core.InvalidProfileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import mark.core.SubjectAdapter;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author Thibault Debatty
 */
public class Config {
    private static final int    DEFAULT_UPDATE_INTERVAL = 10;
    private static final String DEFAULT_MONGO_DB = "MARK";
    private static final int    DEFAULT_MAX_THREADS = 100;
    private static final int    TEST_MAX_THREADS = 10;
    private static final int    DEFAULT_MIN_THREADS = 4;
    private static final int    DEFAULT_IDLE_TIMEOUT = 60;
    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private static final int    DEFAULT_SERVER_PORT = 8080;
    private static final int    DEFAULT_MAX_PENDING_REQUESTS = 200;
    private static final String DEFAULT_MODULES = "./modules";

    private static final String DEFAULT_ADAPTER = "mark.server.DummySubjectAdapter";

    private static final int    DEFAULT_WEB_PORT = 8000;
    private static final String DEFAULT_WEB_ROOT = "../ui";

    /**
     * Build a configuration from a file.
     * @param file
     * @return
     * @throws java.io.FileNotFoundException
     */
    public static final Config fromFile(final File file)
            throws FileNotFoundException {
        Config conf = Config.fromInputStream(new FileInputStream(file));
        conf.path = file;
        return conf;
    }

    /**
     * Build configuration from input stream (usually a resource packed with
     * the jar).
     * @param input
     * @return
     */
    public static final Config fromInputStream(final InputStream input) {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        return yaml.loadAs(input, Config.class);
    }

    /**
     * Instantiate a config for tests: no webserver, updated interval = 1s,
     * clean db at startup.
     * @return
     */
    public static final Config getTestConfig() {
        Config conf = new Config();
        conf.start_webserver = false;
        conf.update_interval = 1;
        conf.mongo_clean = true;
        conf.ignite_autodiscovery = false;
        conf.max_threads = TEST_MAX_THREADS;

        return conf;
    }


    /**
     * The path to this actual configuration file. Useful as some path are
     * relative to this config file...
     */
    private File path;

    /**
     * Folder containing modules: jar files (if any) and activation files.
     */
    public String modules = DEFAULT_MODULES;

    public String adapter_class = DEFAULT_ADAPTER;

    public String log_directory = null;

    //
    public int update_interval = DEFAULT_UPDATE_INTERVAL;

    // Datastore HTTP/JSON-RPC server parameters
    public int max_threads = DEFAULT_MAX_THREADS;
    public int min_threads = DEFAULT_MIN_THREADS;
    public int idle_timeout = DEFAULT_IDLE_TIMEOUT;
    public String server_host = DEFAULT_SERVER_HOST;
    public int server_port = DEFAULT_SERVER_PORT;
    public int max_pending_requests = DEFAULT_MAX_PENDING_REQUESTS;

    /**
     * Start (or not) the integrated webserver.
     * Can be disabled for testing, for example...
     */
    public boolean start_webserver = true;

    public int webserver_port = DEFAULT_WEB_PORT;
    public String webserver_root = DEFAULT_WEB_ROOT;

    // MONGODB parameters
    public String mongo_host = "127.0.0.1";
    public int mongo_port = 27017;
    public String mongo_db = DEFAULT_MONGO_DB;

    /**
     * Empty the MONGO database before starting (useful for testing).
     */
    public boolean mongo_clean = false;

    /**
     * Start a local ignite server.
     * Useful for testing or small installation, to execute the detection
     * tasks on the same server.
     */
    public boolean ignite_start_server = true;

    /**
     * Enable ignite autodiscovery.
     * Disabling is useful for testing or small setups.
     */
    public boolean ignite_autodiscovery = true;

    /**
     * Instantiate a new default configuration.
     */
    public Config() {

    }

    /**
     * Set the path of this configuration file (only used for testing).
     * @param path
     */
    final void setPath(File path) {
        this.path = path;
    }

    /**
     *
     * @return
     * @throws MalformedURLException
     */
    public final URL getDatastoreUrl() throws MalformedURLException {
        return new URL("http", server_host, server_port, "");
    }

    /**
     *
     * @return
     * @throws InvalidProfileException
     */
    public final SubjectAdapter getSubjectAdapter()
            throws InvalidProfileException {
        try {
            return (SubjectAdapter) Class.forName(adapter_class).newInstance();
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException ex) {
            throw new InvalidProfileException("Adapter class is invalid", ex);
        }
    }

    @Override
    public final String toString() {
        return "Config with port " + this.server_port;
    }

    /**
     *
     * @return null if the path is incorrect.
     */
    public final File getModulesDirectory() throws FileNotFoundException {

        File modules_file = new File(modules);

        if (!modules_file.isAbsolute()) {
            // modules is a relative path...
            if (path == null) {
                throw new FileNotFoundException(
                    "modules directory is not valid (not a directory "
                            + "or not a valid path)");
            }
            modules_file = new File(path.toURI().resolve(modules));
        }

        if (!modules_file.isDirectory()) {
            throw new FileNotFoundException(
                    "modules directory is not valid (not a directory "
                            + "or not a valid path)");
        }

        return modules_file;
    }

    /**
     *
     * @param webserver_root
     */
    public final void setWebserverRoot(final String webserver_root) {
        this.webserver_root = webserver_root;
    }

    /**
     *
     * @return
     * @throws FileNotFoundException
     */
    public final File getWebserverRoot() throws FileNotFoundException {
        File webroot_file = new File(webserver_root);

        if (!webroot_file.isAbsolute()) {
            // web root is a relative path...
            if (path == null) {
                throw new FileNotFoundException(
                    "webserver root is not valid: "
                            + webserver_root
                            + " (not a directory or not a valid path)");
            }
            webroot_file = new File(path.toURI().resolve(webserver_root));
        }

        if (!webroot_file.isDirectory()) {
            throw new FileNotFoundException(
                    "webserver root is not valid: "
                            + webserver_root
                            + " (not a directory or not a valid path)");
        }

        return webroot_file;
    }

    public final File getLogDiretory() throws FileNotFoundException {
        if (log_directory == null) {
            throw new FileNotFoundException("Log dir is null (undefined)");
        }

        File logdir_file = new File(log_directory);

        if (!logdir_file.isAbsolute()) {
            // it's a relative file
            if (path == null) {
                throw new FileNotFoundException(
                    "log directory is not valid: "
                        + log_directory
                        + " (not a directory or not a valid path)");
            }
            logdir_file = new File(path.toURI().resolve(log_directory));
        }

        if(!logdir_file.isDirectory()) {
            throw new FileNotFoundException(
                "log directory is not valid: "
                    + log_directory
                    + " (not a directory or not a valid path)");
        }

        return logdir_file;
    }
}