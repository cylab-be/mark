package mark.datastore;

import com.mongodb.client.FindIterable;
import mark.core.ServerInterface;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import mark.activation.ActivationController;
import mark.core.Subject;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.SubjectAdapter;
import org.apache.ignite.cluster.ClusterMetrics;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 *
 * @author Thibault Debatty
 */
public class RequestHandler implements ServerInterface {

    private static final String COLLECTION_RAW_DATA = "RAW_DATA";
    private static final String COLLECTION_EVIDENCE = "EVIDENCE";

    private final MongoDatabase mongodb_database;
    private final ActivationController activation_controller;
    private final SubjectAdapter adapter;

    /**
     *
     * @param mongodb
     * @param mqclient
     */
    RequestHandler(
            final MongoDatabase mongodb_database,
            final ActivationController activation_controller,
            final SubjectAdapter adapter) {

        this.mongodb_database = mongodb_database;
        this.activation_controller = activation_controller;
        this.adapter = adapter;

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

        mongodb_database.getCollection(COLLECTION_RAW_DATA)
                .insertOne(convert(data));

        activation_controller.notifyRawData(data);
    }

    /**
     * {@inheritDoc}
     * @param label
     * @param subject
     * @return
     */
    public final RawData[] findRawData(
            final String label, final Subject subject) {

        Document query = new Document();
        query.append(LABEL, label);
        adapter.writeToMongo(subject, query);

        FindIterable<Document> documents = mongodb_database
                .getCollection(COLLECTION_RAW_DATA)
                .find(query);

        ArrayList<RawData> results = new ArrayList<RawData>();
        for (Document doc : documents) {
            results.add(convert(doc));
        }
        return results.toArray(new RawData[results.size()]);
    }

    /**
     *
     * @param evidence
     */
    public final void addEvidence(final Evidence evidence) {
        mongodb_database.getCollection(COLLECTION_EVIDENCE)
                .insertOne(convert(evidence));

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

    private static final String LABEL = "LABEL";
    private static final String TIME = "TIME";
    private static final String DATA = "DATA";
    private static final String SCORE = "SCORE";
    private static final String REPORT = "REPORT";

    /**
     * Convert from MongoDB document to RawData.
     * @param doc
     * @return
     */
    private RawData convert(final Document doc) {

        RawData data = new RawData();
        data.subject = adapter.readFromMongo(doc);
        data.data = doc.getString(DATA);
        data.time = doc.getInteger(TIME);
        data.label = doc.getString(LABEL);

        return data;

    }

    private Evidence convertEvidence(final Document doc) {

        Evidence evidence = new Evidence();
        evidence.subject = adapter.readFromMongo(doc);
        evidence.score = doc.getDouble(SCORE);
        evidence.time = doc.getInteger(TIME);
        evidence.label = doc.getString(LABEL);
        evidence.report = doc.getString(REPORT);
        evidence.id = doc.getObjectId("_id").toString();

        return evidence;

    }

    /**
     * Convert from RawData to MongoDB document.
     * @param data
     * @return
     */
    private Document convert(final RawData data) {

        Document doc = new Document()
                .append(LABEL, data.label)
                .append(TIME, data.time)
                .append(DATA, data.data);
        adapter.writeToMongo(data.subject, doc);
        return doc;
    }


    /**
     * Convert from Evidence to MongoDB document.
     * @param evidence
     * @return
     */
    private Document convert(final Evidence evidence) {
        Document doc = new Document()
                .append(LABEL, evidence.label)
                .append(TIME, evidence.time)
                .append(SCORE, evidence.score)
                .append(TIME, evidence.time)
                .append(REPORT, evidence.report);

        adapter.writeToMongo(evidence.subject, doc);
        return doc;
    }

    /**
     * {@inheritDoc}
     * @param label
     * @param subject
     * @return
     * @throws Throwable if request fails
     */
    public final Evidence[] findEvidence(
            final String label, final Subject subject)
            throws Throwable {

        Document query = new Document();
        query.append(LABEL, label);
        adapter.writeToMongo(subject, query);

        FindIterable<Document> documents = mongodb_database
                .getCollection(COLLECTION_EVIDENCE)
                .find(query);

        ArrayList<Evidence> results = new ArrayList<Evidence>();
        for (Document doc : documents) {
            results.add(convertEvidence(doc));
        }
        return results.toArray(new Evidence[results.size()]);
    }

    /**
     * Keep only one evidence per subject: the most recent one.
     *
     * @param label
     * @return
     * @throws Throwable if request fails
     */
    public final Evidence[] findEvidence(final String label)
            throws Throwable {

        Document query = new Document();
        query.append(LABEL, label);

        FindIterable<Document> documents = mongodb_database
                .getCollection(COLLECTION_EVIDENCE)
                .find(query);

        HashMap<Subject, Evidence> evidences = new HashMap<Subject, Evidence>();
        for (Document doc : documents) {
            Evidence evidence = convertEvidence(doc);

            Evidence inmap = evidences.get(evidence.subject);
            if (inmap == null) {
                evidences.put(evidence.subject, evidence);
                continue;
            }

            if (evidence.time > inmap.time) {
                evidences.put(evidence.subject, evidence);
            }
        }
        return evidences.values().toArray(new Evidence[evidences.size()]);
    }

    /**
     * Get a single evidence by id, or throw an exception if id is not valid.
     *
     * @param id
     * @return
     */
    public final Evidence findEvidenceById(final String id) {
        Document query = new Document();
        query.append("_id", new ObjectId(id));

        FindIterable<Document> documents = mongodb_database
                .getCollection(COLLECTION_EVIDENCE)
                .find(query);

        Document document = documents.first();

        if (document == null) {
            throw new IllegalArgumentException("Invalid id provided: " + id);
        }

        return convertEvidence(document);
    }

    public final ClusterMetrics igniteStatus() {
        return activation_controller.getIgniteMetrics();
    }
}
