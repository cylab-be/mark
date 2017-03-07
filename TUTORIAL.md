# Tutorial

This tutorial will show you how to implement algorithms for your own application.

For this example, we will suppose you care for a large group of say... cats. Now you would like to know which one of your cats need attention, using some very serious algorithms. This tutorial will show you how to:
* Install and run the MARK server
* Create a maven project
* Define your subject (for our example, a Cat)
* Create your connector to feed information about your cats in the server
* Use profiles
* Implement and combine your detection algorithms

## Installation and run

To run a MARK server, you will need:
* java 7 or newer
* a running mongo server

Download the server-VERSION-standalone.zip from the release page
```
wget https://github.com/blah/server-0.0.7-standalone.zip
```

Extract the archive
```
unzip server-*-standalone.zip
```

Check that mongodb is running
```
sudo service mongodb start
```

Move to directory and run the server:
```
cd server-*-standalone
./run.sh
```

After a few seconds, the server is started. Your browser will open the web interface, which by default is available at http://127.0.0.1:8000

For now the server has nothing to show...

You can stop the server by hitting ```ctrl + c```. It can take some time for the server to stop...

If you look closer at the folder structure of the server, you will see it looks like this:

```
/run.sh       # main script to start the server
/config.yml   # main configuration file
/bin          # contains the server code (server-<version>.jar)
/libs         # contains all library dependencies (jars)
/modules      # this is where you will put your algorithms jars and configuration files
/logs         # well... logs
```

## Create a maven project

It's now time to create your own modules for feeding data into the system and perform detection. The easiest way to do so is using maven:

```
mvn -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.1 -DgroupId=cat -DartifactId=catrank -Dversion=0.1-SNAPSHOT -Dpackage=cat.rank  -Darchetype.interactive=false --batch-mode archetype:generate
```

