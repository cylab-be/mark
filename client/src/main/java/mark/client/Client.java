package mark.client;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import java.net.URL;
import mark.core.ServerInterface;
import mark.core.Evidence;
import mark.core.RawData;

/**
 *
 * @author Thibault Debatty
 */
public class Client implements ServerInterface {

    private JsonRpcHttpClient datastore;

    /**
     * Create a connection to server with provided URL, and test the connection.
     * So we can directly throw an exception if connection failed...
     *
     * @param server_url
     */
    public Client(final URL server_url) {

        datastore = new JsonRpcHttpClient(server_url);
        datastore.setConnectionTimeoutMillis(5000);
    }

    /**
     * {@inheritDoc}
     */
    public final String test() throws Throwable {
        return datastore.invoke("test", null, String.class);

    }

    /**
     * {@inheritDoc}
     *
     * @param data {@inheritDoc}
     */
    public final void addRawData(final RawData data) throws Throwable {

        datastore.invoke("addRawData", new Object[]{data});

    }

    /**
     * {@inheritDoc}
     *
     * @param data {@inheritDoc}
     */
    public final void testString(final String data) throws Throwable {

        datastore.invoke("testString", new Object[]{data});
    }

    public final RawData[] findRawData(
            final String type, final String client, final String server)
            throws Throwable {

        return datastore.invoke(
                "findRawData",
                new Object[]{type, client, server},
                RawData[].class);

    }

    public final void addEvidence(final Evidence evidence) throws Throwable {

        datastore.invoke("addEvidence", new Object[]{evidence});
    }

}
