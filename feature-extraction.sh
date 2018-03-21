#!/usr/bin/env bash

CORPUS="$1"
FEATURES="$2"
SERVER="wsdmcup17-data-server-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
CLIENT="wsdmcup17-wdvd-baseline-feature-extraction-0.0.1-SNAPSHOT-jar-with-dependencies.jar"

JAVA="java"
JAVA_FLAGS='-Xms100m -Xmx128g -XX:+UseSerialGC'

if  grep -F "$SERVER" /proc/[0-9]*/cmdline ||
    grep -F "$CLIENT" /proc/[0-9]*/cmdline ; then
        echo 'Previous instances are still running. Terminate them before continuing.'
        exit 1
fi

SCORE_DIR="$(mktemp -d --tmpdir wsdmcup17-XXXXXXXXXX)"

# Clean up when script exits
trap 'kill $(jobs -p) ; rm -rf "$SCORE_DIR"' EXIT

FILE=wdvc16_2012_10; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60000 >> /dev/null &
FILE=wdvc16_2012_11; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60001 >> /dev/null &
FILE=wdvc16_2013_01; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60002 >> /dev/null &
FILE=wdvc16_2013_03; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60003 >> /dev/null &
FILE=wdvc16_2013_05; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60004 >> /dev/null &
FILE=wdvc16_2013_07; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60005 >> /dev/null &
FILE=wdvc16_2013_09; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60006 >> /dev/null &
FILE=wdvc16_2013_11; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60007 >> /dev/null &
FILE=wdvc16_2014_01; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60008 >> /dev/null &
FILE=wdvc16_2014_03; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60009 >> /dev/null &
FILE=wdvc16_2014_05; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60010 >> /dev/null &
FILE=wdvc16_2014_07; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60011 >> /dev/null &
FILE=wdvc16_2014_09; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60012 >> /dev/null &
FILE=wdvc16_2014_11; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60013 >> /dev/null &
FILE=wdvc16_2015_01; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60014 >> /dev/null &
FILE=wdvc16_2015_03; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60015 >> /dev/null &
FILE=wdvc16_2015_05; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60016 >> /dev/null &
FILE=wdvc16_2015_07; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60017 >> /dev/null &
FILE=wdvc16_2015_09; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60018 >> /dev/null &
FILE=wdvc16_2015_11; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60019 >> /dev/null &
FILE=wdvc16_2016_01; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/training/$FILE.xml.7z"   -m "$CORPUS/training/wdvc16_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60020 >> /dev/null &
FILE=wdvc16_2016_03; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/validation/$FILE.xml.7z" -m "$CORPUS/validation/${FILE}_meta.csv.7z" -o "$SCORE_DIR/$FILE" -p 60021 >> /dev/null &
FILE=wdvc16_2016_05; "$JAVA" $JAVA_FLAGS -jar "$SERVER" -r "$CORPUS/testing/$FILE.xml.7z"    -m "$CORPUS/testing/${FILE}_meta.csv.7z"    -o "$SCORE_DIR/$FILE" -p 60022 >> /dev/null &

sleep 10s

"$JAVA" $JAVA_FLAGS -jar "$CLIENT" -s 127.0.0.1:60000 -t testToken "$FEATURES"