If you are using an IDE (Netbeans or Eclipse), normally you simply need to create a new maven project:
- [Netbeans : Quick Start Java Maven Project](http://wiki.netbeans.org/QuickStartJavaMavenProject)
- [Eclipse : Create a new maven project](http://www.tech-recipes.com/rx/39279/create-a-new-maven-project-in-eclipse/)


In your pom.xml, add the required dependencies:
```
<dependencies>
  <dependency>
      <groupId>info.debatty.mark</groupId>
      <artifactId>core</artifactId>
      <version>0.0.7</version>
      <scope>provided</scope>
  </dependency>

  <dependency>
      <groupId>info.debatty.mark</groupId>
      <artifactId>client</artifactId>
      <version>0.0.7</version>
      <scope>provided</scope>
  </dependency>

  <dependency>
      <groupId>info.debatty.mark</groupId>
      <artifactId>server</artifactId>
      <version>0.0.7</version>
      <scope>test</scope>
  </dependency>
</dependencies>
```

As you can see, the scope of these dependencies is "provided" as they are already included in the installed server.

## Subject and adapter

The server should know what it will be ranking, and how to treat it. This is the role of the **Subject** and **SubjectAdapter** interfaces.

In our case we will be ranking cats:

```java
package cat.rank;

import mark.core.Subject;

public class Cat implements Subject {
    public String name = "";

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!other.getClass().equals(this.getClass())) {
            return false;
        }

        Cat other_cat = (Cat) other;
        return this.name.equals(other_cat.name);
    }
}
```

The **hashCode** and **equals** methods are required as the indicate to the server if two cats are considered the same (or not). In our example, two cats are the same if they have the same name (and hence we make the assumption that two different cats will never have the same name...).

The adapter is used to show the server how to (de)serialize the Subject to JSON and to/from Mongo:
```java
package cat.rank;

import com.fasterxml.jackson.databind.JsonNode;
import mark.core.SubjectAdapter;
import org.bson.Document;

public class CatAdapter implements SubjectAdapter<Cat> {

    public static final String FIELD_NAME = "name";

    @Override
    public void writeToMongo(Cat subject, Document doc) {
        doc.append(FIELD_NAME, subject.name);
    }

    @Override
    public Cat readFromMongo(Document doc) {
        Cat cat = new Cat();
        cat.name = doc.getString(FIELD_NAME);
        return cat;
    }

    @Override
    public Cat deserialize(JsonNode node) {
        Cat cat = new Cat();
        cat.name = node.get(FIELD_NAME).asText();
        return cat;
    }
}
```

You can now build your project:

```
mvn clean install
```

There are two steps to install and configure your plugin:
1. Copy your jar (should be catrank-0.1-SNAPSHOT.jar) to the "modules" directory of the server
2. Modify the configuration file of the server (config.yml) to indicate which class should be used as adapter. It should look like this:

```yml
---
adapter_class: cat.rank.CatAdapter

mongo_host:   127.0.0.1
mongo_port:   27017
mongo_db:     MASFAD

server_host:  127.0.0.1
server_port:  8080

webserver_root: ./ui
```

You can start your server if you wish, but there is still nothing to display...

## Data source

A data source is a component that will feed data to the system. You can run a separate data source that you start separately, or you can create a "plugin" datasource, that will be automatically started and stopped with the rest of the server. For this one, your datasource should implement the **DataSourceInterface**.

For our example, the data source will be a simple sink that waits for incoming connections. This will allow us to test the system using netcat for example. The sink parses the lines it receives, which should look like this:

```
<cat name> : <message>
```

Then the data source saves this data to the server.

```java
package cat.rank;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import mark.core.DataAgentInterface;
import mark.core.DataAgentProfile;
import mark.core.RawData;
import mark.core.ServerInterface;

public class Sink implements DataAgentInterface {

    public void run(DataAgentProfile profile, ServerInterface datastore)
            throws Throwable {

		// The datastore is the connection to the server, which will be used to save data.
        // The profile represents the configuration that was provided for this data agent
        // (we will use it later).
        ServerSocket serverSocket = new ServerSocket(1555);
        Socket clientSocket = serverSocket.accept();
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        out.println("Welcome to Cat Rank! Tell me what's wrong?");

        BufferedReader in = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            String[] parts = inputLine.split(":");
            Cat cat = new Cat();
            cat.name = parts[0].trim();

            RawData<Cat> data = new RawData<Cat>();
            data.subject = cat;
            data.data = parts[1].trim();
            data.time = (int) (System.currentTimeMillis() / 1000);
            data.label = "data.cat.sink";

            datastore.addRawData(data);
        }
    }
}

```

You can now rebuild your project, and **copy the jar to the modules directory** of the server.

To run the sink together with the server, there is one step left. **In the modules directory**, you have to create a configuration file that ends with **.data.yml**. You can name it **catsink.data.yml** for example, and it should look like this:

```yml
---
class_name: cat.rank.Sink
label:      data.cat.sink
parameters: {
}
```

### Profile parameters

Instead of hardcoding the data.label value and the port on which our sink should listen, we can also fetch them from the config file. The **profile** that is provided is actually a programmatic representation of this config file. Hence we can modify our data agent like this to use the label:

```java
data.label = profile.label;
```

To get the port from the configuration file:

```java
int port = Integer.valueOf(profile.parameters.get("port"));
ServerSocket serverSocket = new ServerSocket(port);
```

And the corresponding configuration file **catsink.data.yml** should look like:

```yml
---
class_name: cat.rank.Sink
label:      data.cat.sink
parameters: {
    port:   1555
}
```

## Detection algorithms

For the detection algorithms, the principle is exactly the same, but this time we have to implement the **DetectionAgentInterface**. For example, let's say we simply want to count the number of messages a cat has sent to us.

By the way: fetching all the data and counting it is of course extremely inefficient! But this is just for the example...

```java
package cat.rank;

import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;

public class Counter implements DetectionAgentInterface<Cat>{

    public void analyze(
            Cat subject,
            String actual_trigger_label,
            DetectionAgentProfile profile,
            ServerInterface<Cat> datastore) throws Throwable {

        // Let's grab all data concerning this cat that has this label
        RawData<Cat>[] data = datastore.findRawData(
                actual_trigger_label, subject);

        Evidence<Cat> report = new Evidence<Cat>();
        report.subject = subject;
        report.score = data.length;
        report.report = "<h2>Message counter</h2><p>I found " + data.length
                + " messages from this cat</p>";

        // Use the time reference of the last submitted data
        report.time = data[data.length - 1].time;

        // As for the data agent, we use the label that was defined in the
        // configuration file
        report.label = profile.label;

        // And save the report
        datastore.addEvidence(report);
    }
}
```

Now we can once again **compile our project and put the jar in the modules directory of the server**.

Then we have to create a configuration file for the detection algorithm, and put it in the same **modules** directory. It should end with **detection.yml** this time. So you can name it **counter.detection.yml** for example:

```
---
class_name:     cat.rank.Counter
trigger_label:  data.cat
label:          detection.counter
```

## Visualizing results

We can now start the server:

```
./run.sh
```

To test the data source, open a terminal and use netcat to connect to port 1555:

![](./netcat.png)

Now if you use your browser to look at the interface, located at http://127.0.0.1:8000 by default, it should show you the ranking produced by your detection algorithm:

![](./home.png)

If you look at the status page, it will confirm that 2 or 3 detection tasks have been executed. It also shows that you have one detection algorithm configured (cat.rank.Counter), that will produce reports with the label "detection.counter". This detection is triggered when new data arrives with the label "data.cat".

![](./status.png)

## Combining detectors

The main goal of the MARK framework is to allow us to easiy combine multiple detectors. For the example, we will create a second detector that checks if a cat is asking for help:

```java
package cat.rank;

import mark.core.DetectionAgentInterface;
import mark.core.DetectionAgentProfile;
import mark.core.Evidence;
import mark.core.RawData;
import mark.core.ServerInterface;

public class HelpDetecor implements DetectionAgentInterface<Cat>{

    public void analyze(
            Cat subject,
            String actual_trigger_label,
            DetectionAgentProfile profile,
            ServerInterface<Cat> datastore) throws Throwable {

        // Let's grab all data concerning this cat that has this label
        RawData<Cat>[] data = datastore.findRawData(
                actual_trigger_label, subject);

        for (RawData<Cat> entry : data) {
            if (! entry.data.contains("HELP")) {
                continue;
            }

            Evidence<Cat> report = new Evidence<Cat>();
            report.subject = subject;
            report.score = 10;
            report.report = "<h2>HELP detector</h2>"
                    + "<p>I found a cry for help!</p>";

            // Use the time reference of the last submitted data
            report.time = data[data.length - 1].time;

            // As for the data agent, we use the label that was defined in
            // the configuration file
            report.label = profile.label;

            // And save the report
            datastore.addEvidence(report);

            // No need to go further
            return;
        }
    }
}
```

We can **compile** the project and **copy the new jar to the modules directory**.

As usual, we need to create a configuration file to activate this detector, let's call it **help.detection.yml**:

```
---
class_name:     cat.rank.HelpDetetor
trigger_label:  data.cat
label:          detection.help
```

Finally, we would like to combine the scores computed by these two algorithms, using a classical average. Luckily, there is already an agent to perform this operation. Hence we simply need to create an additional configuration file. Let's call this one **average.detection.yml**:

```
---
class_name:     mark.detection.Average
trigger_label:  detection
label:          aggregate.average
```

The **trigger label** we defined ("detection") indicates that this agent should be triggered each time there is new data with a **label that starts with** "detection". Hence it will be triggered by both our Counter agent and our HelpDetection agent.

If you restart the server and head to the status page, it will show you have three detectors now, and how the triggering happens.

![](./combine-detectors.png)

On the homepage you can choose which ranking you wish to visualize.

All source files and configuration files are available on a dedicated git repository:

https://github.com/RUCD/mark-tutorial