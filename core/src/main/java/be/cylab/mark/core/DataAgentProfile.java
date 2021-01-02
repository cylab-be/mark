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
public class DataAgentProfile extends AbstractAgentProfile {

    /**
     * The path to the profile file, can be used to compute relative paths.
     */
    private File path;

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
            return (DataAgentInterface)
                    Class.forName(getClassName()).newInstance();
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException ex) {
            throw new InvalidProfileException(
                    "Cannot instantiate data agent " + getClassName() + " : "
                    + ex.getMessage(),
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
