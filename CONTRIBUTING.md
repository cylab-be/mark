# Contributing

## Architecture

A MARK **server** consists of:
  - A **datastore** : a **json-rpc server** (based on a Jetty http server) that will receive requests to add or retrieve data. The rpc-server connects to a **MongoDB** server to actually store the data.
  - An **activation logic engine** that decides which agents should be triggered when new data or new evidences are saved to the database. The activation logic engine starts and connects to an Apache Ignite compute cluster to run the analysis tasks.
  - A **web server** (also based on Jetty) to serve the web interface. This web interface is implemented using PHP and JavaScript.
  - Optionaly: **data agents** to push data to the datastore. Data agents can be
    - **file based**: they read a file containing the data and push the content to the datastore
    - **network based**: they open a tcp server that receives data (e.g. syslog) which is pushed to the datastore

The server is independant of the data that will be analyzed (the subject). Hence the actual implementation must provide a SubjectAdapter...

## Maven modules
- **core:** contains core interface definitions (Evidence, RawData, ServerInterface)
- **client**: contains a java client class to connect to the server and perform requests
- **server:** contains the server code, with basic agents
- **integration:** contains integration tests (which require compiled core, client and server code)
- **example**

## Building & running

To run the system, you need a MongoDB server running.

To build the complete system:
```
mvn clean install
```

The server/scripts folder contains a startup script with a basic configuration (no agents).

## UML

![Server UML diagram](https://gitlab.cylab.be/cylab/mark/-/jobs/artifacts/master/raw/server/target/server.urm.png?job=test:jdk-11:mongo-4.4)



