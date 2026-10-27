@echo off
chcp 65001 > nul
if not exist bin mkdir bin

echo Compiling production and test sources...
dir /s /b src\*.java > sources.txt
dir /s /b test\*.java >> sources.txt
javac -encoding UTF-8 -d bin -cp "bin;lib/*" @sources.txt
set ERR=%ERRORLEVEL%
if exist sources.txt del sources.txt

if %ERR% neq 0 (
    echo [ERROR] Compilation failed!
    exit /b %ERR%
)

echo.
echo Running JUnit 5 Test Suite...
java -cp "bin;lib/*" org.junit.platform.console.ConsoleLauncher execute --scan-class-path %*
exit /b %ERRORLEVEL%
