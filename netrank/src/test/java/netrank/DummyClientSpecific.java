/*
 * The MIT License
 *
 * Copyright 2017 Georgi Nikolov.
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

import mark.activation.DummyClient;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.Subject;


public class DummyClientSpecific<T extends Subject> extends DummyClient<T> {

    private static final int N_APT = 5500;
    private static final int N_NOISE = 5500;
    // Simulate an APT that connects every 60 seconds => f = 0.0166 Hz
    private static final int APT_INTERVAL = 60;
    private Evidence[] evidence_array = new Evidence[1];

    @Override
    public RawData[] findRawData(String type, T subject)
            throws Throwable {

        System.out.println("DEBUG PARAMETERS: " + type + "\n");
        System.out.println("DEBUG PARAMETERS: " + subject + "\n");
        
        if (subject.toString().equals("  :  ")) {
            return new RawData[0];
        }
        
        int start = 123456;

        RawData[] data = new RawData[N_APT + N_NOISE];

        for (int i = 0; i < N_APT + N_NOISE; i++) {
            data[i] = new RawData();
            data[i].subject = subject;
            data[i].label = type;
            data[i].time = start + APT_INTERVAL * i;

            if ( (i & 1) == 0 ) {
                data[i].data = data[i].time + "    "
                    + "126 "
                    + "198.36.158.8 "
                    + "TCP_MISS/"
                    + "200"
                    + "918 GET "
                    + "http://lyfqnr.owvcq.wf/jbul.html - DIRECT/"
                    + "175.193.216.231 text/html";
            } else {
                data[i].data = data[i].time + "    "
                        + "126 "
                        + "198.36.158.8 "
                        + "TCP_MISS/"
                        + "400"
                        + "918 GET "
                        + "http://lyfqnr.owvcq.wf/jbul.html - DIRECT/"
                        + "175.193.216.231 text/html";
            }            
        }

        return data;
    }

    public void addEvidence(Evidence evidence) throws Throwable {
        System.out.println(evidence);
        evidence_array[0] = evidence;
    }

    public void getEvidence() throws Throwable {
        for (int i = 0; i < evidence_array.length; i++){
            System.out.println(evidence_array[i]);
        }
    }
}
