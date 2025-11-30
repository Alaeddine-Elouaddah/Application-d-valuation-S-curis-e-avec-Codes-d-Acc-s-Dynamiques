#!/bin/bash
echo "Compilation et lancement de l'application JavaFX..."
./mvnw clean compile
if [ $? -ne 0 ]; then
    echo "Erreur lors de la compilation"
    exit 1
fi
echo "Lancement de l'application..."
./mvnw javafx:run

