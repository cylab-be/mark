package mark.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import mark.activation.InvalidProfileException;
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
     * @throws org.apache.commons.cli.ParseException
     * @throws java.io.FileNotFoundException
     * @throws mark.activation.InvalidProfileException
     */
    public static void main(final String[] args)
            throws ParseException, FileNotFoundException,
            InvalidProfileException, MalformedURLException, Exception {
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

        Server server = new Server();

        if (cmd.hasOption("c")) {
            server.setConfiguration(Config.fromFile(new File(cmd.getOptionValue("c"))));
        }

        server.start();
    }
}
