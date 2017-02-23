# Tutorial

Suppose you care for a large group of say... cats. Now you would like to know which one of your cats need attention, using some very serious algorithms. This tutorial will show you how to:
* Install and run the MARK server
* Create your connector to feed information about your cats in the server
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

## Create a maven project

It's now time to create your own modules for feeding data into the system and perform detection. The easiest way to do so is using maven:

```
mvn -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.1 -DgroupId=my -DartifactId=cats-ranking -Dversion=0.1-SNAPSHOT -Dpackage=my.cats.ranking  -Darchetype.interactive=false --batch-mode archetype:generate
```

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

The server should now what it will be ranking, and how to treat it. This is the role of the Subject and SubjectAdapter.

In our case we will be ranking cats:

```java
package my.cat.ranking;

import mark.core.Subject;

public class Cat implements Subject {
    public String name = "";
}
```

The adapter is used to show the server how to (de)serialize the Subject to JSON and to/from Mongo:
```java
package info.debatty.mark.tutorial;

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

## Data sources

## Detection algorithms