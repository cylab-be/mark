package mark.server;

/**
 *
 * @author Thibault Debatty
 */
public class MasfadConfig {
    private static final String DEFAULT_MONGO_DB = "MASFAD";
    private static final int    DEFAULT_MAX_THREADS = 100;
    private static final int    DEFAULT_MIN_THREADS = 10;
    private static final int    DEFAULT_IDLE_TIMEOUT = 60;
    private static final int    DEFAULT_SERVER_PORT = 8080;
    private static final int    DEFAULT_MAX_PENDING_REQUESTS = 200;

    // Server configuration
    public String plugins_directory = "./plugins";

    // Datastore HTTP/JSON-RPC server parameters
    public int max_threads = DEFAULT_MAX_THREADS;
    public int min_threads = DEFAULT_MIN_THREADS;
    public int idle_timeout = DEFAULT_IDLE_TIMEOUT;
    public int server_port = DEFAULT_SERVER_PORT;
    public int max_pending_requests = DEFAULT_MAX_PENDING_REQUESTS;

    // MONGODB parameters
    public String mongo_host = "127.0.0.1";
    public int mongo_port = 27017;
    public String mongo_db = DEFAULT_MONGO_DB;

    @Override
    public String toString() {
        return "MasfadConfig with port " + this.server_port;
    }
}