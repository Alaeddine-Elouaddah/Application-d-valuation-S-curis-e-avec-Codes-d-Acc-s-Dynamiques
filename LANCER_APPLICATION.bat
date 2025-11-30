@echo off
echo ========================================
echo   LANCEMENT DE L'APPLICATION JAVAFX
echo ========================================
echo.

REM Vérifier qu'on est à la racine du projet
if not exist "pom.xml" (
    echo [ERREUR] Le fichier pom.xml n'est pas trouvé!
    echo.
    echo Assurez-vous d'être à la racine du projet (où se trouve pom.xml)
    echo.
    pause
    exit /b 1
)

echo [1/3] Téléchargement des dépendances...
call mvnw.cmd dependency:resolve -q
if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Impossible de télécharger les dépendances
    pause
    exit /b %ERRORLEVEL%
)

echo [2/3] Compilation du projet...
call mvnw.cmd clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] La compilation a échoué
    pause
    exit /b %ERRORLEVEL%
)

echo [3/3] Lancement de l'application JavaFX...
echo.
echo L'application va s'ouvrir dans quelques secondes...
echo.

call mvnw.cmd javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo [ERREUR] Impossible de lancer l'application
    echo ========================================
    echo.
    pause
)

