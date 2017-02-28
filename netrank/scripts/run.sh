#!/usr/bin/env bash

ROOT=`dirname "$0"`
ROOT=`cd "$ROOT"; pwd`

# copy netrank.jar to the modules directory, so the server can find it
NETRANK=`ls "$ROOT/../target/netrank-"*".jar" | sort -n | head -1`
rm "$ROOT/modules/"*".jar"
cp "$NETRANK" "$ROOT/modules/"

# Build classpath
SERVER=`ls "$ROOT/../../server/target/server-"*".jar" | sort -n | head -1`
CLASSPATH=$SERVER
for f in $ROOT/../../server/target/libs/*.jar; do 
  CLASSPATH=${CLASSPATH}:$f;
done

# start the server with appropriate config file
java -Xms1024m -cp "$CLASSPATH" mark.server.Main -c "$ROOT/config.yml"
