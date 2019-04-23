package mark.datastore;

import com.mongodb.client.FindIterable;
import mark.core.ServerInterface;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import mark.activation.ActivationControllerInterface;
import mark.core.Subject;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.SubjectAdapter;
import org.apache.ignite.cluster.ClusterMetrics;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thibault Debatty
 */
public class RequestHandler implements ServerInterface {

    private static final String COLLECTION_DATA = "DATA";
    private static final String COLLECTION_EVIDENCE = "EVIDENCE";
    private static final String COLLECTION_FILES = "FILES";

    private static final Logger LOGGER
            = LoggerFactory.getLogger(RequestHandler.class);

    private final MongoDatabase mongodb;
    private final GridFSBucket gridfsbucket;
    private final ActivationControllerInterface activation_controller;
    private final SubjectAdapter adapter;

    //Cache
    private final HashMap<String, Object> agents_cache;

    /**
     *
     * @param mongodb
     * @param mqclient
     */
    public RequestHandler(
            final MongoDatabase mongodb,
            final ActivationControllerInterface activation_controller,
            final SubjectAdapter adapter) {

        this.agents_cache = new HashMap();
        this.mongodb = mongodb;
        this.activation_controller = activation_controller;
        this.adapter = adapter;
        this.gridfsbucket = GridFSBuckets.create(mongodb, COLLECTION_FILES);

        // Create indexes for LABEL and TIME
        Document index = new Document(LABEL, 1);
        mongodb.getCollection(COLLECTION_DATA).createIndex(index);
        mongodb.getCollection(COLLECTION_EVIDENCE).createIndex(index);

        index = new Document(TIME, 1);
        mongodb.getCollection(COLLECTION_DATA).createIndex(index);
        mongodb.getCollection(COLLECTION_EVIDENCE).createIndex(index);

        // Create indexes for CLIENT and SERVER
        index = new Document(CLIENT, 1);
        mongodb.getCollection(COLLECTION_DATA).createIndex(index);
        mongodb.getCollection(COLLECTION_EVIDENCE).createIndex(index);

        index = new Document(SERVER, 1);
        mongodb.getCollection(COLLECTION_DATA).createIndex(index);
        mongodb.getCollection(COLLECTION_EVIDENCE).createIndex(index);
    }

    /**
     * Should return the string 1.
     *
     * @return
     */
    @Override
    public final String test() {
        return "1";
    }

    /**
     * Test RPC method that accepts a single string parameter. Do nothing but
     * print string on screen.
     *
     * @param data
     */
    public final void testString(final String data) {
        System.out.println(data);
    }

    /**
     * {@inheritDoc}
     *
     * @param data {@inheritDoc}
     */
    public final void addRawData(final RawData data) {

        mongodb.getCollection(COLLECTION_DATA)
                .insertOne(convert(data));

        activation_controller.notifyRawData(data);
    }

    public final RawData[] findData(Document query) {
        FindIterable<Document> documents = mongodb
                .getCollection(COLLECTION_DATA)
                .find(query);

        ArrayList<RawData> results = new ArrayList<>();
        for (Document doc : documents) {
            results.add(convert(doc));
        }
        return results.toArray(new RawData[results.size()]);
    }

    /**
     * {@inheritDoc}
     *
     * @param label
     * @param subject
     * @return
     */
    public final RawData[] findRawData(
            final String label, final Subject subject) {

        Document query = new Document();
        query.append(LABEL, label);
        adapter.writeToMongo(subject, query);

        FindIterable<Document> documents = mongodb
                .getCollection(COLLECTION_DATA)
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
        mongodb.getCollection(COLLECTION_EVIDENCE)
                .insertOne(convert(evidence));

        activation_controller.notifyEvidence(evidence);
    }

    @Override
    public ObjectId addFile(final byte[] bytes, final String filename)
            throws Throwable {
        ObjectId file_id;
        try (GridFSUploadStream uploadStream
                = this.gridfsbucket.openUploadStream(filename)) {
            uploadStream.write(bytes);
            file_id = uploadStream.getObjectId();
        }
        return file_id;
    }

    @Override
    public byte[] findFile(ObjectId file_id) throws Throwable {
        byte[] data;
        try (GridFSDownloadStream downloadStream
                = this.gridfsbucket.openDownloadStream(file_id)) {
            int fileLength = (int) downloadStream.getGridFSFile().getLength();
            data = new byte[fileLength];
            downloadStream.read(data);
        }
        return data;
    }

