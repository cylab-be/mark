/*
 * The MIT License
 *
 * Copyright 2017 Thibault Debatty.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package mark.agent.detection;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mark.activation.DetectionAgentInterface;
import mark.core.ServerInterface;
import mark.core.Subject;
import mark.core.SubjectAdapter;

/**
 * Detection agent that runs an external command.
 * In the YAML configuration file, use the following fields:
 * - command : command to run
 * - wd : work directory
 * - any other parameter you provide will be transmitted to the command
 * @author Thibault Debatty
 */
public class Run implements DetectionAgentInterface {

    static final String KEY_COMMAND = "command";
    static final String KEY_WD = "wd";

    private Map<String, String> parameters;
    private String label;
    private String input_label;
    private Subject subject;
    private String datastore_url;

    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setInputLabel(String input_label) {
        this.input_label = input_label;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public final void run() {
        LinkedList<String> command = new LinkedList<String>();
        command.add(parameters.get(KEY_COMMAND));

        command.add("-d");
        command.add(datastore_url);

        for (String key : parameters.keySet()) {
            if (key.equals(KEY_COMMAND) || key.equals(KEY_WD)) {
                continue;
            }

            command.add("-" + key);
            command.add(parameters.get(key));
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(parameters.get(KEY_WD)));
        try {
            Process p = pb.start();
            p.waitFor();
            String out = readInputStream(p.getInputStream());
            System.out.println(out);

        } catch (IOException ex) {
            Logger.getLogger(Run.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Run.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private static String readInputStream(final InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }

        return sb.toString();
    }

    public void setDatastoreUrl(String datastore_url) {
        this.datastore_url = datastore_url;
    }

    public void setSubjectAdapter(SubjectAdapter subject_adapter) {
        // not needed...
    }
}
