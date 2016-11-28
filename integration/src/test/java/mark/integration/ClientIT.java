package mark.integration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import junit.framework.TestCase;
import mark.activation.DetectionAgentProfile;
import mark.activation.InvalidProfileException;
import mark.client.Client;
import mark.core.RawData;
import mark.server.Server;

/**
 * The server must be compiled and started before we can run this
 * test => integration test (and not unit test).
 *
 * @author Thibault Debatty
 */
public class ClientIT extends TestCase {

    private Server server;

    protected final void startDummyServer()
            throws FileNotFoundException, InvalidProfileException,
            MalformedURLException, Exception {
        server = new Server();

        // Activate the dummy activation profiles
        InputStream activation_file = getClass()
                .getResourceAsStream("/detection.dummy.yml");
        server.addDetectionAgentProfile(DetectionAgentProfile.fromInputStream(activation_file));
        server.start();
    }

    /**
     * Test of Test method, of class DatastoreClient.
     */
    public final void testTest() throws Throwable {
        System.out.println("test");
        startDummyServer();
        Client datastore = new Client(new URL("http://127.0.0.1:8080"));
        assertEquals("1", datastore.test());
        server.stop();
    }

    /**
     *
     * @throws java.io.FileNotFoundException
     * @throws mark.activation.InvalidProfileException
     */
    public final void testString() throws Throwable {
        System.out.println("testString");
        startDummyServer();
        Client datastore = new Client(new URL("http://127.0.0.1:8080"));
        datastore.testString("My String");
        server.stop();
    }

    /**
     * Test of Test method, of class DatastoreClient.
     * @throws java.io.FileNotFoundException
     * @throws mark.activation.InvalidProfileException
     */
    public final void testAddRawData() throws Throwable {
        System.out.println("addRawData");

        startDummyServer();
        Client datastore = new Client(new URL("http://127.0.0.1:8080"));
        RawData data = new RawData();
        data.label = "http";
        data.client = "1.2.3.4";
        data.server = "www.google.be";
        data.time = 1230987;
        data.data = "A proxy log line...";
        datastore.addRawData(data);
        server.stop();
    }

    public final void testAddFindRawData() throws Throwable {
        System.out.println("addRawData and findRawData");

        startDummyServer();
        String type = "http";
        String client = "1.2.3.4";
        String servername = "www.google.be";

        Client datastore = new Client(new URL("http://127.0.0.1:8080"));
        RawData[] original_data =
                datastore.findRawData(type, client, servername);

        RawData new_data = new RawData();
        new_data.label = type;
        new_data.client = client;
        new_data.server = servername;
        new_data.time = (int) (System.currentTimeMillis() / 1000L);
        new_data.data = "A proxy log line...";
        datastore.addRawData(new_data);

        assertEquals(
                original_data.length + 1,
                datastore.findRawData(type, client, servername).length);
        server.stop();
    }

    /**
     *
     * @throws FileNotFoundException
     * @throws InvalidProfileException
     */
    public final void testReadWriteRawData() throws Throwable {
        System.out.println("addRawData, with a ReadWrite detection agent");

        // Start server with read-write activation profile
        server = new Server();
        InputStream activation_file = getClass()
                .getResourceAsStream("/detection.readwrite.yml");
        server.addDetectionAgentProfile(DetectionAgentProfile.fromInputStream(activation_file));
        server.start();

        Client datastore = new Client(new URL("http://127.0.0.1:8080"));
        RawData data = new RawData();
        data.label = "http";
        data.client = "1.2.3.4";
        data.server = "www.google.be";
        data.time = 1230987;
        data.data = "A proxy log line...";
        datastore.addRawData(data);
        server.stop();
    }

    public final void testInvalidConnection() throws Throwable {

        System.out.println("Test invalid connection");
        startDummyServer();

        Client datastore = null;
        try {
            datastore = new Client(new URL("http://123.45.67.89:8082"));
            datastore.test();
            fail("Should throw a SocketTimeoutException !");
            datastore.test();

        } catch (SocketTimeoutException ex) {
            assertEquals(
                    ex.getClass().getName(), "java.net.SocketTimeoutException");

        } finally {
            server.stop();
        }
    }
}
