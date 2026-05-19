#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$SCRIPT_DIR"

MAVEN_VERSION="3.9.15"
MAVEN_DIR="$SCRIPT_DIR/.tools/apache-maven-$MAVEN_VERSION"
MAVEN_BIN="$MAVEN_DIR/bin/mvn"
MAVEN_TGZ="$SCRIPT_DIR/.tools/apache-maven-$MAVEN_VERSION-bin.tar.gz"
MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz"

if ! command -v java >/dev/null 2>&1; then
    echo "Java was not found on PATH. Please install Java 17 or newer and try again." >&2
    exit 1
fi

JAVA_HOME_FROM_PATH=$(java -XshowSettings:properties -version 2>&1 | awk -F'= ' '/java.home =/ {print $2; exit}')
if [ -n "$JAVA_HOME_FROM_PATH" ]; then
    export JAVA_HOME="$JAVA_HOME_FROM_PATH"
fi

if command -v mvn >/dev/null 2>&1; then
    exec mvn clean javafx:run
fi

if [ ! -x "$MAVEN_BIN" ]; then
    mkdir -p "$SCRIPT_DIR/.tools"
    echo "Maven was not found. Downloading Apache Maven $MAVEN_VERSION..."

    if command -v curl >/dev/null 2>&1; then
        curl -fsSL "$MAVEN_URL" -o "$MAVEN_TGZ"
    elif command -v wget >/dev/null 2>&1; then
        wget -qO "$MAVEN_TGZ" "$MAVEN_URL"
    else
        echo "Neither curl nor wget is installed, so Maven cannot be downloaded automatically." >&2
        exit 1
    fi

    tar -xzf "$MAVEN_TGZ" -C "$SCRIPT_DIR/.tools"
fi

exec "$MAVEN_BIN" clean javafx:run
