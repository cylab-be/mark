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
    String test();

    /**
     *
     * @param data
     */
    void testString(String data);

    /**
     * Add raw data to the datastore and eventually trigger analysis.
     *
     * @param data
     */
    void addRawData(RawData data);

    /**
     *
     * @param evidence
     */
    void addEvidence(Evidence evidence);

    /**
     *
     * @param type
     * @param client
     * @param server
     * @return
     */
    RawData[] findRawData(String type, String client, String server);



}
