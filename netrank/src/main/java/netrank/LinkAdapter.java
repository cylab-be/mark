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

package netrank;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.URL;
import mark.client.Client;
import mark.core.ServerInterface;
import mark.core.SubjectAdapter;
import org.bson.Document;

/**
 *
 * @author Thibault Debatty
 */
public class LinkAdapter extends SubjectAdapter<Link> {

    private static final String FIELD_CLIENT = "CLIENT";
    private static final String FIELD_SERVER = "SERVER";

    @Override
    public final Link deserialize(
            final JsonParser jparser,
            final DeserializationContext context)
            throws IOException, JsonProcessingException {

        JsonNode tree = jparser.getCodec().readTree(jparser);
        return deserialize(tree);

    }

    /**
     *
     * @param link
     * @param doc
     */
    public final void writeToMongo(final Link link, final Document doc) {
        doc.append(FIELD_CLIENT, link.getClient());
        doc.append(FIELD_SERVER, link.getServer());
    }

    /**
     *
     * @param doc
     * @return
     */
    public final Link readFromMongo(final Document doc) {
        return new Link(
                doc.getString(FIELD_CLIENT),
                doc.getString(FIELD_SERVER));
    }

    /**
     *
     * @param node
     * @return
     */
    public final Link deserialize(final JsonNode node) {
        return new Link(
                node.get("client").textValue(),
                node.get("server").textValue());
    }

    @Override
    public final ServerInterface<Link> getDatastore(final URL url) {
        return new Client<Link>(url, this);
    }
}
