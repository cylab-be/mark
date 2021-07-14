/*
 * The MIT License
 *
 * Copyright 2021 tibo.
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
package be.cylab.mark.server;

import be.cylab.mark.core.DataAgentProfile;
import be.cylab.mark.data.DataAgentContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 * Manages the data sources.
 * @author tibo
 */
@Singleton
public final class DataSourcesController {

    private static final org.slf4j.Logger LOGGER
            = LoggerFactory.getLogger(DataSourcesController.class);

    private final LinkedList<DataAgentContainer> data_agents;
    private final List<DataAgentProfile> profiles;
    private final Config config;

    /**
     *
     * @param config
     */
    @Inject
    public DataSourcesController(final Config config) {
        this.data_agents = new LinkedList<>();
        this.profiles = new LinkedList<>();
        this.config = config;
    }

    /**
     *
     */
    public void start() {

        // Start data agents...
        for (DataAgentContainer agent : data_agents) {
            agent.start();
        }
    }

    /**
     * Wait for data agents to finish.
     *
     * @throws InterruptedException if thread is interrupted during operation
     */
    public void awaitTermination() throws InterruptedException {
        LOGGER.info("Wait for data agents to finish...");
        for (DataAgentContainer agent : data_agents) {
            agent.join();
        }
    }

    /**
     *
     * @throws InterruptedException if thread was killed during stop
     */
    public void stop() throws InterruptedException {
        for (DataAgentContainer agent : data_agents) {
            agent.interrupt();
        }

        this.awaitTermination();
    }

    /**
     *
     */
    public void loadAgentsFromModulesDirectory() {
        LOGGER.info("Parsing modules directory ");
        File modules_dir;
        try {
            modules_dir = config.getModulesDirectory();
        } catch (FileNotFoundException ex) {
            LOGGER.warn(ex.getMessage());
            LOGGER.warn("Skipping modules parsing ...");
            return;
        }

        LOGGER.info(modules_dir.getAbsolutePath());

        // Parse *.data.yml files
        File[] data_agent_files = modules_dir.listFiles(
                (final File dir, final String name) ->
                        name.endsWith(".data.yml"));

        //Instanciate DataAgentProfiles for each previously parsed files.
        for (File file : data_agent_files) {
            try {
                DataAgentProfile profile = DataAgentProfile.fromFile(file);
                this.add(profile);
            } catch (FileNotFoundException ex) {
                LOGGER.warn("file already deleted ? " + file.getAbsolutePath());
            }
        }
        LOGGER.info("Found " + data_agents.size() + " data agents ...");
    }

    /**
     *
     * @param profile
     */
    public void add(final DataAgentProfile profile) {
        profiles.add(profile);
        data_agents.add(new DataAgentContainer(profile, config));
    }

    /**
     *
     * @return
     */
    public List<DataAgentProfile> getProfiles() {
        return profiles;
    }
}
