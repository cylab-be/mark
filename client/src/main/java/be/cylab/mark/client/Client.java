package be.cylab.mark.client;

import be.cylab.mark.core.DetectionAgentProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import java.net.URL;
import java.util.HashMap;
import be.cylab.mark.core.ServerInterface;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.jsonrpc4j.JsonRpcClient;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;

/**
 *
 * @author Thibault Debatty
 */
public class Client implements ServerInterface {

    private static final int CONNECTION_TIMEOUT = 5000;

    private final JsonRpcHttpClient json_rpc_client;
    private final URL server_url;

    /**
     * Create a connection to server with provided URL, and test the connection.
     * So we can directly throw an exception if connection failed...
     *
     * @param server_url
     */
    public Client(final URL server_url) {

        this.server_url = server_url;
        json_rpc_client = new JsonRpcHttpClient(server_url);
        json_rpc_client.setConnectionTimeoutMillis(CONNECTION_TIMEOUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String test() throws Throwable {
        return json_rpc_client.invoke("test", null, String.class);

    }

    /**
     * {@inheritDoc}
     *
     * @param data {@inheritDoc}
     */
    @Override
    public final void addRawData(final RawData data) throws Throwable {

        json_rpc_client.invoke("addRawData", new Object[]{data});

    }

    /**
     * {@inheritDoc}
     *
     * @param bytes {@inheritDoc}
     */
    @Override
    public final ObjectId addFile(final byte[] bytes, final String filename)
            throws Throwable {
        return json_rpc_client.invoke(
                "addFile", new Object[]{bytes, filename}, ObjectId.class);
    }

    /**
     * {@inheritDoc}
     *
     * @param file_id {@inheritDoc}
     */
    @Override
    public final byte[] findFile(final ObjectId file_id) throws Throwable {
        return json_rpc_client.invoke(
                "findFile", new Object[]{file_id}, byte[].class);
    }

    /**
     * {@inheritDoc}
     *
     * @param data {@inheritDoc}
     */
    @Override
    public final void testString(final String data) throws Throwable {

        json_rpc_client.invoke("testString", new Object[]{data});
    }

    /**
     * Allow to pass a json string to search RawData.
     *
     * @param json_query
     * @return
     * @throws Throwable if something went wrong...
     */
    public final RawData[] findData(final String json_query) throws Throwable {

        ObjectNode node = new ObjectMapper().readValue(
                json_query, ObjectNode.class);
        return json_rpc_client.invoke(node, RawData[].class, new HashMap<>());
    }

    /**
     * {@inheritDoc}
     *
     * @param label
     * @param subject
     * @return
     * @throws Throwable
     */
    @Override
    public final RawData[] findRawData(
            final String label, final Map<String, String> subject,
            final long from, final long till) throws Throwable {

        return json_rpc_client.invoke(
                "findRawData",
                new Object[]{label, subject, from, till},
                RawData[].class);
    }

    /**
     * {@inheritDoc}
     *
     * @param label
     * @param subject
     * @return
     * @throws Throwable
     */
    @Override
    public final Evidence[] findEvidence(
            final String label, final Map<String, String> subject)
            throws Throwable {

        Evidence[] evidences = json_rpc_client.invoke(
                "findEvidence",
                new Object[]{label, subject},
                Evidence[].class);

        Arrays.sort(evidences, new EvidenceTimeComparator());

        return evidences;
    }

    /**
     * Find a single evidence by id or throw an exception (if id is invalid).
     *
     * @param id
     * @return
     * @throws Throwable
     */
    @Override
    public final Evidence findEvidenceById(final String id) throws Throwable {

        return json_rpc_client.invoke(
                "findEvidenceById",
                new Object[]{id},
                Evidence.class);
    }

    /**
     * {@inheritDoc}
     *
     * @param evidence
     * @throws Throwable
     */
    @Override
    public final void addEvidence(final Evidence evidence) throws Throwable {

        json_rpc_client.invoke("addEvidence", new Object[]{evidence});
    }

    /**
     * {@inheritDoc}
     *
     * @param label
     * @return
     * @throws Throwable
     */
    @Override
    public final Evidence[] findEvidence(final String label)
            throws Throwable {

        return json_rpc_client.invoke(
                "findEvidence",
                new Object[]{label},
                Evidence[].class);
    }

    /**
     * {@inheritDoc}
     *
     * @param label
     * @param page
     * @return
     * @throws Throwable
     */
    @Override
    public final Evidence[] findEvidence(final String label, final int page)
            throws Throwable {

        return json_rpc_client.invoke(
                "findEvidence",
                new Object[]{label, page},
                Evidence[].class);
    }

    @Override
    public final Evidence[] findLastEvidences(
            final String label, final Map<String, String> subject)
            throws Throwable {
        return json_rpc_client.invoke(
                "findLastEvidences",
                new Object[]{label, subject},
                Evidence[].class);
    }

    @Override
    public final URL getURL() {
        return this.server_url;
    }

    @Override
    public final Object getFromCache(final String key) throws Throwable {
        return json_rpc_client.invoke(
                "getFromCache", new Object[]{key}, Object.class);
    }

    @Override
    public final void storeInCache(final String key, final Object value)
            throws Throwable {
        json_rpc_client.invoke(
                "storeInCache", new Object[]{key, value}, Object.class);
    }

    @Override
    public final boolean compareAndSwapInCache(
            final String key, final Object new_value, final Object old_value)
            throws Throwable {
        return json_rpc_client.invoke(
                "compareAndSwapInCache",
                new Object[]{key, new_value, old_value}, Boolean.class);
    }


    /**
     *
     * @return
     * @throws Throwable if anything goes wrong
     */
    @Override
    public final DetectionAgentProfile[] activation() throws Throwable {
        return json_rpc_client.invoke(
                "activation", null, DetectionAgentProfile[].class);
    }

    @Override
    public final void setAgentProfile(final DetectionAgentProfile profile)
            throws Throwable {

        json_rpc_client.invoke(
                "setAgentProfile",
                new Object[]{profile});
    }

    @Override
    public final Evidence[] findEvidenceSince(
            final String label, final Map<String, String> subject,
            final long time)
            throws Throwable {

        Evidence[] evidences = json_rpc_client.invoke(
                "findEvidenceSince",
                new Object[]{label, subject, time},
                Evidence[].class);

        Arrays.sort(evidences, new EvidenceTimeComparator());

        return evidences;
    }

    /**
     * Get the internal json_rpc_client.
     *
     * Can be used to set a listener...
     *
     * @return
     */
    public final JsonRpcClient getJsonRpcClient() {
        return this.json_rpc_client;
    }


    @Override
    public final void pause() throws Throwable {
        json_rpc_client.invoke("pause", null);
    }

    @Override
    public final void resume() throws Throwable {
        json_rpc_client.invoke("resume", null);
    }

    @Override
    public final Map status() throws Throwable {
        return json_rpc_client.invoke("status", null, Map.class);
    }

    @Override
    public final List<Map> history() throws Throwable {
        return json_rpc_client.invoke("history", null, List.class);
    }

    @Override
    public final void reload() throws Throwable {
        json_rpc_client.invoke("reload", null);
    }

    @Override
    public final RawData[] findLastRawData() throws Throwable {
        return json_rpc_client.invoke("findLastRawData", null, RawData[].class);
    }

    @Override
    public final Evidence[] findLastEvidences() throws Throwable {
        return json_rpc_client.invoke(
                "findLastEvidences", null, Evidence[].class);
    }
}
