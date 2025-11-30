@echo off
echo ========================================
echo   CONFIGURATION JAVA_HOME TEMPORAIRE
echo ========================================
echo.

REM Définir JAVA_HOME pour cette session
set JAVA_HOME=C:\Users\hp zbook\.jdks\openjdk-23.0.1
set PATH=%JAVA_HOME%\bin;%PATH%

echo JAVA_HOME configure: %JAVA_HOME%
echo.

REM Vérifier Java
"%JAVA_HOME%\bin\java.exe" -version
if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Java n'est pas trouve a: %JAVA_HOME%
    echo.
    echo Verifiez que le chemin est correct!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   LANCEMENT DE L'APPLICATION
echo ========================================
echo.

call mvnw.cmd javafx:run

pause

