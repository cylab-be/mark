/*
 * The MIT License
 *
 * Copyright 2017 Thibault Debatty.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package be.cylab.mark.datastore;

import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Evidence;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import junit.framework.TestCase;
import be.cylab.mark.core.RawData;
import be.cylab.mark.server.Config;
import be.cylab.mark.server.DummySubjectAdapter;
import be.cylab.mark.server.DummySubject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.bson.Document;

/**
 *
 * @author Thibault Debatty
 */
public class RequestHandlerTest extends TestCase {

    public void testFindData() {

        RequestHandler rq = this.getRequestHandler();

        RawData<DummySubject> data = new RawData<>();
        data.setData("1234");
        data.setSubject(new DummySubject("test"));
        rq.addRawData(data);

        assertNotSame("", data.getId());

        rq.addRawData(data);

        Document query = new Document("DATA", "1234");
        RawData[] result = rq.findData(query);
        assertEquals(2, result.length);
    }

    public void testFindEvidence() throws Throwable {

        RequestHandler rq = this.getRequestHandler();

        Evidence ev = new Evidence();
        ev.setLabel("test");
        ev.setReport("Some report...");
        ev.setScore(0.99);
        ev.setSubject(new DummySubject("test"));
        ev.setTime(123456);
        rq.addEvidence(ev);

        Evidence[] evidences = rq.findEvidence("test");
        assertEquals(1, evidences.length);
        assertNotSame("", evidences[0].getId());
    }

    public void testFindEvidenceSince() throws Throwable {
        RequestHandler rq = this.getRequestHandler();

        String label = "test";
        DummySubject subject = new DummySubject("test");

        Evidence ev = new Evidence();
        ev.setLabel(label);
        ev.setReport("Some report...");
        ev.setScore(0.99);
        ev.setSubject(subject);
        ev.setTime(123456);
        rq.addEvidence(ev);

        ev.setTime(123000);
        rq.addEvidence(ev);

        Evidence[] evidences = rq.findEvidenceSince(label, subject, 0);
        assertEquals(2, evidences.length);

        evidences = rq.findEvidenceSince(label, subject, 123400);
        assertEquals(1, evidences.length);

        evidences = rq.findEvidenceSince(label, subject, 123500);
        assertEquals(0, evidences.length);
    }

    public void testfindDistinctEntries() throws Throwable {
        RequestHandler rq = this.getRequestHandler();
        String label_field = "LABEL";
        String name_field = "name";

        Evidence ev = new Evidence();
        ev.setLabel("test");
        ev.setReport("Some report...");
        ev.setScore(0.99);
        ev.setSubject(new DummySubject("test"));
        ev.setTime(123456);
        rq.addEvidence(ev);

        ev.setLabel("test2");
        ev.setSubject(new DummySubject("test"));
        rq.addEvidence(ev);

        String[] distinct_labels = rq.findDistinctEntries(label_field);
        assertEquals(2, distinct_labels.length);

        String[] distinct_subjects = rq.findDistinctEntries(name_field);
        assertEquals(1, distinct_subjects.length);
    }

    private RequestHandler getRequestHandler() {
        String mongo_host = System.getenv(Config.ENV_MONGO_HOST);
        if (mongo_host == null) {
            mongo_host = "127.0.0.1";
        }

        MongoClient mongo = new MongoClient(mongo_host);
        MongoDatabase mongodb = mongo.getDatabase("MARK");
        mongodb.drop();

        RequestHandler handler = new RequestHandler(
                mongodb,
                new DummyActivationContoller(),
                new DummySubjectAdapter());

        return handler;
    }

    /**
     * Test that the profile field of an evidence (that contains the
     * detection agent profile of the detector) is correctly stored in mongodb.
     *
     */
    public void testEvidenceProfile() throws Throwable {
        System.out.println("testEvidenceProfile");
        RequestHandler rq = getRequestHandler();

        DetectionAgentProfile profile = new DetectionAgentProfile();
        profile.setClassName("mark.detector");
        profile.setLabel("save.label");
        profile.setTriggerLabel("trigger.label");
        profile.getParameters().put("key", "value");

        Evidence<DummySubject> ev = new Evidence<>();
        ev.setLabel("test");
        ev.setSubject(new DummySubject("me"));
        ev.setProfile(profile);
        rq.addEvidence(ev);

        Evidence other = rq.findEvidence("test", new DummySubject("me"))[0];
        assertEquals("mark.detector", other.getProfile().getClassName());
        assertEquals("value", other.getProfile().getParameter("key"));
    }

    /**
     * Jackson is the library used by jsonrpc4j to convert Java objects
     * to and from json.
     */
    public void testJacksonMappings() throws JsonProcessingException, IOException {

        Evidence ev = new Evidence();
        ev.setId("123456789");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(ev);
        System.out.println(json);
        Evidence result = mapper.readValue(json, Evidence.class);
        assertEquals(ev.getId(), result.getId());
    }

    public void testBsonMappings() {
        Document doc = new Document();
        doc.append("name", "test");
        doc.append("profile", new Document().append("class", "a.b.c.d"));

        assertEquals(
                "a.b.c.d",
                ((Document) doc.get("profile")).get("class"));


    }
}