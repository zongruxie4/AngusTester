#!/bin/sh

# ---------------------------------------------------------------------------
# Start script for the AngusTester application (Initialize by Maven).
# Usage: ./startup-tester.sh
# Author: XiaoLong Liu
# ---------------------------------------------------------------------------

CURRENT_HOME=`dirname "$0"`
# Only set TESTER_HOME if not already set
[ -z "$TESTER_HOME" ] && TESTER_HOME=`cd "$CURRENT_HOME" >/dev/null; pwd`
echo "App Home: $TESTER_HOME"

# Init java environment
eval "$(./init-jdk.sh)"
echo "Java Home: ${JAVA_HOME}"

# Check that target jar exists
EXECUTABLE=@project.build.finalName@.jar
if [ ! -x "$TESTER_HOME"/"$EXECUTABLE" ]; then
    echo "Cannot find $TESTER_HOME/$EXECUTABLE"
    echo "The jar file is absent or does not have execute permission"
    exit 1
fi

# Define config path
if [ -z "$TESTER_CONF_DIR" ] ; then
    TESTER_CONF_DIR="$TESTER_HOME"/conf
    TESTER_CONF_LOG_FILE="$TESTER_CONF_DIR"/@archive.name@-logback.xml
    # Create config path
    # mkdir -p "$TESTER_CONF_DIR"
fi
echo "Conf Dir: $TESTER_CONF_DIR"

# Define the console log path for the tester
if [ -z "$TESTER_LOG_DIR" ] ; then
    TESTER_LOG_DIR="$TESTER_HOME"/logs
    TESTER_CONSOLE_LOG="$TESTER_LOG_DIR"/@archive.name@-console.log
    # Create logs path
    mkdir -p "$TESTER_LOG_DIR" && touch "$TESTER_CONSOLE_LOG"
fi
echo "Logs Dir: $TESTER_LOG_DIR"

# Define the java.io.tmpdir to use for tester
if [ -z "$TESTER_TMPDIR" ] ; then
    TESTER_TMPDIR="$TESTER_HOME"/tmp
    # Create temp path
    mkdir -p "$TESTER_TMPDIR"
fi
echo "Temp Dir: $TESTER_TMPDIR"

# Check the process exists
running_check(){
    PID=`ps -ef |grep $EXECUTABLE |grep -v grep |awk '{print $2}'`
    if [ ! -z "${PID}" ]; then
        echo $PID > "$TESTER_PID"
        echo "AngusTester appears to still be running with PID $PID. Startup aborted."
        echo "If the following process is not a AngusTester process, remove the PID file and try again:"
        ps -f -p $PID
        return 0
    else
        return 1
    fi;
}

# Find tester process PID
TESTER_PID="${TESTER_HOME}/tester.pid"
if [ -e "$TESTER_PID" ]; then
    if [ -s "$TESTER_PID" ]; then
        echo "Existing PID file found during AngusTester startup."
        if [ -r "$TESTER_PID" ]; then
            PID=`cat "$TESTER_PID"`
            ps -p $PID >/dev/null 2>&1
            if [ $? -eq 0 ] ; then
                echo "AngusTester appears to still be running with PID $PID. Startup aborted."
                echo "If the following process is not a AngusTester process, remove the PID file and try again:"
                ps -f -p $PID
                exit 1
            else
                echo "AngusTester process does not exist and removing stale PID file."
                running_check
                if [ $? -eq "0" ]; then
                    echo $PID > "$TESTER_PID"
                    exit 1
                else
                    rm -f $TESTER_PID >/dev/null 2>&1
                    if [ $? != 0 ]; then
                        if [ -w "$TESTER_PID" ]; then
                            cat /dev/null > $TESTER_PID
                        else
                            echo "Unable to remove stale PID file. Startup aborted."
                            exit 1
                        fi
                    fi
                fi
            fi
        else
            echo "Unable to read PID file. Startup aborted."
            exit 1
        fi
    else # tester.pid is empty
        running_check
        if [ $? -eq "0" ]; then
            echo $PID > "$TESTER_PID"
            exit 1
        else
            rm -f "$TESTER_PID" >/dev/null 2>&1
            if [ ! -w "$TESTER_PID" ]; then
                echo "Unable to delete empty PID file. Startup aborted."
                exit 1
            fi
        fi
    fi
else
    running_check
    if [ $? -eq "0" ]; then
        echo "AngusTester PID file($TESTER_PID) is missing. Update PID"
        echo $PID > "$TESTER_PID" # touch $TESTER_PID
        exit 1
    fi
fi

# Run tester
JAVA_OPTS="-server -Xnoagent -Djava.SECURITY.egd=file:/dev/./urandom -Dio.netty.tryReflectionSetAccessible=true"
nohup ${JAVA_HOME}/bin/java -jar $JAVA_OPTS \
  -DHOME_DIR=$TESTER_HOME \
  -DCONF_DIR=$TESTER_CONF_DIR \
  -DLOGS_DIR=$TESTER_LOG_DIR \
  -DPLUGIN_DIR=$TESTER_HOME/plugins \
  -Dlogback.configurationFile=$TESTER_CONF_LOG_FILE \
  -Djava.io.tmpdir=$TESTER_TMPDIR \
 $TESTER_HOME/$EXECUTABLE >> "$TESTER_CONSOLE_LOG" 2>&1 &
echo $! > "$TESTER_PID"

echo "AngusTester started, PID=$!"
