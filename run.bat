@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

set "MAVEN_VERSION=3.9.15"
set "MAVEN_DIR=%SCRIPT_DIR%.tools\apache-maven-%MAVEN_VERSION%"
set "MAVEN_BIN=%MAVEN_DIR%\bin\mvn.cmd"
set "MAVEN_ZIP=%SCRIPT_DIR%.tools\apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip"

where java >nul 2>nul
if errorlevel 1 (
    echo Java was not found on PATH. Please install Java 17 or newer and try again.
    exit /b 1
)

for /f "tokens=2 delims==" %%I in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /c:"java.home ="') do (
    set "JAVA_HOME=%%I"
)
set "JAVA_HOME=%JAVA_HOME:~1%"

where mvn >nul 2>nul
if not errorlevel 1 (
    mvn clean javafx:run
    exit /b %errorlevel%
)

if not exist "%MAVEN_BIN%" (
    if not exist "%SCRIPT_DIR%.tools" mkdir "%SCRIPT_DIR%.tools"
    echo Maven was not found. Downloading Apache Maven %MAVEN_VERSION%...
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "$ProgressPreference='SilentlyContinue';" ^
        "Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%';" ^
        "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%SCRIPT_DIR%.tools' -Force"
    if errorlevel 1 (
        echo Failed to download Maven.
        exit /b 1
    )
)

"%MAVEN_BIN%" clean javafx:run
exit /b %errorlevel%
