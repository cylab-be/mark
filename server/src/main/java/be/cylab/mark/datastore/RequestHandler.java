package be.cylab.mark.datastore;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import be.cylab.mark.core.ServerInterface;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import be.cylab.mark.activation.ActivationControllerInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Subject;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import be.cylab.mark.core.SubjectAdapter;
import com.mongodb.BasicDBObject;
import java.util.Collections;
import java.util.List;
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
    @Override
    public final Map<String, Object> executorStatus() {
        return activation_controller.getExecutorStatus();
    }

    @Override
    public final DetectionAgentProfile[] activation() {
        List<DetectionAgentProfile> profiles =
                activation_controller.getProfiles();
        return profiles.toArray(
                new DetectionAgentProfile[profiles.size()]);
    }

    private static final String LABEL = "LABEL";
    private static final String TIME = "TIME";
    private static final String DATA = "DATA";
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
        data.setSubject(adapter.readFromMongo(doc));
        data.setData(doc.getString(DATA));
        data.setTime(doc.getLong(TIME));
        data.setLabel(doc.getString(LABEL));

        return data;

    }

    private Evidence convertEvidence(final Document doc) {

        Evidence evidence = new Evidence();
        evidence.setSubject(adapter.readFromMongo(doc));
        evidence.setScore(doc.getDouble(SCORE));
        evidence.setTime(doc.getLong(TIME));
        evidence.setLabel(doc.getString(LABEL));
        evidence.setReport(doc.getString(REPORT));
        evidence.setId(doc.getObjectId("_id").toString());

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
                .append(LABEL, data.getLabel())
                .append(TIME, data.getTime())
                .append(DATA, data.getData());
        adapter.writeToMongo(data.getSubject(), doc);
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
                .append(LABEL, evidence.getLabel())
                .append(TIME, evidence.getTime())
                .append(SCORE, evidence.getScore())
                .append(TIME, evidence.getTime())
                .append(REPORT, evidence.getReport());

        adapter.writeToMongo(evidence.getSubject(), doc);
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

        return this.parseEvidences(documents);
    }

    @Override
    public final Evidence[] findEvidence(final String label)
            throws Throwable {
        return this.findEvidence(label, 0);
    }

    private final static int RESULTS_PER_PAGE = 100;

    /**
     * Keep only one evidence per subject: the most recent one.
     *
     * @param label
     * @return
     * @throws Throwable if request fails
     */
    @Override
    public final Evidence[] findEvidence(final String label, final int page)
            throws Throwable {

        LOGGER.debug("findEvidence : " + label);

        Document query = new Document();
        query.append(LABEL, label);

        FindIterable<Document> documents = mongodb
                .getCollection(COLLECTION_EVIDENCE)
                .find(query);

        documents.skip(page * RESULTS_PER_PAGE).limit(RESULTS_PER_PAGE);

        HashMap<Subject, Evidence> evidences = new HashMap<>();
        for (Document doc : documents) {
            Evidence evidence = convertEvidence(doc);

            Evidence inmap = evidences.get(evidence.getSubject());
            if (inmap == null) {
                evidences.put(evidence.getSubject(), evidence);
                continue;
            }

            if (evidence.getTime() > inmap.getTime()) {
                evidences.put(evidence.getSubject(), evidence);
            }
        }

        Evidence[] ev_array =
                evidences.values().toArray(new Evidence[evidences.size()]);

        Arrays.sort(ev_array, Collections.reverseOrder());
        return ev_array;
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
        // return activation_controller.getIgniteMetrics();
        throw new UnsupportedOperationException("Not supported...");
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

            Evidence inmap = evidences.get(evidence.getLabel());
            if (inmap == null) {
                evidences.put(evidence.getLabel(), evidence);
                continue;
            }

            if (evidence.getTime() > inmap.getTime()) {
                evidences.put(evidence.getLabel(), evidence);
            }
        }
        return evidences.values().toArray(new Evidence[evidences.size()]);
    }

    /**
     * Get the number of unique subjects(Client Server couples) in the database.
     *
     * @param doc   doc containing the aggregation value.
     * @return int, number of unique subjects
     */
    public final int findUniqueSubjects(final Document doc) {
        int unique_subjects = 0;
        Document query = new Document("$group",
                            new Document("_id", doc));
        AggregateIterable<Document> db_output = mongodb
                                .getCollection(COLLECTION_EVIDENCE)
                                .aggregate(Arrays.asList(query));

        for (Document db_document : db_output) {
                unique_subjects += 1;
        }
        return unique_subjects;
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

    @Override
    public Evidence[] findEvidenceSince(
            String label, Subject subject, long time) throws Throwable {
        Document query = new Document();
        query.append(LABEL, label);
        query.append(TIME, new BasicDBObject("$gt", time));
        adapter.writeToMongo(subject, query);

        FindIterable<Document> documents = mongodb
                .getCollection(COLLECTION_EVIDENCE)
                .find(query);

        return this.parseEvidences(documents);
    }

    private Evidence[] parseEvidences(FindIterable<Document> documents) {
        List<Evidence> evidences = new ArrayList<>();

        for (Document doc : documents) {
            evidences.add(convertEvidence(doc));
        }

        return evidences.toArray(new Evidence[evidences.size()]);
    }


}
