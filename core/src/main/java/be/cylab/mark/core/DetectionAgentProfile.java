package be.cylab.mark.core;

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
    private String label;

    /**
     * The label that will trigger this detection agent. Ex: http frequency
     * could be triggered by data.http and aggregation could be triggered by
     * evidence.
     */
    private String trigger_label;

    /**
     * The class of the detection agent to trigger.
     */
    private String class_name;

    /**
     * The parameters that will be provided to the detector.
     */
    private HashMap<String, String> parameters = new HashMap<>();


    /**
     *
     * @return
     */
    public final String getLabel() {
        return label;
    }

    /**
     *
     * @param label
     */
    public final void setLabel(final String label) {
        this.label = label;
    }

    /**
     *
     * @return
     */
    public final String getTriggerLabel() {
        return trigger_label;
    }

    /**
     *
     * @param trigger_label
     */
    public final void setTriggerLabel(final String trigger_label) {
        this.trigger_label = trigger_label;
    }

    /**
     *
     * @return
     */
    public final String getClassName() {
        return class_name;
    }

    /**
     *
     * @param class_name
     */
    public final void setClassName(final String class_name) {
        this.class_name = class_name;
    }

    /**
     *
     * @param parameters
     */
    public final void setParameters(final HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     *
     * @return
     */
    public final HashMap<String, String> getParameters() {
        return parameters;
    }

    /**
     *
     * @param name
     * @return
     */
    public final String getParameter(final String name) {
        return parameters.get(name);
    }

    /**
     * Get the value of this parameter, of return default_value if not set.
     * @param name
     * @param default_value
     * @return
     */
    public final String getParameterOrDefault(
            final String name, final String default_value) {
        return parameters.getOrDefault(name, default_value);
    }

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
     * @throws be.cylab.mark.core.InvalidProfileException if the profile is
     * incorrect (generally due to an invalid class name).
     */
    public final DetectionAgentInterface createInstance()
            throws InvalidProfileException {

        try {
            return (DetectionAgentInterface)
                    Class.forName(class_name).newInstance();

        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException ex) {

            throw new InvalidProfileException(
                    "Cannot instantiate detection agent " + class_name + " : "
                    + ex.getMessage(),
                    ex);
        }
    }
}
