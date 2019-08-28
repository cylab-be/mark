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
package be.cylab.mark.activation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import be.cylab.mark.server.Config;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.collision.fifoqueue.FifoQueueCollisionSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

/**
 * Allows to run our detection jobs using an Apache Ignite cluster.
 * @author tibo
 */
@Singleton
public class IgniteExecutor implements ExecutorInterface {

    private final Ignite ignite;
    private final ExecutorService executor;
    private final Config config;


    @Inject
    public IgniteExecutor(final Config config) {

        this.config = config;

        IgniteConfiguration ignite_config = new IgniteConfiguration();
        ignite_config.setPeerClassLoadingEnabled(true);
        ignite_config.setClientMode(!config.ignite_start_server);

        ignite_config.setCollisionSpi(new FifoQueueCollisionSpi());

        // Changing total RAM size to be used by Ignite Node.
        DataStorageConfiguration storage_config =
                new DataStorageConfiguration();
        // Setting the size of the default memory region to
        storage_config.getDefaultDataRegionConfiguration().setMaxSize(
            12L * 1024 * 1024 * 1024);
        ignite_config.setDataStorageConfiguration(storage_config);

        if (!config.ignite_autodiscovery) {
            // Disable autodiscovery
            TcpDiscoverySpi spi = new TcpDiscoverySpi();
            TcpDiscoveryVmIpFinder ip_finder = new TcpDiscoveryVmIpFinder();
            ip_finder.setAddresses(Arrays.asList("127.0.0.1"));
            spi.setIpFinder(ip_finder);
            ignite_config.setDiscoverySpi(spi);
        }

        // Start Ignite framework..
        if (Ignition.state() == IgniteState.STARTED) {
            ignite = Ignition.ignite();
        } else {
            ignite = Ignition.start(ignite_config);
        }

        this.executor = ignite.executorService();
    }


    @Override
    public void submit(Runnable job) {
        this.executor.submit(job);
    }

    @Override
    public boolean shutdown() throws InterruptedException {
        Thread.sleep(2 * 1000 * config.update_interval);
        executor.shutdown();
        return executor.awaitTermination(1, TimeUnit.DAYS);
    }

    @Override
    public int taskCount() {
        return ignite.cluster().metrics().getTotalExecutedJobs();
    }

}
