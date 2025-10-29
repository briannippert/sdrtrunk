#!/bin/bash
# Build script for SDRTrunk with Java 24

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

echo "Building SDRTrunk..."
./gradlew build -x test "$@"
