package be.cylab.mark.integration;

import be.cylab.mark.activation.ActivationController;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.client.Client;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import java.util.HashMap;
import org.bson.Document;

/**
 * The server must be compiled and started before we can run this test =>
 * integration test (and not unit test).
 *
 * @author Thibault Debatty
 */
public class ClientIT extends MarkCase {

    /**
     * Test of Test method, of class DatastoreClient.
     *
     * @throws java.lang.Throwable
     */
    public final void testTest() throws Throwable {
        System.out.println("test");
        System.out.println("====");

        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"));
        assertEquals("1", datastore.test());
    }

    public final void testStatus() throws Throwable {
        System.out.println("status");
        System.out.println("====");

        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"));
        System.out.println(datastore.status());
    }

    /**
     *
     * @throws java.lang.Throwable
     */
    public final void testString() throws Throwable {
        System.out.println("testString");
        System.out.println("==========");

        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"));
        datastore.testString("My String");
    }

    /**
     *
     * @throws Throwable
     */
    public final void testAddFindRawData() throws Throwable {
        System.out.println("addRawData and findRawData");
        System.out.println("==========================");

        String label = "http";
        Link link = new Link("1.2.3.4", "www.google.be");

        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"));
        RawData[] original_data = datastore.findRawData(
                label, link, 0, System.currentTimeMillis());

        RawData new_data = new RawData();
        new_data.setLabel(label);
        new_data.setSubject(link);
        new_data.setTime(System.currentTimeMillis());
        new_data.setData("A proxy log line...");
        datastore.addRawData(new_data);

        RawData[] final_data = datastore.findRawData(
                label, link, 0, System.currentTimeMillis());

        assertEquals(
                original_data.length + 1,
                final_data.length);

        assertEquals(label, final_data[0].getLabel());

        assertEquals(
                "A proxy log line...",
                final_data[0].getData());
    }

    /**
     * Test findRawData(Document).
     */
    public final void testFindDataWithBson() throws Throwable {
        System.out.println("findRawData(Bson.Document)");
        System.out.println("==========================");

        String label = "data.http";
        String client = "1.2.3.4";

        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"));

        RawData data = new RawData();
        data.setLabel(label);
        data.setSubject(new Link(client, "some.server"));
        data.setTime(123456);
        datastore.addRawData(data);

        data.setSubject(new Link(client, "some.other.server"));
        data.setTime(456789);
        datastore.addRawData(data);

        HashMap<String, String> query = new HashMap<>();
        query.put("client", client);

        RawData[] result = datastore.findRawData(
                label,
                query,
                0,
                999999);
        assertEquals(2, result.length);
    }

    /**
     *
     * @throws Throwable
     */
    public final void testActivation() throws Throwable {
        System.out.println("addRawData, with a ReadWrite detection agent");
        System.out.println("============================================");

        // Start server with read-write activation profile
        ActivationController activation_controller = getActivationController();
        activation_controller.setAgentProfile(
                DetectionAgentProfile.fromInputStream(
                        getClass()
                                .getResourceAsStream("/detection.readwrite.yml")));

        // Count the original number of evidences
        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"));
        int original_count = datastore
                .findEvidence(
                        "detection.rw", new Link("1.2.3.4", "www.google.be")).length;

        // add a data, which should trigger the rw detector
        RawData data = new RawData();
        data.setLabel("data.http");
        data.setSubject(new Link("1.2.3.4", "www.google.be"));
        data.setTime(1230987);
        data.setData("A proxy log line...");
        datastore.addRawData(data);

        // Add it twice... detector should be triggered only once...
        data.setTime(1234567);
        datastore.addRawData(data);

        Thread.sleep(1000);
        getTestServer().awaitTermination();

        int final_count = datastore
                .findEvidence(
                        "detection.rw", new Link("1.2.3.4", "www.google.be")).length;

        assertEquals(original_count + 2, final_count);
    }

