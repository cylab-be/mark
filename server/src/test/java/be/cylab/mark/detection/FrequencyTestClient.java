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
 *
 * @author georgi
 * @param <T>
 */
public class FrequencyTestClient<T extends Subject> extends DummyClient<T>{

    private final LinkedList<Evidence<T>> evidences = new LinkedList<>();
    private final int n_apt;
    private final int n_noise;
    private final int apt_interval;

    public FrequencyTestClient(final int number_apts,
                            final int number_noise,
                            final int apt_interval) {
        this.n_apt = number_apts;
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
        //get current time to start the simulated data
        Date date = new Date();
        //use getTime() to get the timestamp in miliseconds, divide by 1000
        //to transform it to seconds
        long start_time = date.getTime() / 1000;
        Random rand = new Random();

        //if no apts were generated return empty data
        if (n_apt == 0) {
            return new RawData[0];
        }

        RawData[] data = new RawData[n_apt + n_noise];

        //generate the apts with the set interval
        for (int i = 0; i < n_apt; i++) {
            data[i] = new RawData();
            data[i].setSubject(subject);
            data[i].setLabel(type);
            data[i].setTime(start_time + apt_interval * i);
            data[i].setData(data[i].getTime() + "    "
                    + "126 "
                    + "198.36.158.8 "
                    + "TCP_MISS/"
                    + "400"
                    + " 918 GET "
                    + "http://cnc.masfad.com - DIRECT/"
                    + "1.1.1.1 text/html");
        }

        // Add a few random requests
        for (int i = n_apt; i < n_apt + n_noise; i++) {
            data[i] = new RawData();
            data[i].setSubject(subject);
            data[i].setLabel(type);
            data[i].setTime(start_time + rand.nextInt(n_apt * apt_interval));
            data[i].setData(data[i].getTime() + "    "
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
