@echo off
title HAVEN Project Presentation Launcher
echo ============================================================
echo        HAVEN - B.Tech Project Presentation Launcher
echo ============================================================
echo.
echo Select how you want to present:
echo   [1] Open Microsoft PowerPoint Deck (haven_presentation.pptx)  [RECOMMENDED FOR VIVA]
echo   [2] Open Interactive High-Def Deck (haven_presentation.html)
echo   [3] Recompile and Regenerate PowerPoint Deck
echo.
set /p opt="Enter choice (1/2/3) [Default: 1]: "
if "%opt%"=="" set opt=1

if "%opt%"=="1" (
    echo Launching Microsoft PowerPoint presentation...
    start "" "%~dp0haven_presentation.pptx"
    goto end
)

if "%opt%"=="2" (
    echo Launching browser presentation deck...
    start "" "%~dp0haven_presentation.html"
    goto end
)

if "%opt%"=="3" (
    echo Regenerating PowerPoint file from Java source...
    cmd /c "javac -encoding UTF-8 GeneratePresentationPPTX.java && java GeneratePresentationPPTX"
    echo Done! Opening newly generated PPTX...
    start "" "%~dp0haven_presentation.pptx"
    goto end
)

:end
exit /b 0
