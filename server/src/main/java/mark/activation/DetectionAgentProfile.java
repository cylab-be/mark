package mark.activation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import mark.server.DataAgentProfile;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author Thibault Debatty
 */
public class DetectionAgentProfile {

    /**
     * Eg: RAW_DATA.
     */
    public ActivationController.Collection collection;

    /**
     * Eg: http.
     */
    public String type;

    public int condition_count;

    public int condition_time = Integer.MAX_VALUE;

    /**
     *
     */
    public String class_name;

    private static final Yaml PARSER = new Yaml(new Constructor(DetectionAgentProfile.class));

    public final static DetectionAgentProfile fromFile(final File file) throws FileNotFoundException {
        return DetectionAgentProfile.fromInputStream(new FileInputStream(file));
    }

    public static final DetectionAgentProfile fromInputStream(final InputStream input) {
        return PARSER.loadAs(input, DetectionAgentProfile.class);
    }

}
