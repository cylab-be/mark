package be.cylab.mark.datastore;

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
import be.cylab.mark.core.DataAgentProfile;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import be.cylab.mark.server.DataSourcesController;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoCommandException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thibault Debatty
 */
@Singleton
public final class RequestHandler implements ServerInterface {

    private static final String COLLECTION_DATA = "DATA";
    private static final String COLLECTION_EVIDENCE = "EVIDENCE";
    private static final String COLLECTION_FILES = "FILES";

    private static final Logger LOGGER
            = LoggerFactory.getLogger(RequestHandler.class);

    private final MongoDatabase mongodb;
    private final GridFSBucket gridfsbucket;
    private final ActivationControllerInterface activation_controller;
    private final DataSourcesController sources;
    private final MongoParser parser;

    //Cache
    private final HashMap<String, Object> agents_cache;


    /**
     *
     * @param mongodb
     * @param activation_controller
     * @param sources
     * @param parser
     */
    @Inject
    public RequestHandler(
            final MongoDatabase mongodb,
            final ActivationControllerInterface activation_controller,
            final DataSourcesController sources,
            final MongoParser parser) {

        this.agents_cache = new HashMap();
        this.mongodb = mongodb;
        this.activation_controller = activation_controller;
        this.sources = sources;
        this.parser = parser;
        this.gridfsbucket = GridFSBuckets.create(mongodb, COLLECTION_FILES);

        // Create indexes for LABEL and TIME
        Document index = new Document(MongoParser.LABEL, 1);
        mongodb.getCollection(COLLECTION_DATA).createIndex(index);
        mongodb.getCollection(COLLECTION_EVIDENCE).createIndex(index);

        index = new Document(MongoParser.TIME, 1);
        mongodb.getCollection(COLLECTION_DATA).createIndex(index);
        mongodb.getCollection(COLLECTION_EVIDENCE).createIndex(index);
    }

    /**
     * Should return the string 1.
     *
     * @return
     */
    @Override
    public String test() {
        return "1";
    }

    /**
     * Test RPC method that accepts a single string parameter. Do nothing but
     * print string on screen.
     *
     * @param data
     */
    @Override
    public void testString(final String data) {
        System.out.println(data);
    }

    /**
     * {@inheritDoc}
     *
     * @param data {@inheritDoc}
     */
    @Override
    public void addRawData(final RawData data) {

        Document document = parser.convert(data);
        mongodb.getCollection(COLLECTION_DATA)
                .insertOne(document);

        ObjectId id = (ObjectId) document.get("_id");
        data.setId(id.toString());

        activation_controller.notifyRawData(data);
    }

    /**
     * {@inheritDoc}
     *
     * @param label
     * @param subject
     * @return
     */
    @Override
    public RawData[] findRawData(
            final String label, final Map<String, String> subject,
            final long from,
            final long till) {

        Document query = new Document();
        query.append(MongoParser.LABEL, label);
        for (Entry entry : subject.entrySet()) {
            query.append(
                    MongoParser.SUBJECT + "." + entry.getKey(),
                    entry.getValue());
        }
        query.append(
                MongoParser.TIME,
                new Document("$gte", from).append("$lte", till));

        FindIterable<Document> documents = mongodb
                .getCollection(COLLECTION_DATA)
                .find(query);

        return this.parseData(documents);

    }

    private RawData[] parseData(final FindIterable<Document> documents) {
        ArrayList<RawData> results = new ArrayList<>();
        for (Document doc : documents) {
            results.add(parser.convert(doc));
        }
        return results.toArray(new RawData[results.size()]);
    }

