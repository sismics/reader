#!/bin/sh

#################################################################
#
# Sismics Reader standalone startup script.
#
# Author: Jean-Marc Tremeaux <jm.tremeaux@gmail.com>
#
#################################################################

READER_HOME=/var/reader
#READER_HOST=0.0.0.0
#READER_PORT=4001
#READER_CONTEXT_PATH=/
READER_MAX_MEMORY=150
READER_PIDFILE=

# SSL configuration here
# Set READER_SECURE to true to activate SSL
READER_SECURE=false
# Set READER_HEADER_AUTHENTICATION to true to activate header-based authentication
READER_HEADER_AUTHENTICATION=false
# Your keystore path
READER_KEYSTORE_PATH=
# The keystore password
READER_KEYSTORE_PASSWORD=
# The keymanager password
READER_KEYMANAGER_PASSWORD=

quiet=0

usage() {
    echo "Usage: reader.sh [options]"
    echo "  --help               This small usage guide."
    echo "  --home=DIR           The directory where reader will create its files (database, index...)"
    echo "                       Make sure it is writable. Default: /var/reader"
    echo "  --host=HOST          The host name or IP address on which to bind Reader."
    echo "                       Only relevant if you have multiple network interfaces and want"
    echo "                       to make Reader available on only one of them. The default value 0.0.0.0"
    echo "                       will bind Reader to all available network interfaces."
    echo "  --port=PORT          The port on which Reader will listen for incoming HTTP traffic"
    echo "                       incoming HTTP traffic. Default: 4001."
    echo "  --context-path=PATH  The context path (i.e., the last part of the Reader URL)."
    echo "                       Typically '/' or '/reader'. Default: '/'."
    echo "  --max-memory=MB      The memory limit (max Java heap size) in megabytes."
    echo "                       Default: 150"
    echo "  --pidfile=PIDFILE    Write PID to this file. Default: not created."
    echo "  --quiet              Don't print anything to standard out. Default: false."
    exit 1
}

# Parse arguments.
while [ $# -ge 1 ]; do
    case $1 in
        --help)
            usage
            ;;
        --home=?*)
            READER_HOME=${1#--home=}
            ;;
        --host=?*)
            READER_HOST=${1#--host=}
            ;;
        --port=?*)
            READER_PORT=${1#--port=}
            ;;
        --context-path=?*)
            READER_CONTEXT_PATH=${1#--context-path=}
            ;;
        --max-memory=?*)
            READER_MAX_MEMORY=${1#--max-memory=}
            ;;
        --pidfile=?*)
            READER_PIDFILE=${1#--pidfile=}
            ;;
        --quiet)
            quiet=1
            ;;
        *)
            usage
            ;;
    esac
    shift
done

# Use JAVA_HOME if set, otherwise assume java is in the path.
JAVA=java
if [ -e "${JAVA_HOME}" ]
    then
    JAVA=${JAVA_HOME}/bin/java
fi

# Create Reader home directory.
mkdir -p ${READER_HOME}
if [ $? -ne 0 ] ; then
    echo Error creating reader base directory ${READER_HOME}. Make sure the directory is writable.
    exit 1
fi
LOG=${READER_HOME}/reader_startup.log
rm -f ${LOG}

cd $(dirname $0)
if [ -L $0 ] && ([ -e /bin/readlink ] || [ -e /usr/bin/readlink ]); then
    cd $(dirname $(readlink $0))
fi

${JAVA} -Xmx${READER_MAX_MEMORY}m \
  -Dreader.home=${READER_HOME} \
  -Dreader.host=${READER_HOST} \
  -Dreader.port=${READER_PORT} \
  -Dreader.secure=${READER_SECURE} \
  -Dreader.header_authentication=${READER_HEADER_AUTHENTICATION} \
  -Dreader.keystore_path=${READER_KEYSTORE_PATH} \
  -Dreader.keystore_password=${READER_KEYSTORE_PASSWORD} \
  -Dreader.keymanager_password=${READER_KEYMANAGER_PASSWORD} \
  -Dreader.contextPath=${READER_CONTEXT_PATH} \
  -Djava.awt.headless=true \
  -jar reader-standalone.jar > ${LOG} 2>&1 &

# Write pid to pidfile if it is defined.
if [ $READER_PIDFILE ]; then
    echo $! > ${READER_PIDFILE}
fi

if [ $quiet = 0 ]; then
    echo Started Reader [PID $!, ${LOG}]
fi

