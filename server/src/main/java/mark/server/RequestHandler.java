package mark.server;

import com.mongodb.client.FindIterable;
import mark.core.ServerInterface;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import mark.activation.ActivationController;
import mark.core.Evidence;
import mark.core.RawData;
import org.bson.Document;

/**
 *
 * @author Thibault Debatty
 */
public class RequestHandler implements ServerInterface {

    private static final String COLLECTION_RAW_DATA = "RAW_DATA";
    private static final String COLLECTION_EVIDENCE = "EVIDENCE";

    private final MongoDatabase mongodb_database;
    private final ActivationController activation_controller;

    /**
     *
     * @param mongodb
     * @param mqclient
     */
    RequestHandler(
            final MongoDatabase mongodb_database,
            final ActivationController activation_controller) {
        this.mongodb_database = mongodb_database;
        this.activation_controller = activation_controller;

    }

    /**
     * Should return the string 1.
     * @return
     */
    @Override
    public String test() {
        return "1";
    }

    public void testString(String data) {
        System.out.println(data);
    }

    /**
     * {@inheritDoc}
     * @param data  {@inheritDoc}
     */
    public final void addRawData(final RawData data) {

        mongodb_database.getCollection(COLLECTION_RAW_DATA)
                .insertOne(RawDataDocument.convert(data));

        activation_controller.notifyRawData(data);
    }

    public RawData[] findRawData(String type, String client, String server) {
        FindIterable<Document> documents = mongodb_database
                .getCollection(COLLECTION_RAW_DATA)
                .find(new Document(RawDataDocument.TYPE, type)
                        .append(RawDataDocument.CLIENT, client)
                        .append(RawDataDocument.SERVER, server));

        ArrayList<RawData> results = new ArrayList<RawData>();
        for (Document doc : documents) {
            results.add(RawDataDocument.convert(doc));
        }
        return results.toArray(new RawData[results.size()]);


    }

    public void addEvidence(Evidence evidence) {
        mongodb_database.getCollection(COLLECTION_EVIDENCE)
                .insertOne(EvidenceDocument.convert(evidence));

        // Do not notify of new evidence for now..
        //activation_controller.notifyEvidence(evidence);
    }
}

/**
 * Helper class for converting between Evidence and MongoDB Document
 * @author Thibault Debatty
 */
class EvidenceDocument {
    public static final String AGENT = "AGENT";
    public static final String TIME = "TIME";
    public static final String CLIENT = "CLIENT";
    public static final String SCORE = "SCORE";
    public static final String SERVER = "SERVER";
    public static final String REPORT = "REPORT";

    static Document convert(Evidence evidence) {
        return new Document()
                .append(AGENT, evidence.agent)
                .append(TIME, evidence.time)
                .append(CLIENT, evidence.client)
                .append(SCORE, evidence.score)
                .append(SERVER, evidence.server)
                .append(TIME, evidence.time);
    }
}

/**
 * Helper class for converting between RawData and MongoDB Document
 * @author Thibault Debatty
 */
class RawDataDocument {
    public static final String TYPE = "TYPE";
    public static final String TIME = "TIME";
    public static final String CLIENT = "CLIENT";
    public static final String SERVER = "SERVER";
    public static final String DATA = "DATA";

    static RawData convert(final Document doc) {
        RawData data = new RawData();
        data.client = doc.getString(CLIENT);
        data.data = doc.getString(DATA);
        data.server = doc.getString(SERVER);
        data.time = doc.getInteger(TIME);
        data.type = doc.getString(TYPE);

        return data;

    }

    static Document convert(final RawData data) {

        return new Document()
                .append(TYPE, data.type)
                .append(TIME, data.time)
                .append(CLIENT, data.client)
                .append(SERVER, data.server)
                .append(DATA, data.data);
    }
}