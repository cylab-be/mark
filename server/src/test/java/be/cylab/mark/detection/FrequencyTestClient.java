/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cylab.mark.detection;

import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import be.cylab.mark.core.Subject;

/**
 * Generates 1 day (86400 seconds) of fake data, containing an fixed frequency
 * with provided interval + a number of noise data.
 *
 * @author georgi
 * @param <T>
 */
public class FrequencyTestClient<T extends Subject> extends DummyClient<T>{

    private final LinkedList<Evidence<T>> evidences = new LinkedList<>();
    private final int n_noise;
    private final int apt_interval;

    private static final int TIME_WINDOW = 86400;

    public FrequencyTestClient(final int number_noise,
                            final int apt_interval) {

        this.n_noise = number_noise;
        this.apt_interval = apt_interval;
    }

    /**
     *
     * @param type
     * @param subject
     * @param l
     * @param l1
     * @return
     * @throws Throwable
     */
    @Override
    public RawData[] findRawData(String type, T subject,
            long l, long l1) throws Throwable {

        Date date = new Date();
        long start_time = date.getTime() / 1000 - TIME_WINDOW; // in seconds

        int n_apt = TIME_WINDOW / apt_interval;

        RawData[] data = new RawData[n_apt + n_noise];

        //generate the apts with the set interval
        for (int i = 0; i < n_apt; i++) {
            long time = start_time + apt_interval * i;
            data[i] = new RawData();
            data[i].setSubject(subject);
            data[i].setLabel(type);
            data[i].setTime(time);
            data[i].setData(time + "    "
                    + "126 "
                    + "198.36.158.8 "
                    + "TCP_MISS/"
                    + "400"
                    + " 918 GET "
                    + "http://cnc.masfad.com - DIRECT/"
                    + "1.1.1.1 text/html");
        }

        // Add a random requests
        Random rand = new Random();
        for (int i = n_apt; i < n_apt + n_noise; i++) {
            long time = start_time + rand.nextInt(86400);
            data[i] = new RawData();
            data[i].setSubject(subject);
            data[i].setLabel(type);
            data[i].setTime(time);
            data[i].setData(time + "    "
                    + "126 "
                    + "198.36.158.8 "
                    + "TCP_MISS/"
                    + "200"
                    + " 918 GET "
                    + "http://lyfqnr.owvcq.wf/jbul.html - DIRECT/"
                    + "175.193.216.231 text/html");
        }

        return data;
    }

    @Override
    public void addEvidence(Evidence evidence) throws Throwable {
        System.out.println(evidence.getReport());
        this.evidences.add(evidence);
    }

    @Override
    public LinkedList<Evidence<T>> getEvidences() {
        return this.evidences;
    }
}
