package be.cylab.mark.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
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
    private String class_name;

    /**
     * The label for the data that will be added to the datastore.
     */
    private String label;

    /**
     * Additional parameters to pass to the agent (e.g time range).
     */
    private Map<String, String> parameters = new HashMap<>();

    /**
     * The path to the profile file, can be used to compute relative paths.
     */
    private File path;


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
    public final Map<String, String> getParameters() {
        return parameters;
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
    public final File getPath() {
        return path;
    }

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
        profile.path = file;
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
     * @throws InvalidProfileException if the profile is not parsed correctly
     */
    public final DataAgentInterface createInstance()
            throws InvalidProfileException {

        try {
            return (DataAgentInterface) Class.forName(class_name).newInstance();
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException ex) {
            throw new InvalidProfileException(
                    "Cannot instantiate data agent " + class_name,
                    ex);
        }
    }

    /**
     * Compute the File corresponding to this filename, which can be an
     * absolute path, or a relative path to this data agent profile file.
     * @param filename
     * @return
     * @throws FileNotFoundException if file is not found
     */
    public final File getPath(final String filename)
            throws FileNotFoundException {
        File file = new File(filename);

        if (!file.isAbsolute()) {
            // modules is a relative path...
            if (path == null) {
                throw new FileNotFoundException(
                    "provided modules directory is not valid (not a directory "
                            + "or no a valid path)");
            }
            file = new File(path.toURI().resolve(filename));
        }

        return file;
    }
}
