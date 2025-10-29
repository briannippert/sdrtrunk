#!/bin/bash
# Stop script for SDRTrunk

PID_FILE="/tmp/sdrtrunk.pid"
LOG_FILE="/tmp/sdrtrunk.log"

if [ ! -f "$PID_FILE" ]; then
    echo "SDRTrunk doesn't appear to be running (no PID file found)"
    echo "PID file expected at: $PID_FILE"
    exit 1
fi

PID=$(cat "$PID_FILE")

if ! ps -p $PID > /dev/null 2>&1; then
    echo "SDRTrunk is not running (stale PID file)"
    rm -f "$PID_FILE"
    exit 1
fi

echo "Stopping SDRTrunk (PID: $PID)..."

# Try graceful shutdown first
kill $PID

# Wait up to 10 seconds for graceful shutdown
for i in {1..10}; do
    if ! ps -p $PID > /dev/null 2>&1; then
        echo "✓ SDRTrunk stopped successfully"
        rm -f "$PID_FILE"
        exit 0
    fi
    sleep 1
    echo -n "."
done

echo ""
echo "Process didn't stop gracefully, forcing shutdown..."
kill -9 $PID

# Wait a moment
sleep 1

if ! ps -p $PID > /dev/null 2>&1; then
    echo "✓ SDRTrunk forcefully stopped"
    rm -f "$PID_FILE"
    exit 0
else
    echo "✗ Failed to stop SDRTrunk (PID: $PID)"
    echo "You may need to manually kill the process: kill -9 $PID"
    exit 1
fi
