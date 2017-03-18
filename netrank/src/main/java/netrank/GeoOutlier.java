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
import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;
import mark.core.Subject;

/**
 *
 * @author Georgi Nikolov
 * Agent class for determining servers whose coordinates are too far away from
 * the normal servers the clients has been connected to.
 * They are outliers and may be malicious.
 */
public class GeoOutlier implements DetectionAgentInterface {

    //Helper function to determine the distance between two locations
    //based on their longitude and latitude
    private double distance(final double lat1, final double lng1
            , final double lat2, final double lng2) {

        double earth_radius = 6371; // in kilometer

        double d_lat = Math.toRadians(lat2 - lat1);
        double d_lng = Math.toRadians(lng2 - lng1);

        double sind_lat = Math.sin(d_lat / 2);
        double sind_lng = Math.sin(d_lng / 2);

        double a = Math.pow(sind_lat, 2) + Math.pow(sind_lng, 2)
            * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double dist = earth_radius * c;

        return dist; // output distance in kilometer
    }

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

         //Run through the List of Locations and determine if there are outliers
         //that lie outside of the determined acceptable limits for longitude
         //and latitude.
        ArrayList<Location> outliers = new ArrayList<>();
        Location prev_location_holder = null;
        for (int counter = 0; counter < locations.size(); counter++) {
            Location current_location = locations.get(counter);
            if (prev_location_holder == null) {
                prev_location_holder = current_location;
            } else {
                //Check that we are not comparing a good connection to a
                //previously determined outlier connection.
                //check the distance between the previous and current locations
                //using the latitude and longitude. If the Distance is bigger
                //than 500 is considered an outlier.
                if (!outliers.contains(prev_location_holder)
                        && distance(prev_location_holder.latitude,
                        prev_location_holder.longitude,
                        current_location.latitude,
                        current_location.longitude) > 500) {
                    outliers.add(current_location);
                }
                prev_location_holder = current_location;
            }
        }

        //If there are any outliers create an evidence.
        if (outliers.size() > 0) {
            Evidence evidence = new Evidence();
            //If there are 10 or fewer outliers bigger chance of a malicious
            //connection
            if (outliers.size() <= 10) {
                evidence.score = 1;
            //The bigger number of outliers the smaller chance of a malicious
            //connection
            } else {
                evidence.score = 1 - outliers.size() / 100;
            }
            evidence.subject = subject;
            evidence.label = profile.label;
            evidence.time = raw_data[raw_data.length - 1].time;
            evidence.report = "Found " + outliers.size()
                    + " outlier in the connections with"
                    + " distance between the servers bigger than 500 kilometers"
                    + "\n";

            datastore.addEvidence(evidence);
        }

    }
}
