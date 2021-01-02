package be.cylab.mark.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author Thibault Debatty
 */
public class DetectionAgentProfile extends AbstractAgentProfile {

    /**
     * The label that will trigger this detection agent. Ex: http frequency
     * could be triggered by data.http and aggregation could be triggered by
     * evidence.
     */
    private String trigger_label = "";

    /**
     * The label showing the triggering interval of this detection agent. Ex:
     * an agent can be triggered constaly when = 0 or once a day = 86400.
     */
    private int trigger_interval = 0;

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
     * Minimum duration between triggering this detector for the same subject,
     * expressed in seconds.
     * @return
     */
    public final int getTriggerInterval() {
        return trigger_interval;
    }

    /**
     * Set the minimum duration between triggering this detector for the same
     * subject, expressed in seconds.
     * @param trigger_interval
     */
    public final void setTriggerInterval(final int trigger_interval) {
        this.trigger_interval = trigger_interval;
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
                    Class.forName(getClassName()).newInstance();

        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException ex) {

            throw new InvalidProfileException(
                    "Cannot instantiate detection agent " + getClassName()
                    + " : " + ex.getMessage(),
                    ex);
        }
    }
}
