package mark.core;

/**
 * Interface defining all methods provided by the server.
 * These are implemented by the client and server packages.
 *
 * @author Thibault Debatty
 */
public interface ServerInterface {

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
    void addRawData(RawData data) throws Throwable;

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
    RawData[] findRawData(String type, String client, String server)
            throws Throwable;



}
