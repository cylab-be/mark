package mark.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author Thibault Debatty
 */
public class DetectionAgentProfile {

    /**
     * The label attached to the evidence produced by this detection agent. Ex:
     * evidence.http.frequency.1week or aggregation
     */
    public String label;

    /**
     * The label that will trigger this detection agent. Ex: http frequency
     * could be triggered by data.http and aggregation could be triggered by
     * evidence.
     */
    public String trigger_label;

    /**
     * The class of the detection agent to trigger.
     */
    public String class_name;

    /**
     * The parameters that will be provided to the detector.
     */
    public HashMap<String, String> parameters = new HashMap<>();

    private static final Yaml PARSER
            = new Yaml(new Constructor(DetectionAgentProfile.class));

    /**
     * Read a detection agent profile from a YAML file.
     *
     * @param file
     * @return
     * @throws FileNotFoundException if the file does not exist
     */
    public static final DetectionAgentProfile fromFile(final File file)
            throws FileNotFoundException {
        return DetectionAgentProfile.fromInputStream(new FileInputStream(file));
    }

    /**
     * Read a detection agent profile from an input stream (a resource file).
     *
     * @param input
     * @return
     */
    public static final DetectionAgentProfile fromInputStream(
            final InputStream input) {
        return PARSER.loadAs(input, DetectionAgentProfile.class);
    }

    /**
     * Instantiate a detection agent for the provided subject.
     *
     * @return
     * @throws mark.core.InvalidProfileException if the profile is incorrect
     * (generally due to an invalid class name).
     */
    public final DetectionAgentInterface createInstance()
            throws InvalidProfileException {

        try {
            return (DetectionAgentInterface) Class.forName(class_name).newInstance();
        } catch (ClassNotFoundException ex) {
            throw new InvalidProfileException(
                    "Cannot instantiate data agent " + class_name,
                    ex);
        } catch (InstantiationException ex) {
            throw new InvalidProfileException(
                    "Cannot instantiate data agent " + class_name,
                    ex);
        } catch (IllegalAccessException ex) {
            throw new InvalidProfileException(
                    "Cannot instantiate data agent " + class_name,
                    ex);
        }
    }

    /**
     * Test if the provided label should trigger this detection agent. return
     * trigger_label.startsWith(label)
     *
     * @param label
     * @return
     */
    public final boolean match(final String label) {
        return label.startsWith(this.trigger_label);
    }
}
