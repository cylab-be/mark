package mark.core;

import mark.core.DataAgentInterface;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import mark.core.InvalidProfileException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author Thibault Debatty
 */
public class DataAgentProfile {

    /**
     * Name of the class to instantiate (must implement DataAgentInterface).
     */
    public String class_name;

    /**
     * The label for the data that will be added to the datastore.
     */
    public String label;

    /**
     * Additional parameters to pass to the agent (e.g time range).
     */
    public Map<String, String> parameters;

    public String path;

    private static final Yaml PARSER =
            new Yaml(new Constructor(DataAgentProfile.class));

    /**
     * Parse YAML configuration file to create data agent profile.
     * @param file
     * @return
     * @throws FileNotFoundException if the config file does not exist
     */
    public static final DataAgentProfile fromFile(final File file)
            throws FileNotFoundException {
        DataAgentProfile profile =
                DataAgentProfile.fromInputStream(new FileInputStream(file));
        profile.path = file.getAbsolutePath();
        return profile;
    }

    /**
     * Create profile from input stream (for example a resource file in jar).
     * @param input
     * @return
     */
    public static final DataAgentProfile fromInputStream(
            final InputStream input) {
        return PARSER.loadAs(input, DataAgentProfile.class);
    }

    /**
     * Create an instance of this data agent.
     * @return
     * @throws InvalidProfileException
     */
    public final DataAgentInterface createInstance()
            throws InvalidProfileException {

        try {
            return (DataAgentInterface) Class.forName(class_name).newInstance();
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
}
