package mark.datastore;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Inject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import mark.activation.ActivationControllerInterface;
import mark.core.Subject;
import mark.server.Config;
import mark.core.InvalidProfileException;
import mark.core.SubjectAdapter;
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
    @Inject
    public Datastore(
            final Config config,
            final ActivationControllerInterface activation_controller)
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
                config.getSubjectAdapter()); //HERE IS PROBLEM

        ObjectMapper object_mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(
                Subject.class,
                new SubjectDeserializer<>(config.getSubjectAdapter()));
        object_mapper.registerModule(module);

        JsonRpcServer jsonrpc_server
                = new JsonRpcServer(object_mapper, datastore_handler);

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
     * Start the datastore. This will start the json-rpc server in a separate
     * thread and return when the server is ready.
     *
     * @throws Exception
     */
    public final void start() throws Exception {

        server.start();

        while (!server.isStarted()) {
            Thread.sleep(STARTUP_DELAY);
        }
    }

    /**
     * Stop the datastore.
     *
     * @throws Exception if jetty fails to stop.
     */
    public final void stop() throws Exception {
        if (server == null) {
            return;
        }

        server.stop();
    }
}

/**
 * JSON Deserializer that can be provided to the JSON-RPC server.
 *
 * @author Thibault Debatty
 * @param <T>
 */
class SubjectDeserializer<T extends Subject> extends JsonDeserializer<T> {

    private final SubjectAdapter<T> adapter;

    SubjectDeserializer(final SubjectAdapter<T> adapter) {
        super();
        this.adapter = adapter;
    }

    @Override
    public T deserialize(
            final JsonParser jparser,
            final DeserializationContext context)
            throws IOException, JsonProcessingException {

        JsonNode tree = jparser.getCodec().readTree(jparser);
        return adapter.deserialize(tree);
    }

}
