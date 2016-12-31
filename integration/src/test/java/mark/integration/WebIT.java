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

package mark.integration;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlHeading1;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import junit.framework.TestCase;
import mark.masfad2.Link;
import mark.masfad2.LinkAdapter;
import mark.server.Server;

/**
 *
 * @author Thibault Debatty
 */
public class WebIT extends TestCase {
    private WebClient client;
    private String base_url;
    private Server<Link> server;

    @Override
    public final void setUp() throws Exception {
        client = new WebClient();
        base_url = "http://127.0.0.1:8000/";

        server = new Server<Link>(new LinkAdapter());
        server.start();
    }

    @Override
    public final void tearDown() throws Exception {
        server.stop();
    }

    /**
     * Tests for the homepage.
     * - title is "Multi Agent..."
     */
    public final void testHomepage() throws IOException {
        System.out.println("Test Homepage");
        System.out.println("=============");
        HtmlPage page = client.getPage(base_url);
        System.out.println(page.getByXPath("//h1").get(0));
        HtmlHeading1 h1 = (HtmlHeading1) page.getByXPath("//h1").get(0);
        assertEquals(
                "Multi Agent Ranking Framework",
                h1.getTextContent());
    }
}