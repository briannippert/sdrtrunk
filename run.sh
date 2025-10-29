#!/bin/bash
# Run script for SDRTrunk in background

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

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is required but not found!"
    echo ""
    echo "Please install Java and ensure it's in your PATH"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1)
echo "Using Java: $JAVA_VERSION"

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
