#!/bin/bash
# Status script for SDRTrunk

PID_FILE="/tmp/sdrtrunk.pid"
LOG_FILE="/tmp/sdrtrunk.log"

echo "========================================="
echo "  SDRTrunk Status"
echo "========================================="
echo ""

if [ ! -f "$PID_FILE" ]; then
    echo "Status: NOT RUNNING"
    echo ""
    echo "Start SDRTrunk with: ./run.sh"
    exit 0
fi

PID=$(cat "$PID_FILE")

if ! ps -p $PID > /dev/null 2>&1; then
    echo "Status: NOT RUNNING (stale PID file)"
    echo ""
    rm -f "$PID_FILE"
    echo "Start SDRTrunk with: ./run.sh"
    exit 0
fi

# Get process info
PROC_INFO=$(ps -p $PID -o %cpu,%mem,etime,cmd --no-headers)

echo "Status: RUNNING ✓"
echo "PID: $PID"
echo "Process Info: $PROC_INFO"
echo ""
echo "Files:"
echo "  PID file: $PID_FILE"
echo "  Log file: $LOG_FILE"
echo ""
echo "Web UI: http://localhost:8080"
echo ""
echo "Commands:"
echo "  View logs:  tail -f $LOG_FILE"
echo "  Stop:       ./stop.sh"
echo ""

# Check if web server is responding
if command -v curl > /dev/null 2>&1; then
    if curl -s --connect-timeout 2 http://localhost:8080 > /dev/null 2>&1; then
        echo "Web Server: RESPONDING ✓"
    else
        echo "Web Server: NOT RESPONDING (may still be starting...)"
    fi
else
    echo "Web Server: Cannot check (curl not installed)"
fi

echo ""
