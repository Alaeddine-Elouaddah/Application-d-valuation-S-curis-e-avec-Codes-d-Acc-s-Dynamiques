@echo off
echo ========================================
echo     DOCKER JAVA FX - PROJET QCM
echo ========================================
echo.

echo Étape 1: Nettoyage...
docker stop projetqcm-container 2>nul
docker rm projetqcm-container 2>nul
docker rmi projetqcm:latest 2>nul

echo.
echo Étape 2: Construction du JAR...
call mvnw.cmd clean package
if %errorlevel% neq 0 (
    echo ERREUR lors de la construction du JAR!
    pause
    exit /b 1
)

echo.
echo Étape 3: Vérification du JAR...
if not exist "target\ProjetQcm-1.0-SNAPSHOT.jar" (
    echo ERREUR: JAR non trouvé!
    pause
    exit /b 1
)
echo ✓ JAR trouvé: target\ProjetQcm-1.0-SNAPSHOT.jar

echo.
echo Étape 4: Construction de l'image Docker...
docker build --progress=plain --no-cache -t projetqcm:latest .

if %errorlevel% neq 0 (
    echo ERREUR lors de la construction Docker!
    pause
    exit /b 1
)

echo.
echo Étape 5: Lancement du conteneur...
echo ========================================
echo L'application va démarrer dans Docker...
echo Si l'interface ne s'affiche pas, elle tourne en arrière-plan.
echo Pour voir les logs: docker logs -f projetqcm-container
echo ========================================
echo.

docker run -it --rm \
    --name projetqcm-container \
    -e DISPLAY=:99 \
    -e JAVA_OPTS="-Dglass.platform=gtk -Dprism.order=sw" \
    projetqcm:latest

echo.
pause