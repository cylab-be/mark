#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

SERVER=`ls "$bin/../target/server-"*".jar" | sort -n | head -1`
LIBS="$bin/../target/libs/"*

echo "$SERVER"
java -Xms1024m -cp "$LIBS:$SERVER" be.cylab.mark.server.Main -c "$bin/config.yml"
