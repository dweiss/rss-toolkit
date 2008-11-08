#!/bin/bash

# Define these properties according to your installation.

POSTGRES_HOST=
POSTGRES_DATABASE=
POSTGRES_USER=
POSTGRES_PASSWORD=

# Where is Jetty's folder?
JETTY_BIN=`pwd`/`dirname $0`

if [ -f "$JETTY_BIN/rss-toolkit.conf" ]; then
	. "$JETTY_BIN/rss-toolkit.conf"
fi

if [ -z "${POSTGRES_HOST}" ]; then
	echo "Define configuration properties in `dirname $0` or rss-toolkit.conf, see documentation."
	exit 1
fi

# Export global JVM options.
export JAVA_OPTIONS=" \
	-Djava.util.logging.config.file=$JETTY_BIN/../resources/jdk-logging.properties \
	-Dorg.mortbay.log.LogFactory.noDiscovery=false \
	-Djdbc.driverClassName=org.postgresql.Driver \
	-Djdbc.url=jdbc:postgresql://${POSTGRES_HOST}/${POSTGRES_DATABASE} \
	-Djdbc.username=${POSTGRES_USER} \
	-Djdbc.password=${POSTGRES_PASSWORD}"

$JETTY_BIN/jetty.sh $@