package be.cylab.mark.datastore;

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
import be.cylab.mark.activation.ActivationControllerInterface;
import be.cylab.mark.core.InvalidProfileException;
import be.cylab.mark.core.ServerInterface;
import be.cylab.mark.core.Subject;
import be.cylab.mark.server.Config;
import be.cylab.mark.core.SubjectAdapter;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thibault Debatty
 */
public class Datastore {

    private static final int STARTUP_DELAY = 100;

    private static final org.slf4j.Logger LOGGER
            = LoggerFactory.getLogger(Datastore.class);

    private Server server;
    private final Config config;
    private final ActivationControllerInterface activation_controller;
    private ServerInterface request_handler;
    private MongoDatabase mongodb;

    /**
     *
     * @param config
     * @param activation_controller
     */
    @Inject
    public Datastore(
            final Config config,
            final ActivationControllerInterface activation_controller) {

        this.config = config;
        this.activation_controller = activation_controller;
    }

    /**
     * Start the datastore.This will start the json-rpc server in a separate
     * thread and return when the server is ready.
     *
     * @throws be.cylab.mark.core.InvalidProfileException if the adapter class
     * mentioned in the configuration is invalid
     * @throws java.lang.InterruptedException if we were stopped
     * @throws Exception if the server failed to start
     */
    public final void start()
            throws InvalidProfileException, InterruptedException, Exception {

        LOGGER.info("Starting JSON-RPC datastore on " + config.getServerHost()
                + " : " + config.getServerPort());

        mongodb = this.connectToMongodb(config);
        server = this.createJsonRPCServer(mongodb);
        server.start();

        while (!server.isStarted()) {
            Thread.sleep(STARTUP_DELAY);
        }

        LOGGER.info("Datastore started...");
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

    private MongoDatabase connectToMongodb(final Config config) {
        // Connect to mongodb
        MongoClient mongo = new MongoClient(
                config.getMongoHost(), config.getMongoPort());
        MongoDatabase db = mongo.getDatabase(config.getMongoDb());

        if (config.isMongoClean()) {
            db.drop();
        }

        return db;
    }

    private Server createJsonRPCServer(final MongoDatabase mongodb)
            throws InvalidProfileException {

        request_handler = new RequestHandler(
                mongodb,
                activation_controller,
                config.getSubjectAdapter(),
                new MongoParser(config.getSubjectAdapter()));

        ObjectMapper object_mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(
                Subject.class,
                new SubjectDeserializer<>(config.getSubjectAdapter()));
        object_mapper.registerModule(module);

        JsonRpcServer jsonrpc_server
                = new JsonRpcServer(object_mapper, request_handler);

        QueuedThreadPool thread_pool = new QueuedThreadPool(
                config.getMaxThreads(),
                config.getMinThreads(),
                config.getIdleTimeout(),
                new ArrayBlockingQueue<>(config.getMaxPendingRequests()));

        Server jetty = new Server(thread_pool);

        ServerConnector http_connector = new ServerConnector(jetty);
        http_connector.setHost(config.getServerBind());
        http_connector.setPort(config.getServerPort());

        jetty.setConnectors(new Connector[]{http_connector});
        jetty.setHandler(new JettyHandler(jsonrpc_server));

        return jetty;
    }

    /**
     *
     * @return
     */
    public final ServerInterface getRequestHandler() {
        return this.request_handler;
    }

    /**
     *
     * @return
     */
    public final MongoDatabase getMongodb() {
        return this.mongodb;
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
