package mark.agent.detection.http;

import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mark.activation.AbstractDetectionAgent;
import mark.core.RawData;
import mark.client.Client;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 *
 * @author Thibault Debatty
 */
public class Frequency extends AbstractDetectionAgent {

    /**
     * Sampling interval (in second).
     */
    public static final int SAMPLING_INTERVAL = 60;

    public final void run() {
        Client datastore;
        RawData[] raw_data;
        try {
            datastore = getDatastore();
            raw_data = datastore.findRawData(
                getType(), getClient(), getServer());
        } catch (Throwable ex) {
            System.err.println("Could not connect!");
            System.err.println(ex.getMessage());
            return;
        }

        FastFourierTransformer fft_transformer = new FastFourierTransformer(
                DftNormalization.STANDARD);


        int[] times = new int[raw_data.length];
        for (int i = 0; i < raw_data.length; i++) {
            times[i] = raw_data[i].time;
        }

        int min = min(times);
        int max = max(times);
        int size = pow2gt((max - min) / SAMPLING_INTERVAL);
        System.out.println("Vector size: " + size);

        double[] counts = new double[size];
        for (int time : times) {
            int position = (time - min) / SAMPLING_INTERVAL;
            counts[position]++;
        }

        for (double count : counts) {
            System.out.println(count);
        }

        Complex[] transform =
                fft_transformer.transform(counts, TransformType.FORWARD);

        for (Complex complex : transform) {
            System.out.println(complex.getReal());
        }
    }

    static int pow2gt(final int value) {
        int result = 1;

        while (result < value) {
            result <<= 1;
        }
        return result;
    }

    static int min(int[] values) {
        int result = Integer.MAX_VALUE;
        for (int value : values) {
            if (value < result) {
                result = value;
            }
        }
        return result;
    }

    static int max(int[] values) {
        int result = Integer.MIN_VALUE;
        for (int value : values) {
            if (value > result) {
                result = value;
            }
        }
        return result;
    }

}
