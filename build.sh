#!/usr/bin/env bash
# ScrollStop – One-shot build script
# Run this from the ScrollStop/ project root with Android Studio's JDK in PATH.
# Example: JAVA_HOME=/Applications/Android\ Studio.app/Contents/jbr ./build.sh

set -e

echo "=== ScrollStop Build Script ==="

# 1. Verify Java
echo "Java: $(java -version 2>&1 | head -1)"

# 2. Clean
./gradlew clean

# 3. Run unit tests
echo "--- Running unit tests ---"
./gradlew test

# 4. Build debug APK
echo "--- Building debug APK ---"
./gradlew assembleDebug

# 5. Build release APK (unsigned)
echo "--- Building release APK (unsigned) ---"
./gradlew assembleRelease

echo ""
echo "=== Build complete ==="
echo "Debug APK:   app/build/outputs/apk/debug/app-debug.apk"
echo "Release APK: app/build/outputs/apk/release/app-release-unsigned.apk"
echo ""
echo "To install debug APK on connected device:"
echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
