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

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import mark.core.Subject;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

/**
 *
 * @author Georgi Nikolov
 * Agent class for determining servers whose coordinates are too far away from
 * the normal servers the clients has been connected to.
 * They are outliers and may be malicious.
 */
public class GeoOutlier implements DetectionAgentInterface {

    // Analyze function inherited from the DetectionAgentInterface
    // accepts the subject to analyze
    // trigger of the agent
    // the profile used to load the agent
    // the database to which to connect to gather RawData
    @Override
    public final void analyze(
            final Subject subject,
            final String actual_trigger_label,
            final DetectionAgentProfile profile,
            final ServerInterface datastore) throws Throwable {

        RawData[] raw_data = datastore.findRawData(
                actual_trigger_label, subject);

        //Code for Accessing the local GeoLocation File consisting of the
        //information per IP Address.
        ClassLoader class_loader = getClass().getClassLoader();
        File geo_file = new File(class_loader
                .getResource("GeoLiteCity.dat").getFile());
        LookupService cl = new LookupService(geo_file,
                    LookupService.GEOIP_MEMORY_CACHE
                            | LookupService.GEOIP_CHECK_CACHE);

        ArrayList<Location> locations = new ArrayList<>();
        Pattern pattern = Pattern.compile("DIRECT/(\\b(?:(?:25[0-5]|2[0-4]"
                + "[0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]"
                + "|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b)");
        for (RawData raw_data1 : raw_data) {
            String server_ip = null;
            Matcher matcher = pattern.matcher(raw_data1.data);
            if (matcher.find()) {
                server_ip = matcher.group(1);
            }
            if (server_ip != null) {
                Location location = cl.getLocation(server_ip);
                locations.add(location);
            }
        }

        List<LocationWrapper> clusterInput = new ArrayList<>(locations.size());
        for (Location location : locations) {
            clusterInput.add(new LocationWrapper(location));
        }

        //Initialize a new cluster algorithm.
        //We use DBSCANCluster to determine locations close to each other
        //and outliers that don't belong to any cluster.
        DBSCANClusterer dbscan = new DBSCANClusterer(20, 1);
        List<Cluster<LocationWrapper>> clusters = dbscan.cluster(clusterInput);

        //If there are any outliers create an evidence.
        if (clusters.size() > 1) {
            Evidence evidence = new Evidence();

            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found"
                    + " outliers in the connections with"
                    + " distance between the servers bigger than the"
                    + " expected distance between serivers"
                    + "\n";

            datastore.addEvidence(evidence);
        }

    }
    
    private static class LocationWrapper implements Clusterable {
        private final double[] points;
        private final Location location;

        public LocationWrapper(Location location) {
            this.location = location;
            this.points = new double[] {location.latitude, location.longitude};
        }

        public Location getLocation() {
            return this.location;
        }

        @Override
        public double[] getPoint() {
            return this.points;
        }

    }
}
