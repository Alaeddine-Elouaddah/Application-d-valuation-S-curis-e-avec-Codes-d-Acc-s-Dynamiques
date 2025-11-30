# 📖 Guide de Compilation et Lancement

## ⚠️ IMPORTANT: Ne jamais utiliser `javac` directement!

Si vous voyez des erreurs comme:
```
error: package javafx.application does not exist
error: package javafx.fxml does not exist
```

C'est parce que vous utilisez `javac` directement au lieu de Maven!

---

## ✅ LA BONNE MÉTHODE

### Depuis IntelliJ IDEA:

1. **Ouvrez le terminal intégré:**
   - `Alt + F12`
   - Ou: View → Tool Windows → Terminal

2. **Lancez avec Maven:**
   ```bash
   mvnw.cmd javafx:run
   ```

✅ **C'est tout!** Maven:
- Compile automatiquement
- Configure JavaFX
- Lance l'application

---

## 🔧 Commandes Maven Essentielles

### Compiler seulement:
```bash
mvnw.cmd compile
```

### Nettoyer et compiler:
```bash
mvnw.cmd clean compile
```

### Compiler et lancer:
```bash
mvnw.cmd javafx:run
```

### Télécharger les dépendances:
```bash
mvnw.cmd dependency:resolve
```

### Tout nettoyer et recompiler:
```bash
mvnw.cmd clean install
```

---

## 📁 Où sont les fichiers compilés?

Après compilation avec Maven, les fichiers `.class` sont dans:
```
target/classes/
```

**Mais vous n'avez pas besoin d'y toucher!** Maven gère tout.

---

## ❓ Pourquoi Maven?

### Avec `javac` (❌ Compliqué):
```bash
javac -cp "C:\Users\...\javafx-base.jar;C:\Users\...\javafx-controls.jar;..." Main.java
java -cp "..." --module-path "..." --add-modules ... Main
```
- Vous devez trouver tous les JARs
- Vous devez les ajouter au classpath
- Vous devez configurer le module path
- Très long et source d'erreurs!

### Avec Maven (✅ Simple):
```bash
mvnw.cmd javafx:run
```
- Maven lit `pom.xml`
- Télécharge les dépendances automatiquement
- Configure tout correctement
- Une seule commande!

---

## 🎯 Résumé

**Pour compiler et lancer votre projet:**

```bash
mvnw.cmd javafx:run
```

**C'est la seule commande dont vous avez besoin!** 🚀

