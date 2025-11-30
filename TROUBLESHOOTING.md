# Résolution de l'erreur "JavaFX runtime components are missing"

## Pourquoi cette erreur se produit ?

Cette erreur se produit généralement pour **3 raisons principales**:

### 1. **Dépendances non téléchargées** (le plus fréquent)
Les JARs JavaFX ne sont pas dans le répertoire `~/.m2/repository/org/openjfx/`

### 2. **Module path non configuré**
JavaFX nécessite que les modules soient ajoutés au module path avec `--add-modules`

### 3. **Version de Java incompatible**
JavaFX 21 nécessite Java 21 ou supérieur

## Solution Étape par Étape

### Étape 1: Vérifier Java
```bash
java -version
```
Doit afficher: `openjdk version "21"` ou supérieur

### Étape 2: Télécharger les dépendances
```bash
mvnw.cmd dependency:resolve
```
Cette commande télécharge tous les JARs nécessaires dans `~/.m2/repository/`

### Étape 3: Nettoyer et compiler
```bash
mvnw.cmd clean compile
```

### Étape 4: Lancer avec le plugin JavaFX
```bash
mvnw.cmd javafx:run
```

Le plugin JavaFX Maven configure automatiquement:
- Le module path
- Les arguments `--add-modules javafx.controls,javafx.fxml`
- Le classpath avec tous les JARs JavaFX

## Si ça ne fonctionne toujours pas

### Vérifier que les dépendances sont bien téléchargées:
```bash
mvnw.cmd dependency:tree | findstr javafx
```

Vous devriez voir:
```
[INFO] +- org.openjfx:javafx-base:jar:21.0.6:compile
[INFO] +- org.openjfx:javafx-graphics:jar:21.0.6:compile
[INFO] +- org.openjfx:javafx-controls:jar:21.0.6:compile
[INFO] +- org.openjfx:javafx-fxml:jar:21.0.6:compile
```

### Forcer le téléchargement:
```bash
mvnw.cmd clean install -U
```
L'option `-U` force la mise à jour des dépendances

### Vérifier le répertoire Maven local:
Les JARs doivent être dans:
```
C:\Users\VotreNom\.m2\repository\org\openjfx\
```

Si ce dossier est vide ou incomplet, supprimez-le et relancez:
```bash
rmdir /s C:\Users\VotreNom\.m2\repository\org\openjfx
mvnw.cmd dependency:resolve
```

## Alternative: Lancer directement avec Java

Si le plugin Maven ne fonctionne pas, vous pouvez lancer directement:

```bash
mvnw.cmd clean package
java --module-path "%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\21.0.6" --add-modules javafx.controls,javafx.fxml -cp "target/classes;target/dependency/*" com.project.projetqcm.Main
```

Mais c'est plus complexe. Le plugin Maven devrait fonctionner normalement.

## Vérification finale

Si tout est correct, quand vous lancez `mvnw.cmd javafx:run`, vous devriez voir:
1. La compilation réussie
2. L'application JavaFX qui s'ouvre avec l'interface principale

Si vous voyez toujours l'erreur, vérifiez:
- ✅ Java 21+ installé
- ✅ Dépendances téléchargées (`mvnw.cmd dependency:tree`)
- ✅ Compilation réussie (`mvnw.cmd clean compile`)
- ✅ Plugin JavaFX configuré dans `pom.xml`

