#!/bin/bash
# Build script for SDRTrunk

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is required but not found!"
    echo ""
    echo "Please install Java and ensure it's in your PATH"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1)
echo "Using Java: $JAVA_VERSION"

echo "Building SDRTrunk..."
./gradlew build -x test "$@"
