#!/bin/bash
if [ -d "/opt/homebrew/opt/openjdk" ]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk
    export PATH="$JAVA_HOME/bin:$PATH"
fi
java -cp "lib/*:bin/" MechanicShop $1 $2 $3
