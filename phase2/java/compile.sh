#!/bin/bash
# use homebrew java on mac if available, otherwise use whatever's in PATH
if [ -d "/opt/homebrew/opt/openjdk" ]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk
    export PATH="$JAVA_HOME/bin:$PATH"
fi
rm -rf bin/*.class
javac -cp "lib/*" src/MechanicShop.java src/MechanicShopGUI.java src/TestGUI.java -d bin/
echo "compiled MechanicShop.java, MechanicShopGUI.java, and TestGUI.java"
