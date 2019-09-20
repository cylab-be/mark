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
package be.cylab.mark.datastore;

import be.cylab.mark.core.RawData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import junit.framework.TestCase;

/**
 *
 * @author tibo
 */
public class JsonRPCTest extends TestCase {

    public void testStringRequest() throws IOException, Throwable {
        String query = "{\"id\":\"1353101991\",\"jsonrpc\":\"2.0\",\"method\":\"findRawData\",\"params\":[\"data.http\",{\"client\":\"1.2.3.4\",\"server\":\"www.google.be\"}]}";
        System.out.println(query);

        ObjectNode node = new ObjectMapper().readValue(query, ObjectNode.class);
        System.out.println(node);

        assertEquals(query, node.toString());

        JsonRpcHttpClient client = new JsonRpcHttpClient(new URL("http://127.0.0.1:8000"));
        try {
            RawData[] data = client.invoke(node, RawData[].class, new HashMap<>());
            fail("Should throw an exception");
        } catch (java.net.ConnectException ex) {
            return;
        }

        fail("Should throw an exception");
    }
}
