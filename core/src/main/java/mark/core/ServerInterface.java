package mark.core;

import java.net.URL;
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
     * @param type
     * @param subject
     * @return
     * @throws java.lang.Throwable if request fails
     */
    RawData<T>[] findRawData(String type, T subject) throws Throwable;

    /**
     *
     * @param label
     * @param subject
     * @return
     * @throws java.lang.Throwable if request fails
     */
    Evidence<T>[] findEvidence(String label, T subject) throws Throwable;

    /**
     * Find evidence of given label, for all subjects. Useful for displaying
     * ranked list of subjects.
     *
     * @param label
     * @return
     * @throws java.lang.Throwable if request fails
     */
    Evidence<T>[] findEvidence(String label) throws Throwable;

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
}
