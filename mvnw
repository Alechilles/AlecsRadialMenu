#!/bin/sh
# Maven Wrapper script
# Uses maven-wrapper.jar to download and run Maven

BASEDIR=$(cd "$(dirname "$0")" && pwd)
WRAPPER_JAR="$BASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Error: maven-wrapper.jar not found at $WRAPPER_JAR"
    echo "Please ensure the .mvn/wrapper directory exists with maven-wrapper.jar"
    exit 1
fi

# Resolve Java from JAVA_HOME first, then PATH.
if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
elif command -v java >/dev/null 2>&1; then
    JAVACMD=$(command -v java)
else
    echo "Error: Java runtime not found. Set JAVA_HOME or add java to PATH."
    exit 1
fi

# Set MAVEN_PROJECTBASEDIR for multi-module support
export MAVEN_PROJECTBASEDIR="$BASEDIR"

exec "$JAVACMD" $MAVEN_OPTS \
    -Dmaven.multiModuleProjectDirectory="$BASEDIR" \
    -classpath "$WRAPPER_JAR" \
    org.apache.maven.wrapper.MavenWrapperMain "$@"
