package mark.activation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import mark.core.Subject;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author Thibault Debatty
 */
public class DetectionAgentProfile<T extends Subject> {

    /**
     * The label attached to the evidence produced by this detection agent.
     * Ex: evidence.http.frequency.1week or aggregation
     */
    public String label;

    /**
     * The label that will trigger this detection agent.
     * Ex: http frequency could be triggered by data.http and aggregation could
     * be triggered by evidence.
     */
    public String trigger_label;

    /**
     * The class of the detection agent to trigger.
     */
    public String class_name;

    private static final Yaml PARSER = new Yaml(new Constructor(DetectionAgentProfile.class));

    public final static DetectionAgentProfile fromFile(final File file) throws FileNotFoundException {
        return DetectionAgentProfile.fromInputStream(new FileInputStream(file));
    }

    public static final DetectionAgentProfile fromInputStream(final InputStream input) {
        return PARSER.loadAs(input, DetectionAgentProfile.class);
    }

    /**
     *
     * @param subject
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    DetectionAgentInterface getTaskFor(final T subject)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        // Create analysis task
        DetectionAgentInterface<T> new_task =
                (DetectionAgentInterface<T>)
                Class.forName(class_name)
                .newInstance();

        new_task.setSubject(subject);
        new_task.setLabel(label);
        new_task.setInputLabel(trigger_label);

        return new_task;
    }

    boolean match(String label) {
        return this.trigger_label.startsWith(label);
    }

}
