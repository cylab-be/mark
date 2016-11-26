package mark.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
    public String class_name;

    /**
     * Additional parameters to pass to the agent (e.g time range).
     */
    public Map<String, String> parameters;

    public String path;

    private static final Yaml PARSER = new Yaml(new Constructor(DataAgentProfile.class));

    public final static DataAgentProfile fromFile(final File file) throws FileNotFoundException {
        DataAgentProfile profile = DataAgentProfile.fromInputStream(new FileInputStream(file));
        profile.path = file.getAbsolutePath();
        return profile;
    }

    public static final DataAgentProfile fromInputStream(final InputStream input) {
        return PARSER.loadAs(input, DataAgentProfile.class);
    }
}
