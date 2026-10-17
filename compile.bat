@echo off
chcp 65001 > nul
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin -cp "lib/*" @sources.txt
set ERR=%ERRORLEVEL%
if exist sources.txt del sources.txt
if %ERR% equ 0 (
    echo COMPILE_OK
) else (
    echo COMPILE_FAILED
)
exit /b %ERR%
