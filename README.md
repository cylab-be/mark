# MARK
[![Build Status](https://travis-ci.org/RUCD/mark.svg?branch=master)](https://travis-ci.org/RUCD/mark)

Multi Agent Ranking Framework


## Architecture

- **server** consists of:
  - Optionaly: **data agents** to push data to the datastore. Data agents can be
    - **file based**: they read a file containing the data and push the content to the datastore
    - **network based**: they open a tcp server that receives data (e.g. syslog) which is pushed to the dataster
  - A **datastore** that combines:
    - A **json-rpc server** (based on a Jetty http server) that will receive requests to add or retrieve data. The rpc-server connects to a **MongoDB** server to actually store the data.
    - An **activation logic engine** that decides which agents should be triggered when new data or new evidences are saved to the database. The activation logic engine starts and connects to an Apache Ignite compute cluster to run the analysis tasks.
    - A **http file server** (also based on Jetty) to serve the web interface. This web interface is implemented using PHP and JavaScript.
    
The server is independant of the data that will be analyzed (the subject). Hence the actual implementation must provide a SubjectAdapter...


## Maven modules
- **core:** contains core interface definitions (Evidence, RawData, ServerInterface)
- **client**: contains a java client class to connect to the server and perform requests
- **server:** contains the server code, with basic agents
- **integration:** contains integration tests (which require compiled core, client and server code)
- **masfad2:** contains a LinkAdapter (that implements the SubjectAdapter interface) to run the system with network related data, to rank the links bewteen computers according to suspiciousness
- **mark:** maven parent module, that allows to compile and test all modules at once

## Building & starting

To run the system, you need a MongoDB server running.

To build the complete system:
```
cd mark
mvn clean install
```

In the server/scripts folder, there are different scripts to start the server, with different configurations. To start a small server with only a single data source and a few detection agents:

```
cd masfad2
./scripts/start-small.sh
```

When the server is started, it will launch your browser and display the status page.

## JSON-RPC calls
Here are the json-rpc method provided by the datastore:

### status
![status json-rpc call](./status-rpc.png)
