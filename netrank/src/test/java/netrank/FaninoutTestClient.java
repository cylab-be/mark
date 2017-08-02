/*
 * The MIT License
 *
 * Copyright 2017 georgi.
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

import java.util.LinkedList;
import java.util.Random;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.Subject;

/**
 *
 * @author georgi
 * @param <T>
 */
public class FaninoutTestClient<T extends Subject> 
        extends ExtendedDummyClient<T> {

    private static final int N_SERVER = 10;
    private LinkedList<Evidence> evidence = new LinkedList<>();
    private final int N_IP_TO_DOMAIN;
    private final int N_DOMAIN_TO_IP;
    // IF GENERATE_IPS = true --> generate random IPs
    // IF GENERATE_IPS = false --> generate random DOMAINs
    private final boolean GENERATE_IPS;

    // Simulate an APT that connects every 60 seconds => f = 0.0166 Hz
    private static final int APT_INTERVAL = 60;
    private static final String APT_SERVER = "105.244.103.";
    private static final String SERVER = "175.193.216.10";

    public FaninoutTestClient(final boolean type,
                                final int number_of_ips,
                                final int number_of_domains) {
        this.N_IP_TO_DOMAIN = number_of_ips;
        this.N_DOMAIN_TO_IP = number_of_domains;
        this.GENERATE_IPS = type;
    }

    // method for generating random Urls for the generateData method where one
    // IP is linked to a multitude of different domains.
    private String generateRandomUrl() {
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyz1234567890";
        String[] domain_suffix = {"com", "net", "it", "org", "lt", "ac"};
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 6) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        String result = "http://www." + saltStr + "."
                + domain_suffix[rnd.nextInt(domain_suffix.length)]
                + "/testDomainName"; 
        return result;
    }

    private RawData[] generateData(String type, Link subject) {
        int start = 123456;
        Random rand = new Random();
        int extra_logs_to_add;
        RawData[] data;

        if (GENERATE_IPS){
            extra_logs_to_add = N_IP_TO_DOMAIN;
            data = new RawData[N_SERVER + extra_logs_to_add];

            for (int i = 0; i < extra_logs_to_add; i++) {
                data[i] = new RawData();
                data[i].subject = subject;
                data[i].label = type;
                data[i].time = start + APT_INTERVAL * i;
                data[i].data = data[i].time + "    "
                        + "126 "
                        + "198.36.158.8 "
                        + "TCP_MISS/"
                        + "400"
                        + "918 GET "
                        + "http://lyfqnr.owvcq.wf/jbul.html - DIRECT/"
                        + APT_SERVER + Integer.toString(i)
                        + " text/html";
            }
        } else {
            extra_logs_to_add = N_DOMAIN_TO_IP;
            data = new RawData[N_SERVER + extra_logs_to_add];

            for (int i = 0; i < extra_logs_to_add; i++) {
                data[i] = new RawData();
                data[i].subject = subject;
                data[i].label = type;
                data[i].time = start + APT_INTERVAL * i;
                data[i].data = data[i].time + "    "
                        + "126 "
                        + "198.36.158.8 "
                        + "TCP_MISS/"
                        + "400"
                        + "918 GET "
                        + generateRandomUrl()
                        + " - DIRECT/"
                        + APT_SERVER + "10"
                        + " text/html";
            }
        }

        // Add a few random requests
        for (int i = extra_logs_to_add; i < extra_logs_to_add + N_SERVER; i++) {
            data[i] = new RawData();
            data[i].subject = subject;
            data[i].label = type;
            data[i].time = start + rand.nextInt(5 * APT_INTERVAL);
            data[i].data = data[i].time + "    "
                    + "126 "
                    + "198.36.158.8 "
                    + "TCP_MISS/"
                    + "200"
                    + "918 GET "
                    + "http://www.github.com/jbul.html - DIRECT/"
                    + SERVER
                    + " text/html";
        }

        return data;
    }

    @Override
    public RawData[] findRawData(String type, T subject)
            throws Throwable {

        RawData[] result = generateData(type, (Link) subject);
        //System.out.println("DEBUG: " + result.length);
        return result;
    }

    @Override
    public void addEvidence(Evidence evidence) throws Throwable {
        System.out.println(evidence);
        this.evidence.add(evidence);
    }

    @Override
    public Evidence[] findEvidence(String label) {
        return new Evidence[1];
    }

    @Override
    public LinkedList<Evidence> getEvidences() throws Throwable {
        return this.evidence;
    }
    
}
