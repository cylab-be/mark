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

package mark.server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import mark.client.Client;
import mark.core.ServerInterface;
import mark.core.SubjectAdapter;
import org.bson.Document;

/**
 *
 * @author Thibault Debatty
 */
public class DummySubjectAdapter extends SubjectAdapter<DummySuject> {

    @Override
    public void writeToMongo(DummySuject subject, Document doc) {
        doc.append("name", subject.name);
    }

    @Override
    public DummySuject readFromMongo(Document doc) {
        DummySuject ds = new DummySuject();
        ds.name = doc.getString("name");
        return ds;
    }

    @Override
    public DummySuject getInstance() {
        return new DummySuject();
    }

    @Override
    public DummySuject deserialize(JsonNode node) {
        DummySuject ds = new DummySuject();
        ds.name = node.get("name").toString();
        return ds;
    }

    @Override
    public DummySuject deserialize(
            final JsonParser jparser,final DeserializationContext ctx)
            throws IOException, JsonProcessingException {
        return deserialize((JsonNode) jparser.getCodec().readTree(jparser));
    }

    @Override
    public ServerInterface<DummySuject> getDatastore(String url) {
        try {
            return new Client<DummySuject>(new URL(url), this);
        } catch (MalformedURLException ex) {
            Logger.getLogger(DummySubjectAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
