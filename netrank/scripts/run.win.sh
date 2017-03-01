#!/usr/bin/env bash
OS=`uname`
echo "$0"
ROOT=`dirname "$0"`
SCRIPTS=`dirname "$0"`
echo $ROOT
ROOT=`cd "$ROOT"; pwd`
echo $ROOT
if [ "$OS"=MINGW64* ]; then

# copy netrank.jar to the modules directory, so the server can find it
NETRANK=`ls "$ROOT/../target/netrank-"*".jar" | sort -r | head -1`
rm "$ROOT/modules/"*".jar"
cp "$NETRANK" "$ROOT/modules/"

# Build classpath
SERVER=`ls "$ROOT/../../server/target/server-"*".jar" | sort -r | head -1`
SERVER=${SERVER/$ROOT\/..\//}\;

CLASSPATH=""
for f in $ROOT/../../server/target/libs/*.jar; do 
  CLASSPATH=${CLASSPATH/$ROOT\/..\//}\;$f;
done
#CLASSPATH=${CLASSPATH/$ROOT//}
CLASSPATH=${CLASSPATH/$ROOT\/..\//}\;${SERVER}

#echo $CLASSPATH

CONFIG="$SCRIPTS/config.yml"
echo $CONFIG
else

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

CONFIG="$ROOT/config.yml"
fi

# start the server with appropriate config file

CMD="java -Xms1024m -cp \"$CLASSPATH\" mark.server.Main -c \"$CONFIG\""
#cmd=" -Xms1024m -cp \" $LIBS $SERVER\" mark.server.Main -c \"$bin/config.yml\""
echo $CMD
$CMD
