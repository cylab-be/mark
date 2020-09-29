package be.cylab.mark.server;

import be.cylab.mark.activation.IgniteExecutor;
import be.cylab.mark.activation.ThreadsExecutor;
import com.google.inject.Singleton;
import be.cylab.mark.core.InvalidProfileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import be.cylab.mark.core.SubjectAdapter;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author Thibault Debatty
 */
@Singleton
public final class Config {

    /**
     * Datastore HTTP/JSON-RPC server parameters : max threads.
     */
    private int max_threads = DEFAULT_MAX_THREADS;
    private static final int DEFAULT_MAX_THREADS = 100;

    /**
     * Datastore HTTP/JSON-RPC server parameters : min threads.
     */
    private int min_threads = DEFAULT_MIN_THREADS;
    private static final int DEFAULT_MIN_THREADS = 4;

    /**
     * Datastore HTTP/JSON-RPC server parameters : idle timeout.
     */
    private int idle_timeout = DEFAULT_IDLE_TIMEOUT;
    private static final int DEFAULT_IDLE_TIMEOUT = 60;

    private String server_bind = "0.0.0.0";

    /**
     * Server host IP.
     */
    private String server_host = DEFAULT_SERVER_HOST;
    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";

    /**
     * Server host Port.
     */
    private int server_port = DEFAULT_SERVER_PORT;
    private static final int DEFAULT_SERVER_PORT = 8080;

    /**
     * Server max pending requests.
     */
    private int max_pending_requests = DEFAULT_MAX_PENDING_REQUESTS;
    private static final int DEFAULT_MAX_PENDING_REQUESTS = 200;

    /**
     * Folder containing modules: jar files (if any) and activation files.
     */
    private String modules = DEFAULT_MODULES;
    private static final String DEFAULT_MODULES = "./modules";

    /**
     * Adapter class to use.
     */
    private String adapter_class = DEFAULT_ADAPTER_CLASS;
    private static final String DEFAULT_ADAPTER_CLASS
            = "be.cylab.mark.server.DummySubjectAdapter";

    /**
     * Webserver port.
     */
    private int webserver_port = DEFAULT_WEBSERVER_PORT;
    private static final int DEFAULT_WEBSERVER_PORT = 8000;

    /**
     * update interval.
     */
    private int update_interval = DEFAULT_UPDATE_INTERVAL;
    private static final int DEFAULT_UPDATE_INTERVAL = 10;

    /**
     * Start (or not) the integrated webserver. Can be disabled for testing, for
     * example...
     */
    private boolean start_webserver = DEFAULT_START_WEBSERVER;
    private static final boolean DEFAULT_START_WEBSERVER = true;

    /**
     * Empty the MONGO database before starting (useful for testing).
     */
    private boolean mongo_clean = DEFAULT_MONGO_CLEAN;
    private static final boolean DEFAULT_MONGO_CLEAN = false;

    /**
     * MONGODB parameter : host.
     */
    private String mongo_host = DEFAULT_MONGO_HOST;
    private static final String DEFAULT_MONGO_HOST = "127.0.0.1";

    /**
     * MONGODB parameter : port.
     */
    private int mongo_port = DEFAULT_MONGO_PORT;
    private static final int DEFAULT_MONGO_PORT = 27017;

    /**
     * MONGODB parameter : db name.
     */
    private String mongo_db = DEFAULT_MONGO_DB;
    private static final String DEFAULT_MONGO_DB = "MARK";

    /**
     * Start a local ignite server. Useful for testing or small installation, to
     * execute the detection tasks on the same server.
     */
    private boolean ignite_start_server = DEFAULT_IGNITE_START_SERVER;
    private static final boolean DEFAULT_IGNITE_START_SERVER = true;

    /**
     * Enable ignite autodiscovery. Disabling is useful for testing or small
     * setups.
     */
    private boolean ignite_autodiscovery = DEFAULT_IGNITE_AUTODISCOVERY;
    private static final boolean DEFAULT_IGNITE_AUTODISCOVERY = true;

    /**
     * logs directory path.
     */
    private String log_directory = DEFAULT_LOG_DIRECTORY;
    private static final String DEFAULT_LOG_DIRECTORY = "./log";

    private String executor_class = ThreadsExecutor.class.getCanonicalName();

    /**
     * Env variable for setting mongodb host.
     */
    public static final String ENV_MONGO_HOST = "MARK_MONGO_HOST";

