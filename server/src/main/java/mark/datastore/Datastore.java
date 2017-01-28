package mark.datastore;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.util.concurrent.ArrayBlockingQueue;
import mark.activation.ActivationController;
import mark.core.Subject;
import mark.server.Config;
import mark.server.InvalidProfileException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 *
 * @author Thibault Debatty
 */
public class Datastore {

    private static final int STARTUP_DELAY = 100;

    private final Server server;

    /**
     *
     * @param config
     * @param activation_controller
     * @throws mark.server.InvalidProfileException
     */
    public Datastore(
            final Config config,
            final ActivationController activation_controller)
            throws InvalidProfileException {

        // Connect to mongodb
        MongoClient mongo = new MongoClient(
                config.mongo_host, config.mongo_port);
        MongoDatabase mongodb = mongo.getDatabase(config.mongo_db);

        if (config.mongo_clean) {
            mongodb.drop();
        }

        // Create and run HTTP / JSON-RPC server
        RequestHandler datastore_handler = new RequestHandler(
                mongodb,
                activation_controller,
                config.getSubjectAdapter());

        ObjectMapper object_mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Subject.class, config.getSubjectAdapter());
        object_mapper.registerModule(module);

        JsonRpcServer jsonrpc_server =
                new JsonRpcServer(object_mapper, datastore_handler);

        QueuedThreadPool thread_pool = new QueuedThreadPool(
                config.max_threads,
                config.min_threads,
                config.idle_timeout,
                new ArrayBlockingQueue<Runnable>(config.max_pending_requests));

        server = new Server(thread_pool);

        ServerConnector http_connector = new ServerConnector(server);
        http_connector.setHost(config.server_host);
        http_connector.setPort(config.server_port);

        server.setConnectors(new Connector[]{http_connector});
        server.setHandler(new JettyHandler(jsonrpc_server));
    }

    /**
     * Start the datastore.
     * This will start the json-rpc server in a separate thread and return
     * when the server is ready.
     * @throws Exception
     */
    public final void start() throws Exception  {

        server.start();

        while (!server.isStarted()) {
            Thread.sleep(STARTUP_DELAY);
        }
    }

    /**
     * Stop the datastore.
     * @throws Exception if jetty fails to stop.
     */
    public final void stop() throws Exception {
        if (server == null) {
            return;
        }

        server.stop();
    }
}
