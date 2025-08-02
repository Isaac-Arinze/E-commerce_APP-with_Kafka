#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Startup Script
# ----------------------------------------------------------------------------

# Based on Apache Maven Wrapper 3.2.0 (compatible with Maven 3.9.x)

set -e

WRAPPER_DIR="$(cd "$(dirname "$0")" && pwd)"
MVNW_VERBOSE="${MVNW_VERBOSE:-false}"

# Locate JAVA
if [ -n "$JAVA_HOME" ] ; then
  JAVA_EXE="$JAVA_HOME/bin/java"
else
  JAVA_EXE="java"
fi

# Determine OS
cygwin=false
darwin=false
msys=false
case "$(uname)" in
  CYGWIN* ) cygwin=true ;;
  MINGW* ) msys=true ;;
  Darwin* ) darwin=true ;;
esac

# Resolve .mvn directory
MVN_WRAPPER_DIR="$WRAPPER_DIR/.mvn"
WRAPPER_JAR="$MVN_WRAPPER_DIR/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MVN_WRAPPER_DIR/wrapper/maven-wrapper.properties"

# Download URL default if not present in properties
DEFAULT_WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

# Helper: log
log() {
  if [ "$MVNW_VERBOSE" = "true" ]; then
    echo "$@"
  fi
}

# Ensure wrapper jar exists (best-effort download if missing)
if [ ! -f "$WRAPPER_JAR" ]; then
  log "maven-wrapper.jar not found. Attempting to download..."
  # Try using wget or curl if available
  if command -v wget >/dev/null 2>&1; then
    mkdir -p "$MVN_WRAPPER_DIR/wrapper"
    wget -q -O "$WRAPPER_JAR" "$DEFAULT_WRAPPER_URL" || true
  elif command -v curl >/dev/null 2>&1; then
    mkdir -p "$MVN_WRAPPER_DIR/wrapper"
    curl -s -L -o "$WRAPPER_JAR" "$DEFAULT_WRAPPER_URL" || true
  else
    echo "Warning: Could not find wget or curl to download maven-wrapper.jar."
    echo "Please download it manually from:"
    echo "  $DEFAULT_WRAPPER_URL"
    echo "and place it at:"
    echo "  $WRAPPER_JAR"
  fi
fi

# Transform paths for Cygwin/MinGW
if $cygwin || $msys ; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME="$(cygpath --unix "$JAVA_HOME")"
  WRAPPER_DIR="$(cygpath --unix "$WRAPPER_DIR")"
  MVN_WRAPPER_DIR="$(cygpath --unix "$MVN_WRAPPER_DIR")"
  WRAPPER_JAR="$(cygpath --unix "$WRAPPER_JAR")"
  WRAPPER_PROPERTIES="$(cygpath --unix "$WRAPPER_PROPERTIES")"
fi

# JVM Options
MAVEN_OPTS="${MAVEN_OPTS:-}"
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

# Execute Maven Wrapper
exec "$JAVA_EXE" \
  $MAVEN_OPTS \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$WRAPPER_DIR" \
  $WRAPPER_LAUNCHER "$@"
