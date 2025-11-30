@echo off
echo Compilation et lancement de l'application JavaFX...
call mvnw.cmd clean compile
if %ERRORLEVEL% NEQ 0 (
    echo Erreur lors de la compilation
    pause
    exit /b %ERRORLEVEL%
)
echo Lancement de l'application...
call mvnw.cmd javafx:run
pause

