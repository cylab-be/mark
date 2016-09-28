package mark.server;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import mark.activation.ActivationController;
import mark.activation.ActivationProfile;
import mark.activation.InvalidProfileException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 *
 * @author Thibault Debatty
 */
public class Datastore implements Runnable {

    private Config config;

    // isStarted() will be called from another thread => must be volatile
    private volatile Server http_server;
    private final ActivationController activation_controller;

    /**
     * Instatiate a datastore with default config and empty activation profiles.
     */
    public Datastore() {
        this.config = new Config();
        this.activation_controller = new ActivationController();
        activation_controller.setServerAddress(
                "http://" + config.server_host + ":" + config.server_port);
    }

    /**
     * Set the activation profiles.
     * @param profiles
     * @throws Exception if the profiles are corrupted (misspelled class name?)
     */
    public final void setActivationProfiles(
            final Iterable<ActivationProfile> profiles)
            throws Exception {

        activation_controller.setProfiles(profiles);
    }

    /**
     * Set the activation profiles from a file before starting the server.
     * @param profiles
     * @throws FileNotFoundException
     * @throws InvalidProfileException
     */
    public final void setActivationProfiles(final InputStream profiles)
        throws FileNotFoundException, InvalidProfileException{
        activation_controller.setProfiles(profiles);
    }

    /**
     * Set the configuration before starting the server.
     * @param config
     */
    public final void setConfiguration(final Config config) {
        this.config = config;
    }

    /**
     * Run the datastore server, blocking.
     * This method will only return if the server crashes...
     *
     */
    public final void run() {

        // Connect to mongodb
        MongoClient mongodb = new MongoClient(
                config.mongo_host, config.mongo_port);
        MongoDatabase mongodb_database = mongodb.getDatabase(config.mongo_db);

        // Create and run HTTP / JSON-RPC server
        RequestHandler datastore_handler = new RequestHandler(
                mongodb_database,
                activation_controller);

        JsonRpcServer jsonrpc_server = new JsonRpcServer(datastore_handler);

        QueuedThreadPool thread_pool = new QueuedThreadPool(
                config.max_threads,
                config.min_threads,
                config.idle_timeout,
                new ArrayBlockingQueue<Runnable>(config.max_pending_requests));

        http_server = new Server(thread_pool);

        ServerConnector http_connector = new ServerConnector(http_server);
        http_connector.setHost(config.server_host);
        http_connector.setPort(config.server_port);

        http_server.setConnectors(new Connector[]{http_connector});
        http_server.setHandler(new JettyHandler(jsonrpc_server));

        try {
            http_server.start();
        } catch (Exception ex) {
            System.err.println("Failed to start datastore: " + ex.getMessage());
        }
    }

    /**
     * Returns true if the datastore is completely started (http server).
     * @return
     */
    public final boolean isStarted() {
        return http_server != null && http_server.isStarted();
    }

    /**
     * Wait for current tasks to finish and stop the datastore server.
     */
    public final void stop() {
        activation_controller.awaitTermination();

        try {
            http_server.stop();

        } catch (Exception ex) {
            System.err.println(
                    "HTTP server failed to stop: " + ex.getMessage());
        }
    }


}
