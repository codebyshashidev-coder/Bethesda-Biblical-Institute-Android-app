$ErrorActionPreference = 'Stop'
$project = 'D:\DOWNLOADS\Temporary\BethesdaApp'
$javaHome = 'C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot'
$sdkRoot = "$env:LOCALAPPDATA\Android\Sdk"
$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$env:Path"

New-Item -ItemType Directory -Path "$project\gradle\wrapper" -Force | Out-Null
$wrapperJar = "$project\gradle\wrapper\gradle-wrapper.jar"
if (-not (Test-Path $wrapperJar)) {
    Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/gradle/gradle-wrapper/8.5.2/gradle-wrapper-8.5.2.jar' -OutFile $wrapperJar
}
Set-Content -Path "$project\gradle\wrapper\gradle-wrapper.properties" -Value @"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5.2-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"@ -Encoding ascii
Set-Content -Path "$project\gradlew.bat" -Value @"
@echo off
setlocal
set DIR=%~dp0
if "%JAVA_HOME%"=="" (
  set JAVA_EXE=java
) else (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
)
"%JAVA_EXE%" -classpath "%DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
"@ -Encoding ascii
Set-Content -Path "$project\gradlew" -Value @"
#!/usr/bin/env sh
set -e
APP_HOME=$(cd "$(dirname "$0")" && pwd)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec "$JAVA_HOME/bin/java" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
"@ -Encoding ascii

if (-not (Test-Path "$sdkRoot\cmdline-tools\latest\bin\sdkmanager.bat")) {
    New-Item -ItemType Directory -Path $sdkRoot -Force | Out-Null
    New-Item -ItemType Directory -Path "$sdkRoot\cmdline-tools" -Force | Out-Null
    $toolsZip = "$env:TEMP\cmdline-tools.zip"
    Invoke-WebRequest -Uri 'https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip' -OutFile $toolsZip
    New-Item -ItemType Directory -Path "$sdkRoot\cmdline-tools\temp" -Force | Out-Null
    Expand-Archive -Path $toolsZip -DestinationPath "$sdkRoot\cmdline-tools\temp" -Force
    New-Item -ItemType Directory -Path "$sdkRoot\cmdline-tools\latest" -Force | Out-Null
    Copy-Item "$sdkRoot\cmdline-tools\temp\cmdline-tools\*" "$sdkRoot\cmdline-tools\latest" -Recurse -Force
}
Set-Content -Path "$project\local.properties" -Value "sdk.dir=$sdkRoot" -Encoding ascii
& "$sdkRoot\cmdline-tools\latest\bin\sdkmanager.bat" --sdk_root="$sdkRoot" --install "platform-tools" "platforms;android-34" "build-tools;34.0.0"

Write-Host 'Setup complete.'
