package mark.integration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import junit.framework.TestCase;
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

    private Server masfad_server;

    protected final void startDummyServer() throws Exception {
        masfad_server = new Server();

        // Activate the dummy activation profiles
        InputStream activation_file = getClass()
                .getResourceAsStream("/activation-dummy.yml");
        masfad_server.setActivationProfiles(activation_file);
        masfad_server.start();
    }

    /**
     * Test of Test method, of class DatastoreClient.
     * @throws java.lang.Exception
     */
    public final void testTest() throws Exception {
        System.out.println("test");
        startDummyServer();
        Client datastore = new Client();
        assertEquals("1", datastore.test());
        masfad_server.stop();
    }

    /**
     *
     */
    public final void testString() throws Exception {
        System.out.println("testString");
        startDummyServer();
        Client datastore = new Client();
        datastore.testString("My String");
        masfad_server.stop();
    }

    /**
     * Test of Test method, of class DatastoreClient.
     */
    public final void testAddRawData() throws Exception {
        System.out.println("addRawData");

        startDummyServer();
        Client datastore = new Client();
        RawData data = new RawData();
        data.type = "http";
        data.client = "1.2.3.4";
        data.server = "www.google.be";
        data.time = 1230987;
        data.data = "A proxy log line...";
        datastore.addRawData(data);
        masfad_server.stop();
    }

    public final void testAddFindRawData() throws Exception {
        System.out.println("addRawData and findRawData");

        startDummyServer();
        String type = "http";
        String client = "1.2.3.4";
        String server = "www.google.be";

        Client datastore = new Client();
        RawData[] original_data =
                datastore.findRawData(type, client, server);

        RawData new_data = new RawData();
        new_data.type = type;
        new_data.client = client;
        new_data.server = server;
        new_data.time = (int) (System.currentTimeMillis() / 1000L);
        new_data.data = "A proxy log line...";
        datastore.addRawData(new_data);

        assertEquals(
                original_data.length + 1,
                datastore.findRawData(type, client, server).length);
        masfad_server.stop();
    }

    public final void testReadWriteRawData()
            throws FileNotFoundException, Exception {
        System.out.println("addRawData, with a ReadWrite detection agent");

        // Start server with read-write activation profile
        masfad_server = new Server();
        InputStream activation_file = getClass()
                .getResourceAsStream("/activation-readwrite.yml");
        masfad_server.setActivationProfiles(activation_file);
        masfad_server.start();

        Client datastore = new Client();
        RawData data = new RawData();
        data.type = "http";
        data.client = "1.2.3.4";
        data.server = "www.google.be";
        data.time = 1230987;
        data.data = "A proxy log line...";
        datastore.addRawData(data);
        masfad_server.stop();
    }
}
