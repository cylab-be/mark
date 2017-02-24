#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

SERVER=`ls "$bin/../target/server-"*".jar" | sort -n | head -1`
LIBS="$bin/../target/libs/"*

java -Xms1024m -cp "$LIBS:$SERVER" mark.server.Main -c "$bin/config.yml"
