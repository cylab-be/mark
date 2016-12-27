package mark.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author Thibault Debatty
 */
public class Config {
    private static final String DEFAULT_MONGO_DB = "MASFAD";
    private static final int    DEFAULT_MAX_THREADS = 100;
    private static final int    DEFAULT_MIN_THREADS = 10;
    private static final int    DEFAULT_IDLE_TIMEOUT = 60;
    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private static final int    DEFAULT_SERVER_PORT = 8080;
    private static final int    DEFAULT_MAX_PENDING_REQUESTS = 200;
    private static final String DEFAULT_MODULES = "./modules";

    private static final int    DEFAULT_WEB_PORT = 8000;
    private static final String DEFAULT_WEB_ROOT = "../ui";

    // Server configuration

    /**
     * Instantiate a new defautl configuration.
     */
    public Config() {

    }

    /**
     * Build a configuration from a file.
     * @param file
     * @return
     * @throws java.io.FileNotFoundException
     */
    public final static Config fromFile(final File file) throws FileNotFoundException {
        Config conf = Config.fromInputStream(new FileInputStream(file));
        conf.file = file;
        return conf;
    }

    public static final Config fromInputStream(final InputStream input) {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        return yaml.loadAs(input, Config.class);
    }


    /**
     * The path to this actual configuration file. Useful as some path are
     * relative to this config file...
     */
    private File file;

    /**
     * Folder containing modules: jar files (if any) and activation files.
     */
    public String modules = DEFAULT_MODULES;

    // Datastore HTTP/JSON-RPC server parameters
    public int max_threads = DEFAULT_MAX_THREADS;
    public int min_threads = DEFAULT_MIN_THREADS;
    public int idle_timeout = DEFAULT_IDLE_TIMEOUT;
    public String server_host = DEFAULT_SERVER_HOST;
    public int server_port = DEFAULT_SERVER_PORT;
    public int max_pending_requests = DEFAULT_MAX_PENDING_REQUESTS;

    // Web server parameters
    public int web_port = DEFAULT_WEB_PORT;
    public String web_root = DEFAULT_WEB_ROOT;

    // MONGODB parameters
    public String mongo_host = "127.0.0.1";
    public int mongo_port = 27017;
    public String mongo_db = DEFAULT_MONGO_DB;


    @Override
    public final String toString() {
        return "Config with port " + this.server_port;
    }

    /**
     *
     * @return null if the path is incorrect.
     */
    final String getModulesDirectory() {
        if (file == null) {
            return null;
        }

        return file.toURI().resolve(modules).getPath();
    }
}