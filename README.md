# Système de Gestion d'Examens QCM

Application JavaFX pour la gestion d'examens QCM en ligne avec surveillance et sécurité.

## Prérequis

- Java 21 ou supérieur
- Maven 3.6+ (ou utilisez mvnw inclus)
- MongoDB (mongodb://localhost:27017)

## Installation et Lancement

### 1. Démarrer MongoDB
Assurez-vous que MongoDB est démarré sur `localhost:27017`

### 2. Télécharger les dépendances
```bash
mvnw dependency:resolve
```

### 3. Compiler le projet
```bash
mvnw clean compile
```

### 4. Lancer l'application

**Option 1 - Script Windows (recommandé):**
```bash
launch.bat
```

**Option 2 - Maven directement:**
```bash
mvnw javafx:run
```

**Option 3 - Si vous avez Maven installé:**
```bash
mvn clean javafx:run
```



## Structure du Projet

- `src/main/java/com/project/projetqcm/Main.java` - Point d'entrée
- `src/main/java/controllers/` - Contrôleurs JavaFX
- `src/main/java/models/` - Modèles de données MongoDB
- `src/main/java/database/` - Connexion MongoDB
- `src/main/resources/fxml/` - Interfaces FXML


## Résolution de Problèmes

### Erreur "JavaFX runtime components are missing"

**Causes possibles:**
1. Les dépendances JavaFX ne sont pas téléchargées
2. JavaFX n'est pas sur le module path
3. Version de Java incompatible

**Solutions:**

1. **Télécharger toutes les dépendances:**
   ```bash
   mvnw dependency:resolve
   ```

2. **Nettoyer et recompiler:**
   ```bash
   mvnw clean install
   ```

3. **Vérifier la version de Java:**
   ```bash
   java -version
   ```
   Doit afficher Java 21 ou supérieur

4. **Vérifier que JavaFX est dans le classpath:**
   ```bash
   mvnw dependency:tree | findstr javafx
   ```
   Doit afficher les modules javafx-base, javafx-graphics, javafx-controls, javafx-fxml

5. **Si le problème persiste, lancez avec le script:**
   ```bash
   launch.bat
   ```

### Erreur de connexion MongoDB
- Vérifiez que MongoDB est démarré: `mongod`
- Vérifiez l'URL de connexion dans `MongoConnection.java`
- L'application fonctionne même si MongoDB n'est pas démarré (seulement pour l'interface)
 
 ### Solution 2   si la premier ne passe pas 
 ## Déploiement avec Docker

### 1. Lancer l'application
```bash
docker-compose up --build -d
```

### 2. Accéder à l'interface
L'application est accessible via VNC dans le navigateur :
http://localhost:6080/vnc.html

### 3. Arrêter l'application
```bash
docker-compose down -v
```
