@echo off
setlocal
cd /d "%~dp0"
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
if not exist "%JAVA_HOME%\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-17"
if not exist "%JAVA_HOME%\bin\java.exe" set "JAVA_HOME=C:\PROGRA~1\Java\jdk-17"
set "PATH=C:\Windows\System32;C:\Windows\System32\WindowsPowerShell\v1.0;%JAVA_HOME%\bin;C:\Program Files\apache-maven-3.9.16\bin;%PATH%"
set "SPRING_PROFILES_ACTIVE=h2"
echo ========================================================
echo  Starting HRMS Portal (Advanced) with Local H2 Database
echo  URL: http://localhost:8080
echo  Credentials: admin / Admin@123
echo ========================================================
call mvn spring-boot:run
pause
