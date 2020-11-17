/*
 * The MIT License
 *
 * Copyright 2019 Thibault Debatty.
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
package be.cylab.mark.activation;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import junit.framework.TestCase;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterMetrics;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.spi.collision.fifoqueue.FifoQueueCollisionSpi;
/**
 *
 * @author georgi
 */
public class IgniteTest extends TestCase {

    public Ignite getIgnite() {

        if (Ignition.state() == IgniteState.STARTED) {
            return Ignition.ignite();
        }

        IgniteConfiguration ignite_config = new IgniteConfiguration();
        ignite_config.setPeerClassLoadingEnabled(true);
        ignite_config.setClientMode(false);

        ignite_config.setCollisionSpi(new FifoQueueCollisionSpi());

        ignite_config.setMetricsUpdateFrequency(500);

        // Changing total RAM size to be used by Ignite Node.
        DataStorageConfiguration storage_config =
                new DataStorageConfiguration();
        // Setting the size of the default memory region to
        storage_config.getDefaultDataRegionConfiguration().setMaxSize(
            12L * 1024 * 1024 * 1024);
        ignite_config.setDataStorageConfiguration(storage_config);


        // Start Ignite framework..
        return Ignition.start(ignite_config);
    }

    public void testIgniteExecutorService() throws InterruptedException {

        System.out.println("Test Ignite Executor");

        ExecutorService executor = this.getIgnite().executorService();
        for (int i = 0; i < 22; i++) {
            executor.submit(new StupidTask());
        }

        this.watchMetrics();

        /*
        * ClusterMetricsSnapshot [lastUpdateTime=1567105469591, maxActiveJobs=0,
        * curActiveJobs=0, avgActiveJobs=0.0, maxWaitingJobs=0,
        * curWaitingJobs=0, avgWaitingJobs=0.0, maxRejectedJobs=0,
        * curRejectedJobs=0, avgRejectedJobs=0.0, maxCancelledJobs=0,
        * curCancelledJobs=0, avgCancelledJobs=0.0, totalRejectedJobs=0,
        * totalCancelledJobs=0, totalExecutedJobs=0, maxJobWaitTime=0,
        * curJobWaitTime=0, avgJobWaitTime=0.0, maxJobExecTime=0,
        * curJobExecTime=0, avgJobExecTime=0.0, totalExecTasks=1,
        * totalIdleTime=2169, curIdleTime=2169, availProcs=8, load=0.0,
        * avgLoad=0.0, gcLoad=0.0, heapInit=255852544, heapUsed=64329568,
        * heapCommitted=567803904, heapMax=3631742976, heapTotal=3631742976,
        * nonHeapInit=2555904, nonHeapUsed=40053584, nonHeapCommitted=42008576,
        * nonHeapMax=0, nonHeapTotal=-1, upTime=6688, startTime=1567105462936,
        * nodeStartTime=1567105467899, threadCnt=64, peakThreadCnt=64,
        * startedThreadCnt=72, daemonThreadCnt=11, lastDataVer=1567105467556,
        * sentMsgsCnt=0, sentBytesCnt=0, rcvdMsgsCnt=0, rcvdBytesCnt=0,
        * outMesQueueSize=0, totalNodes=1, totalJobsExecTime=0]
        */
    }

    private void watchMetrics() throws InterruptedException {

        IgniteCluster cluster = this.getIgnite().cluster();
        for (int i = 0; i < 15; i++) {
            Thread.sleep(1000);
            ClusterMetrics metrics = cluster.metrics();
            System.out.println(
                    metrics.getCurrentWaitingJobs() + " / " +
                            metrics.getAverageJobWaitTime() + " / " +
                            metrics.getCurrentActiveJobs() + " / " +
                            metrics.getTotalExecutedJobs());
        }

        System.out.println(cluster.metrics());
    }

    private static class StupidTask<R> implements IgniteCallable<String> {

        @Override
        public String call() throws Exception {

            Thread.sleep(4000);
            return "Hello World";
        }
    }
}