    /**
     * Max threads for tests.
     */
    private static final int TEST_MAX_THREADS = 9;

    /**
     * The path to this actual configuration file. Useful as some path are
     * relative to this config file...
     */
    private File path;

    /**
     * Instantiate a new default configuration.
     */
    public Config() {
        this.readEnvironment();
    }

    /**
     * Build a configuration from a file.
     *
     * @param file
     * @return
     * @throws java.io.FileNotFoundException if the config file does not exist
     * @throws Exception if the config is not valid
     */
    public static Config fromFile(final File file)
            throws FileNotFoundException, Exception {

        Config config = fromInputStream(new FileInputStream(file));
        config.readEnvironment();
        config.path = file;
        config.validate();
        return config;
    }

    /**
     * Build configuration from input stream (usually a resource packed with the
     * jar).
     *
     * @param input
     * @return
     */
    public static Config fromInputStream(final InputStream input) {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        Config config = yaml.loadAs(input, Config.class);
        config.readEnvironment();
        return config;
    }

    /**
     * Instantiate a config for tests: no webserver, updated interval = 1s,
     * clean db at startup.
     *
     * @return
     */
    public static Config getTestConfig() {
        Config conf = new Config();
        conf.start_webserver = false;
        conf.update_interval = 1;
        conf.mongo_clean = true;
        conf.ignite_autodiscovery = false;
        conf.log_directory = "/tmp";
        conf.max_threads = TEST_MAX_THREADS;

        return conf;
    }

    /**
     * Set the path of this configuration file (only used for testing).
     *
     * @param path
     */
    void setPath(final File path) {
        this.path = path;
    }

    /**
     * Read environment variables.
     */
    private void readEnvironment() {
        if (System.getenv(ENV_MONGO_HOST) != null) {
            this.mongo_host = System.getenv(ENV_MONGO_HOST);
        }

        if (System.getenv("MARK_MONGO_PORT") != null) {
            this.mongo_port = Integer.valueOf(System.getenv("MARK_MONGO_PORT"));
        }

        if (System.getenv("MARK_SERVER_BIND") != null) {
            this.server_bind = System.getenv("MARK_SERVER_BIND");
        }
    }

    /**
     *
     * @return @throws MalformedURLException if datastore url from configuration
     * is not valid
     */
    public URL getDatastoreUrl() throws MalformedURLException {
        return new URL("http", server_host, server_port, "");
    }

    /**
     *
     * @return @throws InvalidProfileException if the adapter class is invalid
     */
    public SubjectAdapter getSubjectAdapter()
            throws InvalidProfileException {
        try {
            return (SubjectAdapter) Class.forName(adapter_class).
                    newInstance();
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException ex) {
            throw new InvalidProfileException(
                    "Adapter class " + adapter_class + " is invalid",
                    ex);
        }
    }

    /**
     * Check that this configuration is valid.
     * @return
     * @throws java.lang.Exception if the configuration is invalid
     */
    public boolean validate() throws Exception {

        if (server_host.equals("127.0.0.1")
                && executor_class.equals(IgniteExecutor.class.getName())
                && ignite_autodiscovery) {
            throw new Exception(
                    "Server host cannot be 127.0.0.1 with a distributed "
                            + "executor (ignite_autodiscovery is true)!");
        }
        this.getSubjectAdapter();
        this.getDatastoreUrl();

        return true;
    }

    @Override
    public String toString() {
        return "Config with port " + this.server_port;
    }

