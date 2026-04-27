$ErrorActionPreference = 'Stop'

$localJava = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot'
if (Test-Path $localJava) {
    $env:JAVA_HOME = $localJava
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

.\gradlew.bat bootRun
