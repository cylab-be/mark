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
import com.mongodb.client.MongoCursor;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
     * @param activation_controller
     * @param adapter
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
    @Override
    public final void testString(final String data) {
        System.out.println(data);
    }

    /**
     * {@inheritDoc}
     *
     * @param data {@inheritDoc}
     */
    @Override
    public final void addRawData(final RawData data) {

        Document document = convert(data);
        mongodb.getCollection(COLLECTION_DATA)
                .insertOne(document);

        ObjectId id = (ObjectId)document.get( "_id" );
        data.setId(id.toString());

        activation_controller.notifyRawData(data);
    }

    @Override
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
    @Override
    public final RawData[] findRawData(
            final String label, final Subject subject, final long from,
            final long till) {

        Document query = new Document();
        query.append(LABEL, label);
        query.append(TIME, new Document("$gte", from).append("$lte", till));
        adapter.writeToMongo(subject, query);

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
     *
     * @param evidence
     */
    @Override
    public final void addEvidence(final Evidence evidence) {

        Document document = convert(evidence);
        mongodb.getCollection(COLLECTION_EVIDENCE)
                .insertOne(document);
        ObjectId id = (ObjectId)document.get( "_id" );
        evidence.setId(id.toString());

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
    private static final String REFERENCES = "references";

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
        evidence.setReferences(doc.getList(REFERENCES, String.class));
        evidence.setRequests(doc.getList("requests", String.class));

        Document profile_doc = doc.get("profile", Document.class);

        if (profile_doc != null) {
            DetectionAgentProfile profile = new DetectionAgentProfile();
            profile.setClassName(profile_doc.getString("class_name"));
            profile.setLabel(profile_doc.getString("label"));
            profile.setTriggerLabel(profile_doc.getString("trigger_label"));
            profile.setParameters(this.convertToMap(profile_doc.get("parameters", Document.class)));
            evidence.setProfile(profile);
        }

        return evidence;
    }

    public Document convert(Map map) {
        return new Document(map);
    }

    public HashMap convertToMap (Document doc) {
        HashMap map = new HashMap();

        for (Map.Entry<String, Object> entry : doc.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
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
                .append(REPORT, evidence.getReport())
                .append(REFERENCES, evidence.getReferences())
                .append("requests", evidence.getRequests());

        if (evidence.getProfile() != null) {
            Document profile_doc = new Document()
                .append("class_name", evidence.getProfile().getClassName())
                .append("label", evidence.getProfile().getLabel())
                .append("trigger_label", evidence.getProfile().getTriggerLabel())
                .append("parameters", convert(evidence.getProfile().getParameters()));
            doc.append("profile", profile_doc);
        }

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
    @Override
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

    /**
     * 
     * @param label
     * @return
     * @throws Throwable 
     */
    @Override
    public final Evidence[] findEvidence(final String label)
            throws Throwable {

        LOGGER.debug("findEvidence : " + label);

        Document query = new Document();
        query.append(LABEL, label);

        FindIterable<Document> documents = mongodb
                .getCollection(COLLECTION_EVIDENCE)
                .find(query);

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

        if (page < 1) {
            throw new IllegalArgumentException("page must be  >= 1");
        }

        Evidence[] evidences = this.findEvidence(label);

        int start_index = (page - 1) * RESULTS_PER_PAGE;
        if (start_index > evidences.length) {
            return new Evidence[]{};
        }

        int end_index = start_index + RESULTS_PER_PAGE;
        if (end_index > evidences.length) {
            end_index = evidences.length;
        }
        return Arrays.copyOfRange(evidences, start_index, end_index);
    }

    /**
     * Get a single evidence by id, or throw an exception if id is not valid.
     *
     * @param id
     * @return
     */
    @Override
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
    @Override
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

        HashMap<String, Evidence> evidences = new HashMap<>();
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
    public final Subject[] findUniqueSubjects(final Document doc) {
        int unique_subjects = 0;
        List<Subject> entries = new ArrayList<>();
        Document query = new Document("$group",
                            new Document("_id", doc));
        AggregateIterable<Document> db_output = mongodb
                                .getCollection(COLLECTION_EVIDENCE)
                                .aggregate(Arrays.asList(query));

        for (Document db_document : db_output) {
                unique_subjects += 1;
                entries.add(adapter.readFromMongo(db_document
                        .get("_id", Document.class)));
        }
        return entries.toArray(new Subject[entries.size()]);
    }

    public final String[] findDistinctEntries(final String field) {
        List<String> entries = new ArrayList<>();

        try (MongoCursor<String> cursor = mongodb
                .getCollection(COLLECTION_EVIDENCE)
                .distinct(field, String.class).iterator()) {

            while (cursor.hasNext()) {
                String temp_entry = cursor.next();
                entries.add(temp_entry);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return entries.toArray(new String[entries.size()]);
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

    @Override
    public void pause() throws Throwable {
        this.activation_controller.pauseExecution();
    }

    @Override
    public void resume() throws Throwable {
        this.activation_controller.resumeExecution();
    }

    @Override
    public Map<String, Object> status() throws Throwable {
        Map<String, Object> status = new HashMap<>();
        status.putAll(this.markStatus());
        status.putAll(this.dbStatus());
        status.putAll(this.executorStatus());
        return status;
    }

    private Map<String, Object> executorStatus() {
        return activation_controller.getExecutorStatus();
    }

    private Map<String, Object> markStatus() throws Throwable {
        Map<String, Object> status = new HashMap<>();

        status.put("running", activation_controller.isRunning());
        status.put("version", getClass().getPackage().getImplementationVersion());

        OperatingSystemMXBean os =
         (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        status.put("arch", os.getArch());
        status.put("processors", os.getAvailableProcessors());
        status.put("load", os.getSystemLoadAverage());
        status.put("os.name", os.getName());
        status.put("os.version", os.getVersion());

        Runtime rt = Runtime.getRuntime();
        status.put("memory.total", rt.maxMemory());
        status.put("memory.used", rt.totalMemory() - rt.freeMemory());

        return status;
    }

    private Map<String, Object> dbStatus() throws Throwable {
        Map<String, Object> status = new HashMap<>();
        status.put("db.data.count", mongodb.getCollection(COLLECTION_DATA).countDocuments());
        status.put("db.evidence.count", mongodb.getCollection(COLLECTION_EVIDENCE).countDocuments());

        Document stats = mongodb.runCommand(Document.parse("{ collStats: '" + COLLECTION_DATA + "', scale: 1}"));
        try {
            status.put("db.data.size", stats.getInteger("size"));
        } catch (Exception ex) {
            status.put("db.data.size", stats.getDouble("size"));
        }

        stats = mongodb.runCommand(Document.parse("{ collStats: '" + COLLECTION_EVIDENCE + "', scale: 1}"));
        try {
            status.put("db.evidence.size", stats.getInteger("size"));
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch status from DB: " + ex.getMessage());
            status.put("db.evidence.size", stats.getDouble("size"));
        }

        return status;
    }

    @Override
    public List<Map> history() throws Throwable {

        // 1h
        long start_time = System.currentTimeMillis() - 1000 * 3600;
        Document query = new Document()
                .append("time", new Document("$gt", start_time));

        FindIterable<Document> documents = mongodb
                .getCollection("statistics")
                .find(query);


        List<Map> history = new LinkedList<>();

        for (Document doc : documents) {
            history.add(convertToMap(doc));
        }

        return history;
    }
}
