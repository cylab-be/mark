package be.cylab.mark.datastore;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.google.inject.Inject;
import com.mongodb.client.MongoDatabase;
import java.util.concurrent.ArrayBlockingQueue;
import be.cylab.mark.core.InvalidProfileException;
import be.cylab.mark.core.ServerInterface;
import be.cylab.mark.server.Config;
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

    private Server jetty;
    private final Config config;
    private final RequestHandler request_handler;
    private final MongoDatabase mongodb;

    /**
     *
     * @param config
     * @param request_handler
     * @param mongodb
     */
    @Inject
    public Datastore(
            final Config config,
            final RequestHandler request_handler,
            final MongoDatabase mongodb) {

        this.config = config;
        this.request_handler = request_handler;
        this.mongodb = mongodb;
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

        // create jsonrpc server
        JsonRpcServer jsonrpc_server
                = new JsonRpcServer(request_handler);

        QueuedThreadPool thread_pool = new QueuedThreadPool(
                config.getMaxThreads(),
                config.getMinThreads(),
                config.getIdleTimeout(),
                new ArrayBlockingQueue<>(config.getMaxPendingRequests()));

        jetty = new Server(thread_pool);

        ServerConnector http_connector = new ServerConnector(jetty);
        http_connector.setHost(config.getServerBind());
        http_connector.setPort(config.getServerPort());

        jetty.setConnectors(new Connector[]{http_connector});
        jetty.setHandler(new JettyHandler(jsonrpc_server));
        jetty.start();

        while (!jetty.isStarted()) {
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
        if (jetty == null) {
            return;
        }

        jetty.stop();
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
