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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;

/**
 *
 * @author georgi
 */
public class Faninout implements DetectionAgentInterface<Link> {

    private static final int THRESHOLD = 60;

    /**
     * method for parsing the data recieved from the database and constructing
     * a hashmap with IP-domain pairs.
     * @param rawdata recieved from the database.
     * @return hmap HashMap with IP-domain and Domain-IP key-value pairs for
     * each unique domain and IP and their relative IP/domain pairs.
     */
    private HashMap<String, LinkedList<String>> parseDomainIp(
                                                    final RawData[] rawdata) {
        HashMap<String, LinkedList<String>> hmap = new HashMap<>();
        Pattern pattern_ip = Pattern.compile("DIRECT/(\\b(?:(?:25[0-5]|2[0-4]"
                + "[0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]"
                + "|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b)");
        Pattern pattern_domain = Pattern.compile("[^:/?#]+:?//([^/?#]*)?"
                + "([^?#]*)(\\\\?[^#]*)?(#.*)?");
        for (RawData data: rawdata) {
            RawData current = data;
            String log = current.data;
            Matcher matcher_ip = pattern_ip.matcher(log);
            Matcher matcher_domain = pattern_domain.matcher(log);

            // if no IP or domain name in the log file discard and continue
            if (!matcher_ip.find() || !matcher_domain.find()) {
                continue;
            }

            String ip = matcher_ip.group(1);
            String domain = matcher_domain.group(1);
            // get a list of all the ips linked to the specific domain
            LinkedList<String> ips = hmap.get(domain);
            // get a list of all the domains linked to the specific ip
            LinkedList<String> domains = hmap.get(ip);
            // if the list of IPs for the specific domain is not empty
            if (ips != null) {
                // check if the IP is already in the list
                if (!ips.contains(ip)) {
                    // if its not in the list add it to the list
                    hmap.get(domain).add(ip);
                }
            // if the list is empty --> no key for the specific domain
            } else {
                // create a new entry for the domain with empty list
                hmap.put(domain, new LinkedList<String>());
                // add the IP to the list for the specific domain
                hmap.get(domain).add(ip);
            }
            // if the list of domains for the specific IP is not empty
            if (domains != null) {
                // check if the domain is already in the list
                if (!domains.contains(domain)) {
                    // if its not in the list add it to the list
                    hmap.get(ip).add(domain);
                }
            // if the list is empty --> no key for the specific IP
            } else {
                // add an entry for the IP with a new empty list
                hmap.put(ip, new LinkedList<String>());
                // add the domain to the list for the specific IP
                hmap.get(ip).add(domain);
            }
        }
        return hmap;
    }

    @Override
    public final void analyze(
            final Link subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
            actual_trigger_label, subject);


        // get the parsed data in a hashmap format with keys for each
        // encountered domain and IP
        HashMap<String, LinkedList<String>> res_map = parseDomainIp(raw_data);
        // get all the keys from the hashmap
        List<String> list_of_keys = new ArrayList<>(res_map.keySet());
        // iterate over each key and check if the values it holds exceed the
        // THRESHOLD set by default
        for (String key: list_of_keys) {
            LinkedList<String> values = res_map.get(key);

            if (values.size() > THRESHOLD) {
                Evidence evidence = new Evidence();
                evidence.score = 1;
                evidence.subject = subject;
                evidence.label = profile.label;
                evidence.time = raw_data[raw_data.length - 1].time;
                evidence.report = "Found an item: "
                        + key
                        + " that corresponds to too many different ips/domains"
                        + " (" + values.size() + ")";
                datastore.addEvidence(evidence);
            }
        }
    }
}