    /**
     *
     * @return
     */
    public final Map<String, Object> status() {
        HashMap<String, Object> status = new HashMap<String, Object>();
        status.put("state", "running");
        status.put("activation", activation_controller.getProfiles());
        status.put("executed", activation_controller.getTaskCount());
        return status;
    }

    public final Document mongoStatus() {
        return mongodb.runCommand(
                new Document("serverStatus", 1)
                        .append("shardConnPoolStats", 1)
                        .append("dbStats", 1));
    }

    private static final String LABEL = "LABEL";
    private static final String TIME = "TIME";
    private static final String DATA = "DATA";
    private static final String CLIENT = "CLIENT";
    private static final String SERVER = "SERVER";
    private static final String SCORE = "SCORE";
    private static final String REPORT = "REPORT";

    /**
     * Convert from MongoDB document to RawData.
     *
     * @param doc
     * @return
     */
    private RawData convert(final Document doc) {

        RawData data = new RawData();
        data.subject = adapter.readFromMongo(doc);
        data.data = doc.getString(DATA);
        data.time = doc.getLong(TIME);
        data.label = doc.getString(LABEL);

        return data;

    }

    private Evidence convertEvidence(final Document doc) {

        Evidence evidence = new Evidence();
        evidence.subject = adapter.readFromMongo(doc);
        evidence.score = doc.getDouble(SCORE);
        evidence.time = doc.getLong(TIME);
        evidence.label = doc.getString(LABEL);
        evidence.report = doc.getString(REPORT);
        evidence.id = doc.getObjectId("_id").toString();

        return evidence;

    }

    /**
     * Convert from RawData to MongoDB document.
     *
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
     *
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
     *
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

        FindIterable<Document> documents = mongodb
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
    @Override
    public final Evidence[] findEvidence(final String label)
            throws Throwable {

        LOGGER.debug("findEvidence : " + label);

        try {
            Document query = new Document();
            query.append(LABEL, label);

            FindIterable<Document> documents = mongodb
                    .getCollection(COLLECTION_EVIDENCE)
                    .find(query);

            HashMap<Subject, Evidence> evidences
                    = new HashMap<Subject, Evidence>();
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

            Evidence[] evidences_array = evidences.values()
                    .toArray(new Evidence[evidences.size()]);

            return evidences_array;
        } catch (Throwable ex) {
            LOGGER.error("findEvidence : " + label, ex);
            throw ex;
        }
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

        FindIterable<Document> documents = mongodb
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

    public URL getURL() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     *
     * @param label
     * @param subject
     * @return
     */
    public final Evidence[] findLastEvidences(
            final String label, final Subject subject) {
        Document query = new Document();
        // Find everything that starts with "label"
        Pattern regex = Pattern.compile("^" + label);
        query.append(LABEL, regex);

        // ... corresponding to subject
        adapter.writeToMongo(subject, query);

        FindIterable<Document> documents = mongodb
                .getCollection(COLLECTION_EVIDENCE)
                .find(query);

        HashMap<String, Evidence> evidences = new HashMap<String, Evidence>();
        for (Document doc : documents) {
            Evidence evidence = convertEvidence(doc);

            Evidence inmap = evidences.get(evidence.label);
            if (inmap == null) {
                evidences.put(evidence.label, evidence);
                continue;
            }

            if (evidence.time > inmap.time) {
                evidences.put(evidence.label, evidence);
            }
        }
        return evidences.values().toArray(new Evidence[evidences.size()]);
    }

    /**
     * {@inheritDoc}
     *
     * @param key
     * @return
     * @throws Throwable
     */
    @Override
    public Object getFromCache(final String key) throws Throwable {
        synchronized (agents_cache) {
            return this.agents_cache.get(key);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param key
     * @param value
     * @throws Throwable
     */
    @Override
    public void storeInCache(final String key, final Object value) throws Throwable {
        synchronized (agents_cache) {
            this.agents_cache.put(key, value);
        }
    }

    /**
     * {@inheritDoc} synchronized block because multiple agents can acces to the
     * cache at the same time.
     *
     * @param key
     * @param new_value
     * @param old_value
     * @return
     * @throws Throwable
     */
    @Override
    public boolean compareAndSwapInCache(final String key,
            final Object new_value, final Object old_value) throws Throwable {
        boolean swaped = false;
        synchronized (agents_cache) {
            Object current = agents_cache.get(key);
            //If the value is not in the cache or if it didn't change
            if (current == null || current.equals(old_value)) {
                agents_cache.put(key, new_value);
                swaped = true;
            }
        }
        return swaped;
    }

}
