package be.cylab.mark.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 *
 * @author Thibault Debatty
 */
public final class Main {

    private Main() {
    }

    /**
     *
     * @param args
     * @throws org.apache.commons.cli.ParseException
     * @throws java.io.FileNotFoundException
     * @throws java.net.MalformedURLException
     */
    public static void main(final String[] args)
            throws Throwable {

        // Parse command line arguments
        Options options = new Options();

        // add t option
        options.addOption("c", true, "Configuration file to use");
        options.addOption("h", false, "Show this help");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar server-<version>.jar", options);
            return;
        }

        //Config config = new Config();
        File config_file = null;
        if (cmd.hasOption("c")) {
            config_file = new File(cmd.getOptionValue("c"));
        }

        //Dependency injection
        Injector injector = Guice.createInjector(new BillingModule(config_file));
        final Server server = injector.getInstance(Server.class);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    server.stop();
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }
        });

        server.start();
    }
}
