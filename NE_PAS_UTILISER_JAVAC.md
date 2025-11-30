# ⚠️ NE PAS UTILISER `javac` DIRECTEMENT!

## ❌ Pourquoi ça ne fonctionne pas?

Quand vous utilisez `javac Main.java` directement, le compilateur Java ne sait pas où trouver les bibliothèques JavaFX (javafx.application, javafx.fxml, etc.) car elles ne sont pas dans le classpath.

## ✅ SOLUTION: Utiliser Maven

Maven gère automatiquement:
- ✅ Le téléchargement des dépendances
- ✅ Le classpath avec toutes les bibliothèques
- ✅ La compilation
- ✅ Le lancement avec JavaFX

---

## 🚀 Comment compiler et lancer CORRECTEMENT

### Méthode 1: Compiler et lancer en une commande (RECOMMANDÉ)

Dans le terminal IntelliJ (`Alt + F12`) ou PowerShell:

```bash
mvnw.cmd javafx:run
```

Cette commande:
1. Télécharge les dépendances si nécessaire
2. Compile le projet
3. Lance l'application avec JavaFX correctement configuré

---

### Méthode 2: Compiler puis lancer séparément

**Étape 1: Compiler**
```bash
mvnw.cmd clean compile
```

**Étape 2: Lancer**
```bash
mvnw.cmd javafx:run
```

---

### Méthode 3: Créer un JAR exécutable

**Étape 1: Créer le JAR**
```bash
mvnw.cmd clean package
```

**Étape 2: Lancer le JAR**
```bash
java --module-path "%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\21.0.6" --add-modules javafx.controls,javafx.fxml -jar target/ProjetQcm-1.0-SNAPSHOT.jar
```

(Mais c'est compliqué, utilisez plutôt la méthode 1!)

---

## 📝 Commandes Maven utiles

```bash
# Télécharger les dépendances
mvnw.cmd dependency:resolve

# Compiler
mvnw.cmd compile

# Nettoyer et compiler
mvnw.cmd clean compile

# Compiler et lancer
mvnw.cmd javafx:run

# Créer un JAR
mvnw.cmd package
```

---

## 💡 Pourquoi Maven?

Maven lit le fichier `pom.xml` qui contient toutes les dépendances (JavaFX, MongoDB, etc.) et:
- Les télécharge automatiquement dans `~/.m2/repository/`
- Les ajoute au classpath lors de la compilation
- Configure JavaFX correctement pour le lancement

Quand vous utilisez `javac` directement, vous devez manuellement:
- Trouver tous les JARs JavaFX
- Les ajouter au classpath avec `-cp` ou `-classpath`
- Configurer le module path
- C'est très compliqué et source d'erreurs!

**C'est pourquoi on utilise Maven!** 🎯

---

## ✅ Résumé

❌ **NE FAITES PAS:**
```bash
javac Main.java
java Main
```

✅ **FAITES CECI:**
```bash
mvnw.cmd javafx:run
```

C'est tout! Maven fait le reste automatiquement. 🚀

