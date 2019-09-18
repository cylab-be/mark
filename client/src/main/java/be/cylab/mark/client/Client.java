package be.cylab.mark.client;

import be.cylab.mark.core.DetectionAgentProfile;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import be.cylab.mark.core.Subject;
import be.cylab.mark.core.ServerInterface;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import be.cylab.mark.core.SubjectAdapter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 *
 * @author Thibault Debatty
 * @param <T>
 */
public class Client<T extends Subject> implements ServerInterface {

    private static final int CONNECTION_TIMEOUT = 5000;

    private final JsonRpcHttpClient datastore;
    private final URL server_url;

    /**
     * Create a connection to server with provided URL, and test the connection.
     * So we can directly throw an exception if connection failed...
     *
     * @param server_url
     * @param adapter
     */
    public Client(final URL server_url, final SubjectAdapter adapter) {

        this.server_url = server_url;

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(
                RawData.class, new RawDataDezerializer(adapter));
        module.addDeserializer(
                Evidence.class, new EvidenceDeserializer(adapter));
        mapper.registerModule(module);

        datastore
                = new JsonRpcHttpClient(
                        mapper, server_url, new HashMap<>());
        datastore.setConnectionTimeoutMillis(CONNECTION_TIMEOUT);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String test() throws Throwable {
        return datastore.invoke("test", null, String.class);

    }

    /**
     * {@inheritDoc}
     *
     * @param data {@inheritDoc}
     */
    @Override
    public final void addRawData(final RawData data) throws Throwable {

        datastore.invoke("addRawData", new Object[]{data});

    }

    /**
     * {@inheritDoc}
     *
     * @param bytes {@inheritDoc}
     */
    public final ObjectId addFile(final byte[] bytes, final String filename)
            throws Throwable {
        return datastore.invoke(
                "addFile", new Object[]{bytes, filename}, ObjectId.class);
    }

    /**
     * {@inheritDoc}
     *
     * @param file_id {@inheritDoc}
     */
    @Override
    public final byte[] findFile(final ObjectId file_id) throws Throwable {
        return datastore.invoke(
                "findFile", new Object[]{file_id}, byte[].class);
    }

    /**
     * {@inheritDoc}
     *
     * @param data {@inheritDoc}
     */
    @Override
    public final void testString(final String data) throws Throwable {

        datastore.invoke("testString", new Object[]{data});
    }

    @Override
    public final RawData[] findData(final Document query) throws Throwable {
        return datastore.invoke(
                "findData",
                new Object[]{query},
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
    public final RawData[] findRawData(
            final String label, final Subject subject)
            throws Throwable {

        return datastore.invoke(
                "findRawData",
                new Object[]{label, subject},
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
            final String label, final Subject subject)
            throws Throwable {

        Evidence[] evidences = datastore.invoke(
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

        return datastore.invoke(
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

        datastore.invoke("addEvidence", new Object[]{evidence});
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

        return datastore.invoke(
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

        return datastore.invoke(
                "findEvidence",
                new Object[]{label, page},
                Evidence[].class);
    }

    @Override
    public final Evidence[] findLastEvidences(
            final String label, final Subject subject)
            throws Throwable {
        return datastore.invoke(
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
        return datastore.invoke(
                "getFromCache", new Object[]{key}, Object.class);
    }

    @Override
    public final void storeInCache(final String key, final Object value)
            throws Throwable {
        datastore.invoke(
                "storeInCache", new Object[]{key, value}, Object.class);
    }

    @Override
    public final boolean compareAndSwapInCache(
            final String key, final Object new_value, final Object old_value)
            throws Throwable {
        return datastore.invoke(
                "compareAndSwapInCache",
                new Object[]{key, new_value, old_value}, Boolean.class);
    }


    /**
     *
     * @return
     * @throws Throwable if anything goes wrong
     */
    public final DetectionAgentProfile[] activation() throws Throwable {
        return datastore.invoke(
                "activation", null, DetectionAgentProfile[].class);
    }

    @Override
    public final Map executorStatus() throws Throwable {
        return datastore.invoke("executorStatus", null, Map.class);
    }

    @Override
    public final Evidence[] findEvidenceSince(
            final String label, final Subject subject, final long time)
            throws Throwable {

        Evidence[] evidences = datastore.invoke(
                "findEvidenceSince",
                new Object[]{label, subject, time},
                Evidence[].class);

        Arrays.sort(evidences, new EvidenceTimeComparator());

        return evidences;
    }

    /**
     * Helper class to deserialize raw data, using the subject adapter.
     *
     * @param <T>
     */
    private static class RawDataDezerializer<T extends Subject>
            extends JsonDeserializer<RawData> {

        private final SubjectAdapter<T> adapter;

        RawDataDezerializer(final SubjectAdapter<T> adapter) {
            this.adapter = adapter;
        }

        @Override
        public RawData deserialize(
                final JsonParser jparser,
                final DeserializationContext context)
                throws IOException, JsonProcessingException {

            TreeNode tree = jparser.getCodec().readTree(jparser);
            RawData<T> data = new RawData<>();
            data.setData(((TextNode) tree.get("data")).asText());
            data.setLabel(((TextNode) tree.get("label")).asText());
            data.setTime(((NumericNode) tree.get("time")).asLong());
            data.setSubject(
                    adapter.deserialize((JsonNode) tree.get("subject")));

            return data;
        }
    }

    /**
     * Helper class to deserialize evidence, using subject adapter.
     *
     * @param <T>
     */
    private static class EvidenceDeserializer
            extends JsonDeserializer<Evidence> {

        private final SubjectAdapter adapter;

        EvidenceDeserializer(final SubjectAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public Evidence deserialize(
                final JsonParser jparser,
                final DeserializationContext ctx)
                throws IOException, JsonProcessingException {

            TreeNode tree = jparser.getCodec().readTree(jparser);

            Evidence ev = new Evidence();
            ev.setId(((TextNode) tree.get("id")).asText());
            ev.setReport(((TextNode) tree.get("report")).asText());
            ev.setLabel(((TextNode) tree.get("label")).asText());
            ev.setScore(((NumericNode) tree.get("score")).asDouble());
            ev.setTime(((NumericNode) tree.get("time")).asLong());
            ev.setReferences(
                    deserializeList(((ArrayNode) tree.get("references"))));

            ev.setSubject(
                    adapter.deserialize((JsonNode) tree.get("subject")));

            return ev;
        }

        private List<String> deserializeList(final ArrayNode node) {
            List<String> values = new ArrayList<>();
            for (JsonNode element : node) {
                values.add(element.asText());
            }
            return values;
        }
    }
}
