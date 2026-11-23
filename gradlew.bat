@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"
java -cp "gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
endlocal