package be.cylab.mark.example;

import be.cylab.mark.core.ClientWrapperInterface;
import java.util.Random;
import be.cylab.mark.core.DetectionAgentInterface;
import be.cylab.mark.core.DetectionAgentProfile;
import be.cylab.mark.core.Event;
import be.cylab.mark.core.Evidence;
import be.cylab.mark.core.RawData;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Dummy detection agent that reads some data from datastore and writes
 * one evidence.
 * @author Thibault Debatty
 */
public class ExampleDetector implements DetectionAgentInterface {


    @Override
    public final void analyze(
            final Event ev,
            final DetectionAgentProfile profile,
            final ClientWrapperInterface datastore) throws Throwable {

        long now = System.currentTimeMillis();
        long since = now - 1000 * 300;

        RawData[] data = datastore.findRawData(
                ev.getLabel(),
                ev.getSubject(),
                since, now);

        // Process data ...
        Random rand = new Random();

        // and create an illustration image
        File file = datastore.createSharedFile("image.png");
        writeImage(1200, 600, file);

        // will be something like /data/123456_image.png
        String file_url = datastore.getURLFromFile(file);

        String report = "<p>Found " + data.length + " data records with label "
            + ev.getLabel() + "</p><p><img src='" + file_url + "'></p>";

        // Add evidences to datastore
        Evidence evidence = new Evidence();
        evidence.setLabel(profile.getLabel());
        evidence.setSubject(ev.getSubject());
        evidence.setReport(report);
        evidence.setScore(rand.nextDouble());
        evidence.setTime(ev.getTimestamp());
        datastore.addEvidence(evidence);
    }

    /**
     * Create a random image (PNG) of given size, and write it down to given
     * file.
     * @param width
     * @param height
     * @param file
     */
    public final void writeImage(
            final int width, final int height, final File file) {

        //create buffered image object img
        BufferedImage img = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);

        //create random image pixel by pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int a = (int) (Math.random() * 256); //alpha
                int r = (int) (Math.random() * 256); //red
                int g = (int) (Math.random() * 256); //green
                int b = (int) (Math.random() * 256); //blue

                int p = (a << 24) | (r << 16) | (g << 8) | b; //pixel

                img.setRGB(x, y, p);
            }
        }

        //write image
        try {
            ImageIO.write(img, "png", file);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }
}
