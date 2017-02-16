#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`


NETRANK=`ls "$bin/../target/netrank-"*".jar" | sort -n | head -1`
SERVER=`ls "$bin/../../server/target/server-"*".jar" | sort -n | head -1`

# copy masfad2.jar to the modules directory, so the server can find it use the
# masfad2 agents
cp "$NETRANK" "$bin/modules/"

# start the server with appropriate config file
java -Xms1024m -jar "$SERVER" -c "$bin/config.yml"
