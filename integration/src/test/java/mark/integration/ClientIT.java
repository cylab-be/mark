package mark.integration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import junit.framework.TestCase;
import mark.activation.DetectionAgentProfile;
import mark.server.InvalidProfileException;
import mark.client.Client;
import mark.masfad2.Link;
import mark.core.RawData;
import mark.masfad2.LinkAdapter;
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
            throws FileNotFoundException, InvalidProfileException,
            MalformedURLException, Exception {

        Config config = new Config();
        config.adapter_class = LinkAdapter.class.getName();
        server = new Server(config);

        // Activate the dummy activation profiles
        InputStream activation_file = getClass()
                .getResourceAsStream("/detection.dummy.yml");
        server.addDetectionAgent(DetectionAgentProfile.fromInputStream(activation_file));
        server.start();
    }

    /**
     * Test of Test method, of class DatastoreClient.
     */
    public final void testTest() throws Throwable {
        System.out.println("test");
        System.out.println("====");

        startDummyServer();
        Client datastore = new Client(new URL("http://127.0.0.1:8080"), new LinkAdapter());
        assertEquals("1", datastore.test());
    }

    /**
     *
     * @throws java.io.FileNotFoundException
     * @throws mark.activation.InvalidProfileException
     */
    public final void testString() throws Throwable {
        System.out.println("testString");
        System.out.println("==========");

        startDummyServer();
        Client datastore = new Client(new URL("http://127.0.0.1:8080"), new LinkAdapter());
        datastore.testString("My String");
    }

    /**
     * Test of Test method, of class DatastoreClient.
     * @throws java.io.FileNotFoundException
     * @throws mark.activation.InvalidProfileException
     */
    public final void testAddRawData() throws Throwable {
        System.out.println("addRawData");
        System.out.println("==========");

        startDummyServer();

        Client datastore = new Client(new URL("http://127.0.0.1:8080"), new LinkAdapter());
        RawData data = new RawData();
        data.label = "http";
        data.subject = new Link("1.2.3.4", "www.google.be");
        data.time = 1230987;
        data.data = "A proxy log line...";
        datastore.addRawData(data);
    }

    public final void testAddFindRawData() throws Throwable {
        System.out.println("addRawData and findRawData");
        System.out.println("==========================");

        startDummyServer();

        String label = "http";
        Link link = new Link("1.2.3.4", "www.google.be");

        Client<Link> datastore = new Client<Link>(new URL("http://127.0.0.1:8080"), new LinkAdapter());
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
     * @throws FileNotFoundException
     * @throws InvalidProfileException
     */
    public final void testReadWriteRawData() throws Throwable {
        System.out.println("addRawData, with a ReadWrite detection agent");
        System.out.println("============================================");

        // Start server with read-write activation profile
        Config config = new Config();
        config.adapter_class = LinkAdapter.class.getName();
        server = new Server(config);

        InputStream activation_file = getClass()
                .getResourceAsStream("/detection.readwrite.yml");
        server.addDetectionAgent(
                DetectionAgentProfile.fromInputStream(activation_file));
        server.start();

        Client datastore = new Client(new URL("http://127.0.0.1:8080"), new LinkAdapter());
        RawData data = new RawData();
        data.label = "http";
        data.subject = new Link("1.2.3.4", "www.google.be");
        data.time = 1230987;
        data.data = "A proxy log line...";
        datastore.addRawData(data);
    }

    public final void testInvalidConnection() throws Throwable {
        System.out.println("Test invalid connection");
        System.out.println("=======================");
        startDummyServer();

        Client datastore = null;
        try {
            datastore = new Client(new URL("http://123.45.67.89:8082"), new LinkAdapter());
            datastore.test();
            fail("Should throw a SocketTimeoutException !");
            datastore.test();

        } catch (SocketTimeoutException ex) {
            assertEquals(
                    ex.getClass().getName(), "java.net.SocketTimeoutException");

        }
    }
}
