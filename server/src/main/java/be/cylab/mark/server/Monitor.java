/*
 * The MIT License
 *
 * Copyright 2019 tibo.
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
package be.cylab.mark.server;

import be.cylab.mark.core.ServerInterface;
import be.cylab.mark.datastore.Datastore;
import com.mongodb.client.MongoCollection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bson.Document;

/**
 *
 * @author tibo
 */
public class Monitor implements Runnable {

    private final Datastore datastore;

    public Monitor(Datastore datastore) {
        this.datastore = datastore;
    }

    @Override
    public void run() {

        ServerInterface server = datastore.getRequestHandler();
        MongoCollection collection = datastore.getMongodb().getCollection("statistics");

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                break;
            }

            try {
                Map<String, Object> status = server.status();
                status.put("time", System.currentTimeMillis());


                collection.insertOne(new Document(sanitize(status)));
            } catch (Throwable ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    private Map<String, Object> sanitize(Map<String, Object> orig) {
        Map<String, Object> result = new HashMap<>();

        for (Entry<String, Object> entry : orig.entrySet()) {
            result.put(sanitize(entry.getKey()), entry.getValue());
        }

        return result;

    }

    private String sanitize(String s) {
        return s.replace(".", "_");
    }

}
