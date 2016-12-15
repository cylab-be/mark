package mark.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import mark.core.Subject;
import mark.core.ServerInterface;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.SubjectAdapter;

/**
 *
 * @author Thibault Debatty
 */
public class Client<T extends Subject> implements ServerInterface<T> {

    private JsonRpcHttpClient datastore;

    /**
     * Create a connection to server with provided URL, and test the connection.
     * So we can directly throw an exception if connection failed...
     *
     * @param server_url
     */
    public Client(final URL server_url, final SubjectAdapter<T> adapter) {


        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(RawData.class, new RawDataDezerializer(adapter));
        module.addDeserializer(Evidence.class, new EvidenceDeserializer(adapter));
        mapper.registerModule(module);

        datastore = new JsonRpcHttpClient(mapper, server_url, new HashMap<String, String>());
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
    public final void addRawData(final RawData<T> data) throws Throwable {

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

    public final RawData<T>[] findRawData(
            final String label, final T subject)
            throws Throwable {

        System.out.println("Client : search " + subject);
        return datastore.invoke(
                "findRawData",
                new Object[]{label, subject},
                RawData[].class);
    }

    public final Evidence<T>[] findEvidence(
            final String label, final T subject)
            throws Throwable {

        return datastore.invoke(
                "findEvidence",
                new Object[]{label, subject},
                Evidence[].class);

    }

    public final void addEvidence(final Evidence evidence) throws Throwable {

        datastore.invoke("addEvidence", new Object[]{evidence});
    }

    private static class RawDataDezerializer<T extends Subject> extends JsonDeserializer<RawData> {
        private SubjectAdapter<T> adapter;

        public RawDataDezerializer(SubjectAdapter<T> adapter) {
            this.adapter = adapter;
        }

        @Override
        public RawData deserialize(
                JsonParser jparser,
                DeserializationContext context)
                throws IOException, JsonProcessingException {

            TreeNode tree = jparser.getCodec().readTree(jparser);
            RawData<T> data = new RawData<T>();
            data.data = tree.get("data").toString();
            data.label = tree.get("label").toString();
            data.subject = adapter.deserialize((JsonNode) tree.get("subject"));
            data.time = Integer.valueOf(tree.get("time").toString());

            return data;
        }
    }

    private static class EvidenceDeserializer<T extends Subject> extends JsonDeserializer<Evidence> {
        private SubjectAdapter<T> adapter;

        public EvidenceDeserializer(SubjectAdapter<T> adapter) {
            this.adapter = adapter;
        }

        @Override
        public Evidence deserialize(JsonParser jparser, DeserializationContext arg1) throws IOException, JsonProcessingException {
            TreeNode tree = jparser.getCodec().readTree(jparser);
            Evidence<T> data = new Evidence<T>();
            data.report = tree.get("report").toString();
            data.score = Double.valueOf(tree.get("score").toString());
            data.label = tree.get("label").toString();
            data.subject = adapter.deserialize((JsonNode) tree.get("subject"));
            data.time = Integer.valueOf(tree.get("time").toString());

            return data;
        }
    }

}
