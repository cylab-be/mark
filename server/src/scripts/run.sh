#!/usr/bin/env bash

ROOT=`dirname "$0"`
ROOT=`cd "$ROOT"; pwd`

SERVER=`ls "$ROOT/bin/server-"*".jar" | sort -n | head -1`
LIBS="$ROOT/libs/*"
MODULES="$ROOT/modules/*"

java -Xms1024m -cp "$SERVER:$LIBS:$MODULES" be.cylab.mark.server.Main -c "$ROOT/config.yml"
