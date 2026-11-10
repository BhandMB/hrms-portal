$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$java17 = 'C:\PROGRA~1\Java\jdk-17'
if (-not (Test-Path "$java17\bin\java.exe")) {
    $java17 = 'C:\Program Files\Java\jdk-17'
}
if (Test-Path "$java17\bin\java.exe") {
    try {
        $fso = New-Object -ComObject Scripting.FileSystemObject
        $env:JAVA_HOME = $fso.GetFolder($java17).ShortPath
    } catch {
        $env:JAVA_HOME = $java17
    }
} elseif (-not $env:JAVA_HOME) {
    Write-Host 'JAVA_HOME is not set and JDK 17 was not found at C:\Program Files\Java\jdk-17.' -ForegroundColor Red
    exit 1
}

$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "Using Java:" -ForegroundColor Cyan
java --version

Write-Host "Starting HRMS Portal on http://localhost:8080" -ForegroundColor Green
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    mvn spring-boot:run
} elseif (Test-Path "$PSScriptRoot\mvnw.cmd") {
    & "$PSScriptRoot\mvnw.cmd" spring-boot:run
} else {
    Write-Host 'Maven was not found and mvnw.cmd is missing.' -ForegroundColor Red
    exit 1
}
