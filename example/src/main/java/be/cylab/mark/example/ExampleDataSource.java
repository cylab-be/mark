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
package be.cylab.mark.example;

import be.cylab.mark.core.DataAgentInterface;
import be.cylab.mark.core.DataAgentProfile;
import be.cylab.mark.core.RawData;
import be.cylab.mark.core.ServerInterface;
import java.util.Random;

/**
 * A dummy data source, for demo and testing purpose.
 *
 * This data source accepts 1 optional parameter:
 *
 * <ul>
 * <li>count (int) : number of data objects to submit (default = 0 = unlimited)
 * </li>
 * </ul>
 *
 * @author tibo
 */
public class ExampleDataSource implements DataAgentInterface {

    private final String[] names =
            {"Tibo", "Wim", "Georgi", "Alex", "Fred", "Paloma", "Zac"};
    private final Random rand = new Random();
    private volatile boolean must_stop = false;

    @Override
    public final void run(
            final DataAgentProfile profile, final ServerInterface datastore)
            throws Throwable {

        int count = profile.getParameterInt("count", 0);
        int i = 0;

        while (true) {

            if (must_stop) {
                break;
            }

            Subject subject = new Subject(
                    names[rand.nextInt(names.length)]);

            RawData data = new RawData();
            data.setData("Some data...");
            data.setSubject(subject);
            data.setTime(System.currentTimeMillis());
            data.setLabel("data.dummy");
            datastore.addRawData(data);

            Thread.sleep(10000);

            i++;
            if (count > 0 && i >= count) {
                break;
            }
        }
    }

    @Override
    public final void stop() {
        this.must_stop = true;
    }

}
