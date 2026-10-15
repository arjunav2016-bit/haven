@echo off
chcp 65001 > nul
echo Compiling Haven...
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin -cp "lib/*" @sources.txt
del sources.txt
if %ERRORLEVEL% equ 0 (
    echo Launching Haven...
    java -Dfile.encoding=UTF-8 -cp "bin;lib/*" haven.Main
) else (
    echo Compilation failed.
    pause
)
