package mark.server;

import com.mongodb.client.FindIterable;
import mark.core.ServerInterface;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mark.activation.ActivationController;
import mark.core.AnalysisUnit;
import mark.core.Evidence;
import mark.core.Link;
import mark.core.RawData;
import org.bson.Document;

/**
 *
 * @author Thibault Debatty
 */
public class RequestHandler<T extends AnalysisUnit> implements ServerInterface<T> {

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
    public final String test() {
        return "1";
    }

    /**
     * Test RPC method that accepts a single string parameter.
     * Do nothing but print string on screen.
     * @param data
     */
    public final void testString(final String data) {
        System.out.println(data);
    }

    /**
     * {@inheritDoc}
     * @param data  {@inheritDoc}
     */
    public final void addRawData(final RawData data) {

        Logger.getLogger(RequestHandler.class.getName()).log(Level.INFO, data.toString());

        mongodb_database.getCollection(COLLECTION_RAW_DATA)
                .insertOne(RawDataDocument.convert(data));

        activation_controller.notifyRawData(data);
    }

    /**
     *
     * @param label
     * @param subject
     * @return
     */
    public final RawData[] findRawData(
            final String label, final Link subject) {

        Document query = new Document();
        query.append(RawDataDocument.LABEL, label);
        subject.writeToMongo(query);

        FindIterable<Document> documents = mongodb_database
                .getCollection(COLLECTION_RAW_DATA)
                .find(query);

        ArrayList<RawData> results = new ArrayList<RawData>();
        for (Document doc : documents) {
            results.add(RawDataDocument.convert(doc));
        }
        return results.toArray(new RawData[results.size()]);


    }

    /**
     *
     * @param evidence
     */
    public final void addEvidence(final Evidence evidence) {
        mongodb_database.getCollection(COLLECTION_EVIDENCE)
                .insertOne(EvidenceDocument.convert(evidence));

        activation_controller.notifyEvidence(evidence);
    }

    /**
     *
     * @return
     */
    public final  Map<String, Object> status() {
        HashMap<String, Object> status = new HashMap<String, Object>();
        status.put("state", "running");
        status.put("activation", activation_controller.getProfiles());
        status.put("executed", activation_controller.getTaskCount());
        return status;
    }
}

/**
 * Helper class for converting between Evidence and MongoDB Document.
 * @author Thibault Debatty
 */
final class EvidenceDocument {
    public static final String LABEL = "LABEL";
    public static final String TIME = "TIME";
    public static final String SCORE = "SCORE";
    public static final String REPORT = "REPORT";

    static Document convert(final Evidence evidence) {
        Document doc = new Document()
                .append(LABEL, evidence.label)
                .append(TIME, evidence.time)
                .append(SCORE, evidence.score)
                .append(TIME, evidence.time);

        evidence.subject.writeToMongo(doc);
        return doc;
    }

    private EvidenceDocument() {

    }
}

/**
 * Helper class for converting between RawData and MongoDB Document.
 * @author Thibault Debatty
 */
final class RawDataDocument {
    public static final String LABEL = "LABEL";
    public static final String TIME = "TIME";
    public static final String DATA = "DATA";

    static RawData convert(final Document doc) {

        Link subject = new Link();
        subject.readFromMongo(doc);

        RawData data = new RawData();
        data.subject = subject;
        data.data = doc.getString(DATA);
        data.time = doc.getInteger(TIME);
        data.label = doc.getString(LABEL);

        return data;

    }

    static Document convert(final RawData data) {

        Document doc = new Document()
                .append(LABEL, data.label)
                .append(TIME, data.time)
                .append(DATA, data.data);

        data.subject.writeToMongo(doc);
        return doc;

    }

    private RawDataDocument() {

    }
}