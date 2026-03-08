#!/bin/bash
# compile and run on ucr cs166 server
# assumes working directory is ~/phase2
DB_NAME=$USER"_project_DB"
JAVA_DIR="$PWD/java"

export CLASSPATH=$CLASSPATH:$JAVA_DIR/lib/postgresql-42.7.3.jar
javac -cp "$JAVA_DIR/lib/*" "$JAVA_DIR/src/MechanicShop.java" -d "$JAVA_DIR/bin/"
echo "compiled MechanicShop.java"

echo ""
echo "running MechanicShop..."
java -cp "$JAVA_DIR/lib/*:$JAVA_DIR/bin/" MechanicShop "$DB_NAME" "$PGPORT" "$USER"
