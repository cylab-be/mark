package be.cylab.mark.core;

import java.net.URL;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;

/**
 * Interface defining all methods provided by the server. These are implemented
 * by the client and server packages.
 *
 * @author Thibault Debatty
 */
public interface ServerInterface {

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
    void addRawData(RawData data) throws Throwable;

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
     * Find the last data records that were inserted in DB.
     * @return
     * @throws java.lang.Throwable if request fails
     */
    RawData[] findLastRawData() throws Throwable;

    /**
     *
     * @param label
     * @param subject
     * @param from
     * @param till
     * @return
     * @throws java.lang.Throwable if request fails
     */
    RawData[] findRawData(String label, Map<String, String> subject,
            long from, long till) throws Throwable;

    /**
     *
     * @param label
     * @param subject
     * @return
     * @throws java.lang.Throwable if request fails
     */
    Evidence[] findEvidence(String label, Map<String, String> subject)
            throws Throwable;

    /**
     *
     * @param label
     * @param subject
     * @param time
     * @return
     * @throws Throwable if something goes wrong
     */
    Evidence[] findEvidenceSince(String label, Map<String, String> subject,
            long time) throws Throwable;


    /**
     * Find the evidences with highest score, for given label and for all
     * subjects. Used to display the most suspicious subjects.
     *
     * @param label
     * @return
     * @throws java.lang.Throwable if request fails
     */
    Evidence[] findEvidence(String label) throws Throwable;

    /**
     * Find evidence of given label, for all subjects.
     *
     * @param label
     * @param page
     * @return
     * @throws java.lang.Throwable if request fails
     */
    Evidence[] findEvidence(String label, int page) throws Throwable;

    /**
     * Get a single evidence by id.
     *
     * @param id
     * @return
     * @throws Throwable if request fails
     */
    Evidence findEvidenceById(String id) throws Throwable;

    /**
     *
     * @return
     */
    URL getURL();

    /**
     * Find the last evidences that were inserted in the DB.
     * @return
     * @throws java.lang.Throwable if request fails
     */
    Evidence[] findLastEvidences() throws Throwable;

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
    Evidence[] findLastEvidences(String label, Map<String, String> subject)
            throws Throwable;


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
     * Get the current configuration (activation profiles).
     * @return
     * @throws java.lang.Throwable if something goes wrong
     */
    DetectionAgentProfile[] activation() throws Throwable;

    /**
     * Add or update the configuration a detector. If profile.label is already
     * defined, the configuration is updated, otherwise a new detector is added.
     * @param profile
     * @throws java.lang.Throwable if something went wrong
     */
    void setAgentProfile(DetectionAgentProfile profile) throws Throwable;

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

    /**
     * Reload the directory containing detection agents profiles.
     * @throws java.lang.Throwable if something went wrong
     */
    void reload() throws Throwable;


    /**
     * Get the last previous status objects.
     * @return
     * @throws Throwable if something goes wrong
     */
    List<Map> history() throws Throwable;

}
