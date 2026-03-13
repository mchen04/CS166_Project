#!/bin/bash
# run.sh - runs the CLI application
# Usage: ./run.sh <dbname> <port> <user>
if [ -d "/opt/homebrew/opt/openjdk" ]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk
    export PATH="$JAVA_HOME/bin:$PATH"
fi
java -cp "lib/*:bin/" MechanicShop $1 $2 $3
