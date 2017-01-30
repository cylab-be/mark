package mark.integration;

import java.net.SocketTimeoutException;
import java.net.URL;
import junit.framework.TestCase;
import mark.activation.DetectionAgentProfile;
import mark.client.Client;
import netrank.Link;
import netrank.LinkAdapter;
import mark.core.RawData;
import mark.server.Config;
import mark.server.Server;

/**
 * The server must be compiled and started before we can run this
 * test => integration test (and not unit test).
 *
 * @author Thibault Debatty
 */
public class ClientIT extends TestCase {

    private Server server;

    @Override
    protected final void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    protected final void startDummyServer()
            throws Throwable {


        Config config = Config.getTestConfig();
        config.adapter_class = LinkAdapter.class.getName();
        server = new Server(config);

        // Activate the dummy activation profile
        server.addDetectionAgent(DetectionAgentProfile.fromInputStream(
                getClass()
                .getResourceAsStream("/detection.dummy.yml")));
        server.start();
    }

    /**
     * Test of Test method, of class DatastoreClient.
     * @throws java.lang.Throwable
     */
    public final void testTest() throws Throwable {
        System.out.println("test");
        System.out.println("====");

        startDummyServer();
        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"), new LinkAdapter());
        assertEquals("1", datastore.test());
    }

    /**
     *
     * @throws java.lang.Throwable
     */
    public final void testString() throws Throwable {
        System.out.println("testString");
        System.out.println("==========");

        startDummyServer();
        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"), new LinkAdapter());
        datastore.testString("My String");
    }

    /**
     *
     * @throws Throwable
     */
    public final void testAddFindRawData() throws Throwable {
        System.out.println("addRawData and findRawData");
        System.out.println("==========================");

        startDummyServer();

        String label = "http";
        Link link = new Link("1.2.3.4", "www.google.be");

        Client<Link> datastore = new Client<Link>(
                new URL("http://127.0.0.1:8080"), new LinkAdapter());
        RawData[] original_data = datastore.findRawData(label, link);

        RawData new_data = new RawData();
        new_data.label = label;
        new_data.subject = link;
        new_data.time = (int) (System.currentTimeMillis() / 1000L);
        new_data.data = "A proxy log line...";
        datastore.addRawData(new_data);

        assertEquals(
                original_data.length + 1,
                datastore.findRawData(label, link).length);
    }

    /**
     *
     * @throws Throwable
     */
    public final void testActivation() throws Throwable {
        System.out.println("addRawData, with a ReadWrite detection agent");
        System.out.println("============================================");

        // Start server with read-write activation profile
        Config config = Config.getTestConfig();
        config.adapter_class = LinkAdapter.class.getName();
        server = new Server(config);

        server.addDetectionAgent(
                DetectionAgentProfile.fromInputStream(
                        getClass()
                        .getResourceAsStream("/detection.readwrite.yml")));
        server.start();

        // Count the original number of evidences
        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"), new LinkAdapter());
        int original_count = datastore
                .findEvidence(
                        "detection.rw", new Link("1.2.3.4", "www.google.be"))
                .length;

        // add a data, which should trigger the rw detector
        RawData data = new RawData();
        data.label = "data.http";
        data.subject = new Link("1.2.3.4", "www.google.be");
        data.time = 1230987;
        data.data = "A proxy log line...";
        datastore.addRawData(data);

        server.awaitTermination();

        int final_count = datastore
                .findEvidence(
                        "detection.rw", new Link("1.2.3.4", "www.google.be"))
                .length;

        assertEquals(original_count + 2, final_count);
    }

    /**
     *
     * @throws Throwable
     */
    public final void testInvalidConnection() throws Throwable {
        System.out.println("Test invalid connection");
        System.out.println("=======================");
        startDummyServer();

        try {
            Client datastore = new Client(
                    new URL("http://123.45.67.89:8082"), new LinkAdapter());
            datastore.test();
            fail("Should throw a SocketTimeoutException !");
            datastore.test();

        } catch (SocketTimeoutException ex) {
            assertEquals(
                    ex.getClass().getName(), "java.net.SocketTimeoutException");

        }
    }
}
