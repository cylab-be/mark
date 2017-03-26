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

package mark.datastore;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import junit.framework.TestCase;
import mark.core.RawData;
import mark.server.Config;
import mark.server.DummySubjectAdapter;
import mark.server.DummySuject;
import org.bson.Document;

/**
 *
 * @author Thibault Debatty
 */
public class RequestHandlerTest extends TestCase {

    public RequestHandlerTest(String testName) {
        super(testName);
    }

    public void testFindData() {
        MongoClient mongo = new MongoClient();
        MongoDatabase mongodb = mongo.getDatabase("MARK");
        mongodb.drop();

        RequestHandler handler = new RequestHandler(
                mongodb,
                new DummyActivationContoller(),
                new DummySubjectAdapter());

        RawData<DummySuject> data = new RawData<>();
        data.data = "1234";
        data.subject = new DummySuject("test");
        handler.addRawData(data);
        handler.addRawData(data);

        Document query = new Document("DATA", "1234");
        RawData[] result = handler.findData(query);
        assertEquals(2, result.length);
    }


}
