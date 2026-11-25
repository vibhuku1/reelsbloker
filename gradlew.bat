@rem Gradle startup script for Windows.
@rem Run: gradlew.bat assembleDebug

@setlocal
@set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
@java %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
@endlocal
