package mark.server;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;


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
     * @throws mark.server.InvalidProfileException
     * @throws java.net.MalformedURLException
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


        Config config = new Config();
        if (cmd.hasOption("c")) {
            config = Config.fromFile(new File(cmd.getOptionValue("c")));
        }

        final Server server = new Server(config);

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

        // Launch browser
        String url = "http://127.0.0.1:8000";

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
