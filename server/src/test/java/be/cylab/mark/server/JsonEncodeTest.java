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

package be.cylab.mark.server;

import be.cylab.mark.webserver.Point;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * Test generation of JSON points for the history chart.
 *
 * To run only these tests:
 * mvn test -Dtest=JsonEncodeTest
 * @author Thibault Debatty
 */
public class JsonEncodeTest extends TestCase {

    /**
     * Test we correctly parse a .yml config file
     */
    public void testJsonEncode() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        Point point = new Point(12445, 0.2);

        String json = objectMapper.writeValueAsString(point);
        System.out.println(json);


        List<Point> points = new ArrayList<>();
        points.add(new Point(12345, 0.2));
        points.add(new Point(12356, 0.8));
        System.out.println(objectMapper.writeValueAsString(points));

    }
}

