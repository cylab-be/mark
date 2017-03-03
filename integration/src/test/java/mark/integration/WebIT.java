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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlHeading1;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.nio.file.Paths;
import junit.framework.TestCase;
import netrank.LinkAdapter;
import mark.server.Config;
import mark.server.Server;

/**
 *
 * @author Thibault Debatty
 */
public class WebIT extends TestCase {
    private WebClient client;
    private String base_url;
    private Server server;


    @Override
    public final void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    /**
     * Tests for the homepage.
     * - title is "Multi Agent..."
     */
    public final void testHomepage() throws Throwable {
        System.out.println("Test Homepage");
        System.out.println("=============");

        client = new WebClient();
        base_url = "http://127.0.0.1:8000/";

        Config config = Config.getTestConfig();
        config.start_webserver = true;
        config.setWebserverRoot(
                Paths.get("").toAbsolutePath().getParent().resolve("ui")
                        .toString());
        config.adapter_class = LinkAdapter.class.getName();
        server = new Server(config);
        server.start();

        client.getCache().clear();
        HtmlPage page = client.getPage(base_url);
        System.out.println(((HtmlHeading1) page.getByXPath("//h1").get(0)).getTextContent());

        client.getCache().clear();
        HtmlPage page3 = client.getPage(base_url);
        System.out.println(((HtmlHeading1) page3.getByXPath("//h1").get(0)).getTextContent());

        client.getCache().clear();
        HtmlPage page4 = client.getPage(base_url);
        System.out.println(((HtmlHeading1) page4.getByXPath("//h1").get(0)).getTextContent());

        client.getCache().clear();
        HtmlPage page2 = client.getPage(base_url);
        HtmlHeading1 h1 = (HtmlHeading1) page2.getByXPath("//h1").get(0);
        assertEquals(
                "Multi Agent Ranking Framework",
                h1.getTextContent());
    }
}