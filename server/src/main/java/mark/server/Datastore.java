package mark.server;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import mark.activation.ActivationController;
import mark.activation.ActivationProfile;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 *
 * @author Thibault Debatty
 */
public class Datastore implements Runnable {


    private MasfadConfig config;
    private Server http_server;
    private final ActivationController activation_controller;

    /**
     * Instatiate a datastore with default config and empty activation profiles.
     */
    public Datastore() {
        this.config = new MasfadConfig();
        this.activation_controller = new ActivationController();
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

    public void setActivationProfiles(InputStream profiles) throws Exception  {
        activation_controller.setProfiles(profiles);
    }

    public void setConfiguration(MasfadConfig config) {
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
        }
    }


}
