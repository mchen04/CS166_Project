#!/bin/bash
# compile.sh - compiles MechanicShop and MechanicShopGUI
# Usage: ./compile.sh
if [ -d "/opt/homebrew/opt/openjdk" ]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk
    export PATH="$JAVA_HOME/bin:$PATH"
fi
rm -rf bin/*.class
javac -cp "lib/*" src/MechanicShop.java src/MechanicShopGUI.java -d bin/
echo "compiled MechanicShop.java and MechanicShopGUI.java"
