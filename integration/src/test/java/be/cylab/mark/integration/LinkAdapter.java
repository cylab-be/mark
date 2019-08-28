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

package be.cylab.mark.integration;

import com.fasterxml.jackson.databind.JsonNode;
import be.cylab.mark.core.SubjectAdapter;
import org.bson.Document;

/**
 *
 * @author Thibault Debatty
 */
public class LinkAdapter implements SubjectAdapter<Link> {

    /**
     * Field used to store the client in Mongo.
     */
    public static final String CLIENT = "CLIENT";

    /**
     * Field use to store the server in Mongo.
     */
    public static final String SERVER = "SERVER";

    /**
     * {@inheritDoc}
     * @param link
     * @param doc
     */
    @Override
    public final void writeToMongo(final Link link, final Document doc) {
        doc.append(CLIENT, link.getClient());
        doc.append(SERVER, link.getServer());
    }

    /**
     * {@inheritDoc}
     * @param doc
     * @return
     */
    @Override
    public final Link readFromMongo(final Document doc) {
        return new Link(
                doc.getString(CLIENT),
                doc.getString(SERVER));
    }

    /**
     * {@inheritDoc}
     * @param node
     * @return
     */
    @Override
    public final Link deserialize(final JsonNode node) {
        return new Link(
                node.get("client").textValue(),
                node.get("server").textValue());
    }
}
