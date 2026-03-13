#!/bin/bash
# run_gui.sh - runs the Swing GUI application
# Usage: ./run_gui.sh <dbname> <port> <user>
if [ -d "/opt/homebrew/opt/openjdk" ]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk
    export PATH="$JAVA_HOME/bin:$PATH"
fi
java -cp "lib/*:bin/" MechanicShopGUI $1 $2 $3