    /**
     * We are testing if an object stored in cache is the same when we retrieve
     * it. Testing with two differents objects. (String and ArrayList)
     *
     * @throws Throwable
     */
    public final void testStoreInAndGetFromCache() throws Throwable {
        System.out.println("storeInCache, get from cache");
        System.out.println("============================");

        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"));

        String key1 = "key1";
        String key2 = "key2";
        String value1 = "value";
        ArrayList value2 = new ArrayList();

        datastore.storeInCache(key1, value1);
        datastore.storeInCache(key2, value2);

        String retrieved_value1 = (String) datastore.getFromCache(key1);
        ArrayList retrieved_value2 = (ArrayList) datastore.getFromCache(key2);

        assertEquals(value1, retrieved_value1);
        assertEquals(value2, retrieved_value2);
    }

    /**
     * We are testing the CompareAndSwapInCache method. Testing with a good old
     * value and a bad old value.
     *
     * @throws Throwable
     */
    public final void testCompareAndSwapInCache() throws Throwable {
        System.out.println("Compare and swap in cache");
        System.out.println("=========================");

        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"));

        String key1 = "key1";
        String value1 = "value";

        datastore.storeInCache(key1, value1);

        String retrieved_value1 = (String) datastore.getFromCache(key1);
        assertTrue(datastore.compareAndSwapInCache(
                key1, "new value", retrieved_value1));
        assertFalse(datastore.compareAndSwapInCache(
                key1, "new value again", retrieved_value1));

    }

    public final void testFindEvidence() throws Throwable {
        System.out.println("findEvidence, test we get the most recent");
        System.out.println("=========================================");

        // Count the original number of evidences
        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"));

        String label = "my.test";
        assertEquals(0, datastore.findEvidence(label).length);

        Evidence evidence = new Evidence();
        Link subject = new Link("1.2.3.4", "test.me");
        evidence.setLabel(label);
        evidence.setScore(0.9);
        evidence.setTime(1234);
        evidence.setSubject(subject);
        datastore.addEvidence(evidence);

        // After some time, score decreases
        evidence.setScore(0.8);
        evidence.setTime(2345);
        datastore.addEvidence(evidence);

        // Ask for last evidences
        Evidence[] evidences = datastore.findEvidence(label);
        assertEquals(1, evidences.length);
        assertNotSame("", evidences[0].getId());

        // Check it is indeed the most recent report
        assertEquals(2345, evidences[0].getTime());
    }

    public final void testFindEvidenceSince() throws Throwable {
        System.out.println("findEvidenceSince");
        System.out.println("=================");

        // Count the original number of evidences
        Client datastore = new Client(
                new URL("http://127.0.0.1:8080"));

        String label = "my.test";

        Evidence evidence = new Evidence();
        Link subject = new Link("1.2.3.4", "test.me");
        evidence.setLabel(label);
        evidence.setScore(0.9);
        evidence.setTime(12345);
        evidence.setSubject(subject);
        datastore.addEvidence(evidence);

        evidence.setTime(12000);


        // Ask for last evidences
        Evidence[] evidences = datastore.findEvidenceSince(
                label, subject, 12300);

        assertEquals(1, evidences.length);
        assertEquals(12345, evidences[0].getTime());
    }



    /**
     *
     * @throws Throwable
     */
    public final void testInvalidConnection() throws Throwable {
        System.out.println("Test invalid connection");
        System.out.println("=======================");

        try {
            Client datastore = new Client(
                    new URL("http://127.0.0.1:1555"));
            datastore.test();
            fail("Should throw a SocketTimeoutException !");
            datastore.test();

        } catch (SocketTimeoutException ex) {
            assertEquals(
                    ex.getClass().getName(), "java.net.SocketTimeoutException");

        } catch (SocketException ex) {
            //  assertEquals(
            //          ex.getClass().getName(), "java.net.SocketException");
            assertTrue(true);
        }
    }
}
