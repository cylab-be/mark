#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

java -Xms1024m -jar "$bin/../target/masfad2-0.1-SNAPSHOT.jar" -c "$bin/config-basic.yml"
