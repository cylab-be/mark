package mark.client;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import java.net.MalformedURLException;
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
     * Instantiate a connection to datastore server with default URL (localhost
     * and port 8080).
     */
    public Client() {
        try {
            datastore = new JsonRpcHttpClient(
                    new URL("http://127.0.0.1:8080"));
        } catch (MalformedURLException ex) {
            System.err.println("URL of datastore server is not valid!");
            System.exit(1);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final String test() {
        try {
            return datastore.invoke("test", null, String.class);

        } catch (Throwable ex) {
            System.err.println("test failed: " + ex.getMessage());
        }

        return null;
    }

    /**
     * {@inheritDoc}
     * @param data {@inheritDoc}
     */
    public final void addRawData(final RawData data) {
        try {
            datastore.invoke("addRawData", new Object[]{data});
        } catch (Throwable ex) {
            System.err.println("addRawData failed: " + ex.getMessage());
        }

    }

    /**
     * {@inheritDoc}
     * @param data {@inheritDoc }
     */
    public final void testString(final String data) {
        try {
            datastore.invoke("testString", new Object[]{data});
        } catch (Throwable ex) {
            System.err.println("testString failed: " + ex.getMessage());
        }
    }

    public final RawData[] findRawData(String type, String client, String server) {
        try {
            return datastore.invoke(
                    "findRawData",
                    new Object[]{type, client, server},
                    RawData[].class);

        } catch (Throwable ex) {
            System.err.println("findRawData failed: " + ex.getMessage());
            return new RawData[0];
        }

    }

    public void addEvidence(Evidence evidence) {
        try {
            datastore.invoke("addEvidence", new Object[]{evidence});

        } catch (Throwable ex) {
            System.err.println("addEvidence failed: " + ex.getMessage());
        }
    }

}
