package be.cylab.mark.server;

import com.google.inject.Singleton;
import be.cylab.mark.core.InvalidProfileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import be.cylab.mark.core.SubjectAdapter;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author Thibault Debatty
 */
@Singleton
public class Config {

    /**
     * Datastore HTTP/JSON-RPC server parameters : max threads.
     */
    public int max_threads = DEFAULT_MAX_THREADS;
    private static final int DEFAULT_MAX_THREADS = 100;

    /**
     * Datastore HTTP/JSON-RPC server parameters : min threads.
     */
    public int min_threads = DEFAULT_MIN_THREADS;
    private static final int DEFAULT_MIN_THREADS = 4;

    /**
     * Datastore HTTP/JSON-RPC server parameters : idle timeout.
     */
    public int idle_timeout = DEFAULT_IDLE_TIMEOUT;
    private static final int DEFAULT_IDLE_TIMEOUT = 60;

    /**
     * Server host IP.
     */
    public String server_host = DEFAULT_SERVER_HOST;
    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";

    /**
     * Server host Port.
     */
    public int server_port = DEFAULT_SERVER_PORT;
    private static final int DEFAULT_SERVER_PORT = 8080;

    /**
     * Server max pending requests.
     */
    public int max_pending_requests = DEFAULT_MAX_PENDING_REQUESTS;
    private static final int DEFAULT_MAX_PENDING_REQUESTS = 200;

    /**
     * Folder containing modules: jar files (if any) and activation files.
     */
    public String modules = DEFAULT_MODULES;
    private static final String DEFAULT_MODULES = "./modules";

    /**
     * Adapter class to use.
     */
    public String adapter_class = DEFAULT_ADAPTER_CLASS;
    private static final String DEFAULT_ADAPTER_CLASS
            = "be.cylab.mark.server.DummySubjectAdapter";

    /**
     * Webserver port.
     */
    public int webserver_port = DEFAULT_WEBSERVER_PORT;
    private static final int DEFAULT_WEBSERVER_PORT = 8000;

    /**
     * update interval.
     */
    public int update_interval = DEFAULT_UPDATE_INTERVAL;
    private static final int DEFAULT_UPDATE_INTERVAL = 10;

    /**
     * Start (or not) the integrated webserver. Can be disabled for testing, for
     * example...
     */
    public boolean start_webserver = DEFAULT_START_WEBSERVER;
    private static final boolean DEFAULT_START_WEBSERVER = true;

    /**
     * Web root path.
     */
    public String webserver_root = DEFAULT_WEB_ROOT;
    private static final String DEFAULT_WEB_ROOT = "../ui";

    /**
     * Empty the MONGO database before starting (useful for testing).
     */
    public boolean mongo_clean = DEFAULT_MONGO_CLEAN;
    private static final boolean DEFAULT_MONGO_CLEAN = false;

    /**
     * MONGODB parameter : host.
     */
    public String mongo_host = DEFAULT_MONGO_HOST;
    private static final String DEFAULT_MONGO_HOST = "127.0.0.1";

    /**
     * MONGODB parameter : port.
     */
    public int mongo_port = DEFAULT_MONGO_PORT;
    private static final int DEFAULT_MONGO_PORT = 27017;

    /**
     * MONGODB parameter : db name.
     */
    public String mongo_db = DEFAULT_MONGO_DB;
    private static final String DEFAULT_MONGO_DB = "MARK";

    /**
     * Start a local ignite server. Useful for testing or small installation, to
     * execute the detection tasks on the same server.
     */
    public boolean ignite_start_server = DEFAULT_IGNITE_START_SERVER;
    private static boolean DEFAULT_IGNITE_START_SERVER = true;

    /**
     * Enable ignite autodiscovery. Disabling is useful for testing or small
     * setups.
     */
    public boolean ignite_autodiscovery = DEFAULT_IGNITE_AUTODISCOVERY;
    private static boolean DEFAULT_IGNITE_AUTODISCOVERY = true;

    /**
     * module directory path.
     */
    public String log_directory = DEFAULT_LOG_DIRECTORY;
    private static final String DEFAULT_LOG_DIRECTORY = "./modules";

    /**
     * Env variable for tests.
     */
    public static final String ENV_MONGO_HOST = "MARK_MONGO_HOST";

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOGGER
            = LoggerFactory.getLogger(Config.class);

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
     * Build a configuration from a file.
     *
     * @param file
     * @throws java.io.FileNotFoundException
     * @throws java.lang.NoSuchFieldException
     */
    public Config(final File file)
            throws FileNotFoundException {
        this(new Yaml(new Constructor(Config.class))
                .loadAs(new FileInputStream(file), Config.class));
        this.path = file;
        parseConfig();
    }

    /**
     * Generic copy constructor only for non static attributes.
     *
     * @param config
     */
    private Config(Config config) {
        for (Field field : config.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                this.getClass().getDeclaredField(field.getName()).
                        setAccessible(true);
                field.set(this, field.get(config));
            } catch (SecurityException
                    | IllegalArgumentException
                    | IllegalAccessException
                    | NoSuchFieldException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Build configuration from input stream (usually a resource packed with the
     * jar).
     *
     * @param input
     * @return
     */
    public static final Config config(final InputStream input) {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        return yaml.loadAs(input, Config.class);
    }

    /**
     * Instantiate a config for tests: no webserver, updated interval = 1s,
     * clean db at startup.
     *
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
     * Instantiate a new default configuration.
     */
    public Config() {
        this.mongo_host = System.getenv(ENV_MONGO_HOST);
        if (this.mongo_host == null) {
            this.mongo_host = DEFAULT_MONGO_HOST;
        }
    }

    /**
     * Set the path of this configuration file (only used for testing).
     *
     * @param path
     */
    final void setPath(File path) {
        this.path = path;
    }

    /**
     *
     * @return @throws MalformedURLException
     */
    public final URL getDatastoreUrl() throws MalformedURLException {
        return new URL("http", server_host, server_port, "");
    }

    /**
     *
     * @return @throws InvalidProfileException
     */
    public final SubjectAdapter getSubjectAdapter()
            throws InvalidProfileException {
        try {
            return (SubjectAdapter) Class.forName(adapter_class).
                    newInstance();
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException ex) {
            throw new InvalidProfileException("Adapter class is invalid",
                    ex);
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
     * @return @throws FileNotFoundException
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

        if (!logdir_file.isDirectory()) {
            throw new FileNotFoundException(
                    "log directory is not valid: "
                    + log_directory
                    + " (not a directory or not a valid path)");
        }

        return logdir_file;
    }

    /**
     * Analyze the module folder. - modify the class path - parse data agent
     * profiles - parse detection agent profiles
     *
     * @throws MalformedURLException
     */
    private void parseConfig() {

        LOGGER.info("Parse configuration...");

        // List *.jar and update the class path
        // this is a hack that allows to modify the global (system) class
        // loader.
        try {
            URLClassLoader class_loader
                    = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod(
                    "addURL", URL.class);
            method.setAccessible(true);

            File[] jar_files;
            jar_files = this.getModulesDirectory()
                    .listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(final File dir, final String name) {
                            return name.endsWith(".jar");
                        }
                    });

            for (File jar_file : jar_files) {
                method.invoke(class_loader, jar_file.toURI().toURL());
            }
        } catch (Throwable exc) {
            exc.printStackTrace();
        }
    }
}