    /**
     *
     * @param evidence
     */
    @Override
    public void addEvidence(final Evidence evidence) {

        Document document = parser.convert(evidence);
        mongodb.getCollection(COLLECTION_EVIDENCE)
                .insertOne(document);
        ObjectId id = (ObjectId) document.get("_id");
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
    public byte[] findFile(final ObjectId file_id) throws Throwable {
        byte[] data;
        try (GridFSDownloadStream downloadStream
                = this.gridfsbucket.openDownloadStream(file_id)) {
            int file_length = (int) downloadStream.getGridFSFile().getLength();
            data = new byte[file_length];
            downloadStream.read(data);
        }
        return data;
    }

    @Override
    public DetectionAgentProfile[] activation() {
        List<DetectionAgentProfile> profiles =
                activation_controller.getProfiles();
        return profiles.toArray(
                new DetectionAgentProfile[profiles.size()]);
    }

    @Override
    public void setAgentProfile(final DetectionAgentProfile profile)
            throws Throwable {

        activation_controller.setAgentProfile(profile);

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
    public Evidence[] findEvidence(
            final String label, final Map<String, String> subject)
            throws Throwable {

        Document query = new Document();
        query.append(MongoParser.LABEL, label);
        query.append(MongoParser.SUBJECT, subject);

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
    public Evidence[] findEvidence(final String label)
            throws Throwable {

        LOGGER.debug("findEvidence : " + label);

        Document query = new Document();
        query.append(MongoParser.LABEL, label);

        FindIterable<Document> documents = mongodb
                .getCollection(COLLECTION_EVIDENCE)
                .find(query);

        HashMap<Map, Evidence> evidences = new HashMap<>();
        for (Document doc : documents) {
            Evidence evidence = parser.convertEvidence(doc);

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

    private static final int RESULTS_PER_PAGE = 100;

    /**
     * Keep only one evidence per subject: the most recent one.
     *
     * @param label
     * @return
     * @throws Throwable if request fails
     */
    @Override
    public Evidence[] findEvidence(final String label, final int page)
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
    public Evidence findEvidenceById(final String id) {
        Document query = new Document();
        query.append("_id", new ObjectId(id));

        FindIterable<Document> documents = mongodb
                .getCollection(COLLECTION_EVIDENCE)
                .find(query);

        Document document = documents.first();

        if (document == null) {
            throw new IllegalArgumentException("Invalid id provided: " + id);
        }

        return parser.convertEvidence(document);
    }

    /**
     *
     * @return
     */
    @Override
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
    public Evidence[] findLastEvidences(
            final String label, final Map<String, String> subject) {
        Document query = new Document();
        // Find everything that starts with "label"
        Pattern regex = Pattern.compile("^" + label);
        query.append(MongoParser.LABEL, regex);
        query.append(MongoParser.SUBJECT, subject);

        FindIterable<Document> documents = mongodb
                .getCollection(COLLECTION_EVIDENCE)
                .find(query);

        HashMap<String, Evidence> evidences = new HashMap<>();
        for (Document doc : documents) {
            Evidence evidence = parser.convertEvidence(doc);

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
    public void storeInCache(final String key, final Object value)
            throws Throwable {
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
            final String label, final Map<String, String> subject,
            final long time)
            throws Throwable {
        Document query = new Document();
        query.append(MongoParser.LABEL, label);
        query.append(MongoParser.TIME, new BasicDBObject("$gt", time));
        query.append(MongoParser.SUBJECT, subject);

        FindIterable<Document> documents = mongodb
                .getCollection(COLLECTION_EVIDENCE)
                .find(query);

        return this.parseEvidences(documents);
    }

    private Evidence[] parseEvidences(final FindIterable<Document> documents) {
        List<Evidence> evidences = new ArrayList<>();

        for (Document doc : documents) {
            evidences.add(parser.convertEvidence(doc));
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
    public void reload() {
        this.activation_controller.reload();
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
        status.put(
                "version", getClass().getPackage().getImplementationVersion());

        OperatingSystemMXBean os =
         (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        status.put("arch", os.getArch());
        status.put("processors", os.getAvailableProcessors());
        status.put("load", os.getSystemLoadAverage());
        status.put("os.name", os.getName());
        status.put("os.version", os.getVersion());

        Runtime rt = Runtime.getRuntime();
        status.put("memory.total", rt.maxMemory() / 1024 / 1024);
        status.put(
                "memory.used",
                (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024);

        return status;
    }

    private Map<String, Object> dbStatus() throws Throwable {
        Map<String, Object> status = new HashMap<>();

        try {
            status.put(
                    "db.data.count",
                    mongodb.getCollection(COLLECTION_DATA).countDocuments());
            status.put(
                    "db.evidence.count",
                    mongodb.getCollection(COLLECTION_EVIDENCE)
                            .countDocuments());

            Document stats = mongodb.runCommand(
                    Document.parse(
                            "{ collStats: '" + COLLECTION_DATA
                                    + "', scale: 1048576}"));
            status.put("db.data.size", stats.getInteger("size"));

            stats = mongodb.runCommand(
                    Document.parse(
                            "{ collStats: '" + COLLECTION_EVIDENCE
                                    + "', scale: 1048576}"));
            status.put("db.evidence.size", stats.getInteger("size"));
        } catch (MongoCommandException ex) {
            LOGGER.warn(ex.getMessage());
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
            history.add(parser.convertToMap(doc));
        }

        return history;
    }

    @Override
    public RawData[] findLastRawData() throws Throwable {
        Document query = new Document()
                .append("_id", -1);

        return this.parseData(
            mongodb.getCollection(COLLECTION_DATA)
                    .find()
                    .sort(query)
                    .limit(100));
    }

    @Override
    public Evidence[] findLastEvidences() throws Throwable {
        Document query = new Document()
                .append("_id", -1);

        return this.parseEvidences(
            mongodb.getCollection(COLLECTION_EVIDENCE)
                    .find()
                    .sort(query)
                    .limit(100));
    }

    @Override
    public DataAgentProfile[] sources() throws Throwable {
        List<DataAgentProfile> profiles = sources.getProfiles();
        return profiles.toArray(
                new DataAgentProfile[profiles.size()]);
    }
}
