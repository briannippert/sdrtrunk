#!/bin/bash
# Run script for SDRTrunk with Java 24 in background

PID_FILE="/tmp/sdrtrunk.pid"
LOG_FILE="/tmp/sdrtrunk.log"

# Check if already running
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "SDRTrunk is already running (PID: $PID)"
        echo "Use ./stop.sh to stop it first"
        exit 1
    else
        # Stale PID file, remove it
        rm -f "$PID_FILE"
    fi
fi

# Check if Java 24 is already in PATH
JAVA_VERSION=$(java -version 2>&1 | grep "openjdk version" | cut -d'"' -f2 | cut -d'.' -f1)

if [ "$JAVA_VERSION" != "24" ]; then
    echo "Java 24 not found in PATH. Checking for downloaded JDK..."
    
    # Check common locations for Java 24
    if [ -d "/tmp/jdk-24.0.1-full" ]; then
        export JAVA_HOME=/tmp/jdk-24.0.1-full
        export PATH=$JAVA_HOME/bin:$PATH
        echo "Using Java 24 from: $JAVA_HOME"
    elif [ -d "$HOME/jdk-24.0.1-full" ]; then
        export JAVA_HOME=$HOME/jdk-24.0.1-full
        export PATH=$JAVA_HOME/bin:$PATH
        echo "Using Java 24 from: $JAVA_HOME"
    else
        echo "ERROR: Java 24 is required but not found!"
        echo ""
        echo "Please download Java 24 from:"
        echo "https://download.bell-sw.com/java/24.0.1+11/bellsoft-jdk24.0.1+11-linux-amd64-full.tar.gz"
        echo ""
        echo "Extract it and set JAVA_HOME, or place it in /tmp or your home directory"
        exit 1
    fi
fi

echo "Starting SDRTrunk in background..."
echo "Log file: $LOG_FILE"
echo "PID file: $PID_FILE"
echo "Web UI will be available at: http://localhost:8080"
echo ""

# Start in background and capture PID
nohup ./gradlew run > "$LOG_FILE" 2>&1 &
PID=$!

# Save PID to file
echo $PID > "$PID_FILE"

# Wait a moment and check if it's still running
sleep 3
if ps -p $PID > /dev/null 2>&1; then
    echo "✓ SDRTrunk started successfully (PID: $PID)"
    echo ""
    echo "To view logs:  tail -f $LOG_FILE"
    echo "To stop:       ./stop.sh"
else
    echo "✗ Failed to start SDRTrunk"
    echo "Check the log file for errors: $LOG_FILE"
    rm -f "$PID_FILE"
    exit 1
fi
