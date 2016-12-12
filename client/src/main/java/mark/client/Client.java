package mark.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
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

        JsonDeserializer<RawData> deserializer = new JsonDeserializer<RawData>() {

            @Override
            public RawData deserialize(
                    JsonParser jparser,
                    DeserializationContext context)
                    throws IOException, JsonProcessingException {


                TreeNode tree = jparser.getCodec().readTree(jparser);
                RawData<T> data = new RawData<T>();
                data.data = tree.get("data").toString();
                data.label = tree.get("label").toString();
                data.subject = adapter.deserialize(tree.get("subject"));
                data.time = Integer.valueOf(tree.get("time").toString());

                return data;
            }
        };

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(RawData.class, deserializer);
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

        return datastore.invoke(
                "findRawData",
                new Object[]{label, subject},
                RawData[].class);
    }

    public final void addEvidence(final Evidence evidence) throws Throwable {

        datastore.invoke("addEvidence", new Object[]{evidence});
    }

}
