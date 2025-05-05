#!/bin/sh

# ---------------------------------------------------------------------------
# Stop script for the AngusTester application (Initialize by Maven).
# Usage: ./shutdown-tester.sh
# Author: XiaoLong Liu
# ---------------------------------------------------------------------------

SLEEP=6
EXECUTABLE=@project.build.finalName@.jar

CURRENT_HOME=`dirname "$0"`
# Only set TESTER_HOME if not already set
[ -z "$TESTER_HOME" ] && TESTER_HOME=`cd "$CURRENT_HOME" >/dev/null; pwd`
echo "Home Dir: $TESTER_HOME"

TESTER_PID="${TESTER_HOME}/tester.pid"
if [ -f "$TESTER_PID" ]; then
    if [ -s "$TESTER_PID" ]; then
        kill -0 `cat "$TESTER_PID"` >/dev/null 2>&1
        if [ $? -gt 0 ]; then
            echo "PID file found but no matching process was found."
        else
            PID=`cat "$TESTER_PID"` # TESTER process exists
            rm -f "$TESTER_PID" >/dev/null 2>&1
        fi
    else
        echo "PID file is empty"
        rm -f "$TESTER_PID" >/dev/null 2>&1
    fi
else
    echo "PID file not found"
fi

if [ -z "$PID" ]; then
    PID=`ps -ef |grep $EXECUTABLE |grep -v grep |awk '{print $2}'`
    if [ -z "$PID" ]; then
        echo "AngusTester process not found, shutdown aborted."
        exit 0
    fi;
fi

echo "Attempting to stop the process through OS signal."
kill -15 $PID >/dev/null 2>&1
sleep 3

PID=`ps -ef |grep $EXECUTABLE |grep -v grep |awk '{print $2}'`
if [ ! -z "$PID" ]; then
    sleep $SLEEP
    kill -9 $PID >/dev/null 2>&1
    echo "AngusTester process is killed, PID=$PID"
else
    echo "AngusTester process is stopped"
fi;
