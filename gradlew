#!/usr/bin/env sh
#
# Gradle start-up script for POSIX compatible shells.
# (Minimal bootstrap to let Android Studio and CI auto-download the right Gradle version.)

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Start Gradle
exec "$JAVACMD" $GRADLE_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
