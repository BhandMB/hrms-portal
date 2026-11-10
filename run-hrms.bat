@echo off
cd /d "%~dp0"
if exist "C:\PROGRA~1\Java\jdk-17\bin\java.exe" (
  set "JAVA_HOME=C:\PROGRA~1\Java\jdk-17"
) else if exist "C:\Program Files\Java\jdk-17\bin\java.exe" (
  for %%I in ("C:\Program Files\Java\jdk-17") do set "JAVA_HOME=%%~sI"
)
if not defined JAVA_HOME (
  echo JAVA_HOME is not set and JDK 17 was not found at C:\Program Files\Java\jdk-17.
  pause
  exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
java --version
where mvn >nul 2>nul
if not errorlevel 1 (
  call mvn spring-boot:run
  exit /b %errorlevel%
)
if exist "%~dp0mvnw.cmd" (
  call "%~dp0mvnw.cmd" spring-boot:run
  exit /b %errorlevel%
)
echo Maven was not found and mvnw.cmd is missing.
pause
exit /b 1
