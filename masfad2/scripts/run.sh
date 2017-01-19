#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

# copy masfad2.jar to the modules directory, so the server can find it use the
# masfad2 agents
cp "$bin/../target/masfad2-0.1-SNAPSHOT.jar" "$bin/modules/"

# start the server with appropriate config file
java -Xms1024m -jar "$bin/../../server/target/server-0.1-SNAPSHOT.jar" -c "$bin/config.yml"