    /**
     *
     * @return null if the path is incorrect.
     * @throws java.io.FileNotFoundException if the modules directory does not
     * exist
     */
    public File getModulesDirectory() throws FileNotFoundException {

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
     * @return
     */
    public int getMaxThreads() {
        return max_threads;
    }

    /**
     *
     * @param max_threads
     */
    public void setMaxThreads(final int max_threads) {
        this.max_threads = max_threads;
    }

    /**
     *
     * @return
     */
    public int getMinThreads() {
        return min_threads;
    }

    /**
     *
     * @param min_threads
     */
    public void setMinThreads(final int min_threads) {
        this.min_threads = min_threads;
    }

    /**
     *
     * @return
     */
    public int getIdleTimeout() {
        return idle_timeout;
    }

    /**
     *
     * @param idle_timeout
     */
    public void setIdleTimeout(final int idle_timeout) {
        this.idle_timeout = idle_timeout;
    }

    /**
     *
     * @return
     */
    public String getServerHost() {
        return server_host;
    }

    /**
     *
     * @param server_host
     */
    public void setServerHost(final String server_host) {
        this.server_host = server_host;
    }

    /**
     *
     * @return
     */
    public int getServerPort() {
        return server_port;
    }

    /**
     *
     * @param server_port
     */
    public void setServerPort(final int server_port) {
        this.server_port = server_port;
    }

    /**
     *
     * @return
     */
    public int getMaxPendingRequests() {
        return max_pending_requests;
    }

    /**
     *
     * @param max_pending_requests
     */
    public void setMaxPendingRequests(final int max_pending_requests) {
        this.max_pending_requests = max_pending_requests;
    }

    /**
     *
     * @return
     */
    public String getModules() {
        return modules;
    }

    /**
     *
     * @param modules
     */
    public void setModules(final String modules) {
        this.modules = modules;
    }

    /**
     *
     * @return
     */
    public String getAdapterClass() {
        return adapter_class;
    }

    /**
     *
     * @param adapter_class
     */
    public void setAdapterClass(final String adapter_class) {
        this.adapter_class = adapter_class;
    }

    /**
     *
     * @return
     */
    public int getWebserverPort() {
        return webserver_port;
    }

    /**
     *
     * @param webserver_port
     */
    public void setWebserverPort(final int webserver_port) {
        this.webserver_port = webserver_port;
    }

    /**
     *
     * @return
     */
    public int getUpdateInterval() {
        return update_interval;
    }

    /**
     *
     * @param update_interval
     */
    public void setUpdateInterval(final int update_interval) {
        this.update_interval = update_interval;
    }

    /**
     *
     * @return
     */
    public boolean isStartWebserver() {
        return start_webserver;
    }

    /**
     *
     * @param start_webserver
     */
    public void setStartWebserver(final boolean start_webserver) {
        this.start_webserver = start_webserver;
    }

    /**
     *
     * @return
     */
    public boolean isMongoClean() {
        return mongo_clean;
    }

    /**
     *
     * @param mongo_clean
     */
    public void setMongoClean(final boolean mongo_clean) {
        this.mongo_clean = mongo_clean;
    }

    /**
     *
     * @return
     */
    public String getMongoHost() {
        return mongo_host;
    }

    /**
     *
     * @param mongo_host
     */
    public void setMongoHost(final String mongo_host) {
        this.mongo_host = mongo_host;
    }

    /**
     *
     * @return
     */
    public int getMongoPort() {
        return mongo_port;
    }

    /**
     *
     * @param mongo_port
     */
    public void setMongoPort(final int mongo_port) {
        this.mongo_port = mongo_port;
    }

    /**
     *
     * @return
     */
    public String getMongoDb() {
        return mongo_db;
    }

    /**
     *
     * @param mongo_db
     */
    public void setMongoDb(final String mongo_db) {
        this.mongo_db = mongo_db;
    }

    /**
     *
     * @return
     */
    public boolean isIgniteStartServer() {
        return ignite_start_server;
    }

    /**
     *
     * @param ignite_start_server
     */
    public void setIgniteStartServer(final boolean ignite_start_server) {
        this.ignite_start_server = ignite_start_server;
    }

    /**
     *
     * @return
     */
    public boolean isIgniteAutodiscovery() {
        return ignite_autodiscovery;
    }

    /**
     *
     * @param ignite_autodiscovery
     */
    public void setIgniteAutodiscovery(final boolean ignite_autodiscovery) {
        this.ignite_autodiscovery = ignite_autodiscovery;
    }

    /**
     *
     * @return
     */
    public String getLogDirectory() {
        return log_directory;
    }

    /**
     *
     * @param log_directory
     */
    public void setLogDirectory(final String log_directory) {
        this.log_directory = log_directory;
    }

    /**
     *
     * @return
     */
    public String getServerBind() {
        return this.server_bind;
    }

    /**
     *
     * @param server_bind
     */
    public void setServerBind(final String server_bind) {
        this.server_bind = server_bind;
    }

    /**
     *
     * @return
     */
    public String getExecutorClass() {
        return executor_class;
    }

    /**
     *
     * @param executor_class
     */
    public void setExecutorClass(final String executor_class) {
        this.executor_class = executor_class;
    }
}
