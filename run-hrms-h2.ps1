$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$candidates = @(
    'C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot',
    'C:\PROGRA~1\Java\jdk-17',
    'C:\Program Files\Java\jdk-17'
)
$foundJava = $null
foreach ($cand in $candidates) {
    if (Test-Path "$cand\bin\java.exe") {
        $foundJava = $cand
        break
    }
}
if ($foundJava) {
    $env:JAVA_HOME = $foundJava
} elseif (-not $env:JAVA_HOME) {
    throw 'JDK 17 was not found. Set JAVA_HOME to a JDK 17 installation.'
}
$env:Path = "C:\Windows\System32;C:\Windows\System32\WindowsPowerShell\v1.0;$env:JAVA_HOME\bin;$env:Path"
$env:SPRING_PROFILES_ACTIVE = 'h2'
Write-Host 'Starting HRMS Portal with local H2 persistence on http://localhost:8080' -ForegroundColor Green
if (Get-Command mvn -ErrorAction SilentlyContinue) { mvn spring-boot:run }
elseif (Test-Path "$PSScriptRoot\mvnw.cmd") { & "$PSScriptRoot\mvnw.cmd" spring-boot:run }
else { throw 'Maven was not found and mvnw.cmd is missing.' }
