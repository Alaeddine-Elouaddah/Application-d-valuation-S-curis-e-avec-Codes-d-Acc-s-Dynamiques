# 🔧 FIX: Erreur JavaFX dans IntelliJ IDEA

## ⚠️ Le Problème

IntelliJ lance Java avec le **classpath** mais JavaFX nécessite le **module path** avec `--module-path` ET `--add-modules`.

## ✅ SOLUTION 1: Utiliser Maven (100% Fiable)

**C'est la méthode la plus simple et qui fonctionne toujours!**

1. Dans IntelliJ, ouvrez le terminal: `Alt + F12`
2. Tapez:
   ```bash
   mvnw.cmd javafx:run
   ```

✅ **C'est tout!** Ça fonctionne à tous les coups.

---

## ✅ SOLUTION 2: Configurer IntelliJ Correctement

### Étape 1: Modifier la Configuration

1. **Run** → **Edit Configurations...**
2. Sélectionnez "Main" (ou créez-en une nouvelle)
3. Dans **VM options**, copiez-collez **EXACTEMENT** ceci:

```
--module-path "$USER_HOME$/.m2/repository/org/openjfx/javafx-base/21.0.6/javafx-base-21.0.6.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-base/21.0.6/javafx-base-21.0.6-win.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-graphics/21.0.6/javafx-graphics-21.0.6.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-graphics/21.0.6/javafx-graphics-21.0.6-win.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-controls/21.0.6/javafx-controls-21.0.6.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-controls/21.0.6/javafx-controls-21.0.6-win.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-fxml/21.0.6/javafx-fxml-21.0.6.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-fxml/21.0.6/javafx-fxml-21.0.6-win.jar" --add-modules javafx.controls,javafx.fxml
```

4. **Main class:** `com.project.projetqcm.Main`
5. **Use classpath of module:** `ProjetQcm` (doit être coché)
6. Cliquez **OK**

### Étape 2: Lancer

Cliquez sur le bouton **Run** (ou `Shift + F10`)

---

## ✅ SOLUTION 3: Script de Lancement Automatique

J'ai créé un fichier de configuration dans `.idea/runConfigurations/Main.xml`.

**Pour l'utiliser:**

1. Fermez IntelliJ complètement
2. Rouvrez IntelliJ
3. La configuration "Main" devrait apparaître dans la liste des configurations
4. Sélectionnez-la et lancez

---

## 🔍 Vérification

Si ça fonctionne, vous verrez:
- ✅ L'application JavaFX s'ouvre
- ✅ Interface avec les boutons "Créer un Examen" et "Rejoindre un Examen"
- ✅ Aucune erreur dans la console

---

## ❓ Si ça ne fonctionne toujours pas

### Vérifiez les dépendances:

1. **File** → **Project Structure** (`Ctrl + Alt + Shift + S`)
2. **Libraries** → Vérifiez que vous voyez:
   - `javafx-base-21.0.6`
   - `javafx-graphics-21.0.6`
   - `javafx-controls-21.0.6`
   - `javafx-fxml-21.0.6`

### Reimportez Maven:

1. Clic droit sur `pom.xml`
2. **Maven** → **Reload Project**

### Nettoyez le cache:

1. **File** → **Invalidate Caches / Restart**
2. **Invalidate and Restart**

---

## 💡 Pourquoi cette erreur?

JavaFX est un **module** Java (depuis Java 9+). Il faut:
1. **Module path** (`--module-path`) : où trouver les modules JavaFX
2. **Add modules** (`--add-modules`) : quels modules charger

IntelliJ utilise seulement le classpath par défaut, donc il faut ajouter ces arguments manuellement.

**C'est pourquoi `mvnw.cmd javafx:run` fonctionne toujours** - le plugin Maven fait tout automatiquement!

