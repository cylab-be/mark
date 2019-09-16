#!/usr/bin/env bash

DIR=`dirname "$0"`
DIR=`cd "$DIR"; pwd`
NOW=$(date +"%Y%m%d%H%M%S")
DEPLOY_DIR="/tmp/mark-$NOW"

echo "Deploying to $DEPLOY_DIR..."
mkdir $DEPLOY_DIR

cd $DEPLOY_DIR
cp "$DIR/../server/target/server-"*"-standalone.tar.gz" ./server-standalone.tar.gz

tar --extract --file=./server-standalone.tar.gz --strip-components=1 --directory=$DEPLOY_DIR;

cp "$DIR/target/example-"*".jar" ./modules
cp "$DIR/resources/modules/"* ./modules
cp "$DIR/resources/config.yml" ./

./run.sh
