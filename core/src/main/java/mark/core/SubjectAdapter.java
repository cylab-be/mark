/*
 * The MIT License
 *
 * Copyright 2016 Thibault Debatty.
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

package mark.core;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URL;
import org.bson.Document;

/**
 * The SubjectAdapter is the adapter between the data that will be used
 * by the running server.
 *
 * It has four goals:
 * - Provide the implementation code to serialize and deserialize the subject
 *   to/from Mongo
 * - Provide the implementation code to deserialize the subject from JSON/RPC
 *   calls
 * - Provide the correct datastore link to detection agents (Factory pattern)
 * - Provide a single instance of the subject, which will be used to test the
 *   detection agent profiles before the server starts.
 * @author Thibault Debatty
 */
public abstract class SubjectAdapter<T extends Subject> extends JsonDeserializer<T> {

    public abstract void writeToMongo(T subject, Document doc);
    public abstract T readFromMongo(Document doc);
    public abstract T deserialize(JsonNode node);

    public abstract T getInstance();
    public abstract ServerInterface<T> getDatastore(URL url);
}
