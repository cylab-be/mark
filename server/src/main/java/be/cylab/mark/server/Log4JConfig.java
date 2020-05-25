/*
 * The MIT License
 *
 * Copyright 2020 tibo.
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

import java.net.URI;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;

/**
 *
 * @author tibo
 */
@Plugin(name = "CustomConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(50)
public class Log4JConfig extends ConfigurationFactory {

    @Override
    protected String[] getSupportedTypes() {
        System.out.println("getSupportedTypes");
        return new String[] {"*"};
    }

    @Override
    public Configuration getConfiguration(
            final LoggerContext ctx, final ConfigurationSource source) {
        return getConfiguration(ctx, source.toString(), null);

    }

    @Override
    public Configuration getConfiguration(
            final LoggerContext ctx, final String name, final URI config_location) {
        ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();
        return createConfiguration(name, builder);
    }

    private static final String LOG_PATTERN = "%d [%p] [%t] %c %m%n";

    private Configuration createConfiguration(
            final String name,
            final ConfigurationBuilder<BuiltConfiguration> builder) {

        System.out.println("Configure log4j...");
        builder.setConfigurationName(name);

        // Log errors from LOG4J itself
        builder.setStatusLevel(Level.ERROR);

        // Log to stdout
        AppenderComponentBuilder appenderBuilder = builder
                .newAppender("STDOUT", "CONSOLE")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);

        appenderBuilder
                .add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", LOG_PATTERN));
        builder.add(appenderBuilder);


        // Log MARK server : INFO
        builder.add(builder.newLogger(
                Server.class.getCanonicalName(), Level.DEBUG)
                .add(builder.newAppenderRef("STDOUT"))
                .addAttribute("additivity", false));

        // Log everything else : WARN
        builder.add(builder
                .newRootLogger(Level.WARN)
                .add(builder.newAppenderRef("STDOUT")));
        return builder.build();
    }

}
