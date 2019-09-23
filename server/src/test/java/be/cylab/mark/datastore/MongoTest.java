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

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import org.bson.Document;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

/**
 * https://github.com/mongodb/mongo-java-driver/blob/master/driver-sync/src/examples/tour/PojoQuickTour.java
 *
 * @author Thibault Debatty
 */
public class MongoTest extends TestCase {

    public String mongo_host;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mongo_host = System.getenv("MARK_MONGO_HOST");
        if (mongo_host == null) {
            mongo_host = "127.0.0.1";
        }
    }

    /**
     * Typed classes (like Evidence<T extends Subject>) cannot be saved using
     * the automatic PojoCodeProvider.
     *
     */
    public void testTypedClasses() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Arrays.asList(new ServerAddress(mongo_host))))
                .build();

        MongoClient mongo = MongoClients.create(settings);
        MongoDatabase db = mongo.getDatabase("mytestdb");
        MongoCollection<Document> coll = db.getCollection("tests");
        coll.drop();

        Test<String> t = new Test<>("mytest");
        t.setData("my data...");
        t.references().add("abc");
        t.references().add("def");

        Document doc = new Document();
        t.toDocument(doc);
        coll.insertOne(doc);

        Document first = coll.find().first();
        System.out.println("In Mongo: " + first);

        Test fetched = new Test();
        fetched.fromDocument(first);
        System.out.println("Parsed: " + fetched);

    }

    /**
     * Insert / count simple documents.
     */
    public void testDocument() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Arrays.asList(new ServerAddress(mongo_host))))
                .build();

        MongoClient mongo = MongoClients.create(settings);
        MongoDatabase db = mongo.getDatabase("mytestdb");
        MongoCollection<Document> coll = db.getCollection("tests");
        coll.drop();

        Document doc = new Document().append("name", "Tibo");
        coll.insertOne(doc);
        System.out.println(coll.countDocuments());

    }

    public void testPojo() {

        CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .applyToClusterSettings(builder ->
                        builder.hosts(Arrays.asList(new ServerAddress(mongo_host))))
                .build();

        MongoClient mongo = MongoClients.create(settings);
        MongoDatabase db = mongo.getDatabase("mytestdb");

        MongoCollection<Person> collection
                = db.getCollection("people", Person.class);
        collection.drop();

        Person me = new Person();
        me.setName("Tibo");

        // a simple array of String is not serialized automatically...
        // => we have to use a list
        me.references.add("abc");
        me.references.add("def");
        collection.insertOne(me);

        System.out.println("Mutated Person Model: " + me);

        Person first = collection.find().first();
        System.out.println("In MongoDB: " + first);

    }
}

class Test<T> {
    private String name;
    private T data;
    private List<String> references = new ArrayList<>();

    public Test(String name) {
        this.name = name;
    }

    Test() {

    }

    public void setData(T data) {
        this.data = data;
    }

    public List<String> references() {
        return references;
    }

    public Document toDocument(Document doc) {
        doc.append("name", name);
        doc.append("data", data);
        doc.append("references", references);

        return doc;
    }

    public void fromDocument(Document doc) {
        this.name = doc.get("name", String.class);
        this.references = doc.get("references", references);

        // data cannot be parsed as we don't know the runtime type...
        //this.data = doc.get("data", data);

    }

    @Override
    public String toString() {
        return name + " : " + data + " : " + references;
    }
}
