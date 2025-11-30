@echo off
echo ========================================
echo Lancement de l'application JavaFX
echo ========================================
echo.

REM Vérifier que Maven a téléchargé les dépendances
echo Compilation du projet...
call mvnw.cmd clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] La compilation a échoué
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Lancement avec JavaFX Maven Plugin...
call mvnw.cmd javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo [ERREUR] Impossible de lancer l'application
    echo ========================================
    echo.
    echo Solutions possibles:
    echo 1. Vérifiez que Java 21 est installé: java -version
    echo 2. Vérifiez que les dépendances sont téléchargées: mvnw.cmd dependency:resolve
    echo 3. Essayez de nettoyer et recompiler: mvnw.cmd clean install
    echo.
    pause
)

