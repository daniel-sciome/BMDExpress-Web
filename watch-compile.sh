#!/bin/bash

# Auto-compile script for Spring Boot DevTools hot reload
# Watches src/main/java for changes and triggers mvn compile

echo "🔍 Watching src/main/java for changes..."
echo "📦 Will auto-compile when Java files are modified"
echo "🔄 Spring Boot DevTools will then auto-reload"
echo ""

LAST_HASH=""

while true; do
    # Get hash of all Java files
    CURRENT_HASH=$(find src/main/java -type f -name "*.java" -exec stat -c '%Y' {} \; 2>/dev/null | md5sum)

    if [ "$LAST_HASH" != "" ] && [ "$CURRENT_HASH" != "$LAST_HASH" ]; then
        echo "🔨 Changes detected, compiling..."
        mvn compile -q
        if [ $? -eq 0 ]; then
            echo "✅ Compilation successful at $(date +%H:%M:%S)"
        else
            echo "❌ Compilation failed at $(date +%H:%M:%S)"
        fi
    fi

    LAST_HASH="$CURRENT_HASH"
    sleep 2
done
