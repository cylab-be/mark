package be.cylab.mark.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.File;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
     */
    public static void main(final String[] args) {

        // Parse command line arguments
        Options options = new Options();

        // add t option
        options.addOption("c", true, "Configuration file to use");
        options.addOption("h", false, "Show this help");
        options.addOption("b", false, "Run in batch mode");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
            System.err.println(
                    "Failed to parse command line " + Arrays.toString(args));
            System.exit(1);
        }

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
        Injector injector = Guice.createInjector(
                new BillingModule(config_file));
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

        try {
            if (cmd.hasOption("b")) {
                server.batch();
            } else {
                server.start();
            }
        } catch (Exception ex) {
            System.err.println("Failed to start server: " + ex.getMessage());
            System.exit(1);
        }
    }
}
