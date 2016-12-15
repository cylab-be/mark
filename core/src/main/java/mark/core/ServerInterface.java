package mark.core;

/**
 * Interface defining all methods provided by the server.
 * These are implemented by the client and server packages.
 *
 * @author Thibault Debatty
 */
public interface ServerInterface<T extends Subject> {

    /**
     *
     * @return
     */
    String test() throws Throwable;

    /**
     *
     * @param data
     */
    void testString(String data) throws Throwable;

    /**
     * Add raw data to the datastore and eventually trigger analysis.
     *
     * @param data
     */
    void addRawData(RawData<T> data) throws Throwable;

    /**
     *
     * @param evidence
     */
    void addEvidence(Evidence evidence) throws Throwable;

    /**
     *
     * @param type
     * @param client
     * @param server
     * @return
     */
    RawData<T>[] findRawData(String type, T subject) throws Throwable;

    Evidence<T>[] findEvidence(String label, T subject) throws Throwable;



}
