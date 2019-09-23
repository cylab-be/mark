package be.cylab.mark.core;

import java.net.URL;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Interface defining all methods provided by the server. These are implemented
 * by the client and server packages.
 *
 * @author Thibault Debatty
 * @param <T> The type of data that this server is dealing with (can be a link
 * between between a local computer and a server on the internet, or a person,
 * etc.).
 */
public interface ServerInterface<T extends Subject> {

    /**
     *
     * @return @throws java.lang.Throwable if request fails
     */
    String test() throws Throwable;

    /**
     *
     * @param data
     * @throws java.lang.Throwable if request fails
     */
    void testString(String data) throws Throwable;

    /**
     * Add raw data to the datastore and eventually trigger analysis.
     *
     * @param data
     * @throws java.lang.Throwable if request fails
     */
    void addRawData(RawData<T> data) throws Throwable;

    /**
     *
     * @param evidence
     * @throws java.lang.Throwable if request fails
     */
    void addEvidence(Evidence evidence) throws Throwable;

    /**
     *
     * @param bytes
     * @param filename
     * @return ObjectId in mongodb of the added file.
     * @throws Throwable if request fails
     */
    ObjectId addFile(byte[] bytes, String filename) throws Throwable;

    /**
     *
     * @param file_id
     * @return byte[].
     * @throws Throwable if request fails.
     */
    byte[] findFile(ObjectId file_id) throws Throwable;

    /**
     *
     * @param label
     * @param subject
     * @param from
     * @param till
     * @return
     * @throws java.lang.Throwable if request fails
     */
    RawData<T>[] findRawData(String label, T subject, long from, long till)
            throws Throwable;

    /**
     *
     * @param label
     * @param subject
     * @return
     * @throws java.lang.Throwable if request fails
     */
    Evidence<T>[] findEvidence(String label, T subject) throws Throwable;

    /**
     *
     * @param label
     * @param subject
     * @param time
     * @return
     * @throws Throwable if something goes wrong
     */
    Evidence<T>[] findEvidenceSince(String label, T subject, long time)
            throws Throwable;


    /**
     * Find the evidences with highest score, for given label and for all
     * subjects. Used to display the most suspicious subjects.
     *
     * @param label
     * @return
     * @throws java.lang.Throwable if request fails
     */
    Evidence<T>[] findEvidence(String label) throws Throwable;

    /**
     * Find evidence of given label, for all subjects.
     *
     * @param label
     * @param page
     * @return
     * @throws java.lang.Throwable if request fails
     */
    Evidence<T>[] findEvidence(String label, int page) throws Throwable;

    /**
     * Get a single evidence by id.
     *
     * @param id
     * @return
     * @throws Throwable if request fails
     */
    Evidence<T> findEvidenceById(String id) throws Throwable;

    /**
     *
     * @return
     */
    URL getURL();

    /**
     * Find the evidences according to a pattern (that start with provided
     * pattern), and if multiple evidences are found with same label, return the
     * most recent one.
     *
     * @param label
     * @param subject
     * @return
     * @throws Throwable if an error occured
     */
    Evidence<T>[] findLastEvidences(String label, T subject)
            throws Throwable;

    /**
     * Search for data with a custom filter (don't forget to mention a label,
     * for example).
     *
     * @param query
     * @return
     * @throws Throwable if an error occured
     */
    RawData[] findData(Document query) throws Throwable;

    /**
     * get value from cache represented by a map.
     *
     * @param key to get value.
     * @return value.
     * @throws java.lang.Throwable if an error occured
     */
    Object getFromCache(String key) throws Throwable;

    /**
     * Store the value in the cache with the key key.
     *
     * @param key to store value.
     * @param value to store.
     * @throws Throwable if any problems happens.
     */
    void storeInCache(String key, Object value) throws Throwable;

    /**
     * Compare and swap verify if the current stored value in the cache is
     * equals to old_value, or if the value has never been stored in the cache
     * for this key. Since multiple agents can get access to the cache, We do
     * this verification to not overwrite new values from other agents.
     *
     * @param key to store new value.
     * @param new_value to be stored.
     * @param old_value to verify current value.
     * @return true if it's swaped
     * @throws Throwable if any problems happens.
     */
    boolean compareAndSwapInCache(String key, Object new_value,
            Object old_value) throws Throwable;

    /**
     * Get the status of MARK (running, ram, CPU load, version ...).
     * @return
     * @throws Throwable if something went wrong
     */
    Map<String, Object> status() throws Throwable;

    /**
     * Get the status of the executor service (running tasks etc).
     * @return
     * @throws java.lang.Throwable if something goes wrong
     */
    Map<String, Object> executorStatus() throws Throwable;

    /**
     * Get the status of the database (mongo).
     * @return
     * @throws Throwable if something goes wrong
     */
    Map<String, Object> dbStatus() throws Throwable;

    /**
     * Get the current configuration (activation profiles).
     * @return
     * @throws java.lang.Throwable if something goes wrong
     */
    DetectionAgentProfile[] activation() throws Throwable;

    /**
     * Pause execution (no algorithm will be scheduled).
     *
     * @throws Throwable if something went wrong
     */
    void pause() throws Throwable;

    /**
     * Resume execution.
     * @throws Throwable if something went wrong
     */
    void resume() throws Throwable;

}
