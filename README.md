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


## Maven modules
- **core:** contains core interface definitions (Evidence, RawData, ServerInterface)
- **client**: contains a java client class to connect to the server and perform requests
- **server:** contains the server code, with basic agents
- **integration:** contains integration tests (which require compiled core, client and server code)
- **mark:** maven parent module, that allows to compile and test all modules at once