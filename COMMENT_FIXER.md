# 🚨 COMMENT FIXER L'ERREUR JavaFX

## ⚡ SOLUTION RAPIDE (2 minutes)

### Option 1: Utiliser Maven (RECOMMANDÉ - Fonctionne toujours)

1. Dans IntelliJ, appuyez sur `Alt + F12` pour ouvrir le terminal
2. Tapez cette commande:
   ```bash
   mvnw.cmd javafx:run
   ```
3. Appuyez sur Entrée

✅ **C'est tout!** L'application va se lancer.

---

### Option 2: Configurer IntelliJ (Si vous voulez utiliser le bouton Run)

#### Étape 1: Ouvrir les configurations
- Cliquez sur **Run** (en haut à droite)
- Puis **Edit Configurations...**

#### Étape 2: Modifier la configuration
1. Sélectionnez "Main" dans la liste (ou créez-en une avec le bouton **+**)
2. Dans **VM options**, supprimez tout ce qui est là
3. Copiez-collez **EXACTEMENT** ce texte:

```
--module-path "$USER_HOME$/.m2/repository/org/openjfx/javafx-base/21.0.6/javafx-base-21.0.6.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-base/21.0.6/javafx-base-21.0.6-win.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-graphics/21.0.6/javafx-graphics-21.0.6.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-graphics/21.0.6/javafx-graphics-21.0.6-win.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-controls/21.0.6/javafx-controls-21.0.6.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-controls/21.0.6/javafx-controls-21.0.6-win.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-fxml/21.0.6/javafx-fxml-21.0.6.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-fxml/21.0.6/javafx-fxml-21.0.6-win.jar" --add-modules javafx.controls,javafx.fxml
```

4. Vérifiez que:
   - **Main class:** `com.project.projetqcm.Main`
   - **Use classpath of module:** `ProjetQcm` est coché ✅

5. Cliquez **OK**

#### Étape 3: Lancer
- Cliquez sur le bouton **Run** vert (ou `Shift + F10`)

---

## ✅ Vérification

Si tout fonctionne:
- ✅ Une fenêtre JavaFX s'ouvre
- ✅ Vous voyez "Système de Gestion d'Examens QCM"
- ✅ Deux boutons: "Créer un Examen" et "Rejoindre un Examen"
- ✅ Aucune erreur rouge dans la console

---

## ❓ Pourquoi cette erreur?

JavaFX est un **module Java** (pas juste une bibliothèque). Il faut:
1. Le **module path** (`--module-path`) : où trouver JavaFX
2. Les **modules à charger** (`--add-modules`) : quels modules utiliser

IntelliJ utilise seulement le classpath par défaut, donc il faut ajouter ces arguments.

**C'est pourquoi `mvnw.cmd javafx:run` fonctionne toujours** - Maven fait tout automatiquement!

---

## 🔧 Si ça ne fonctionne toujours pas

### Vérifiez que les dépendances sont téléchargées:

1. Ouvrez le terminal IntelliJ (`Alt + F12`)
2. Tapez:
   ```bash
   mvnw.cmd dependency:resolve
   ```
3. Attendez que ça finisse

### Reimportez le projet Maven:

1. Clic droit sur `pom.xml`
2. **Maven** → **Reload Project**

### Nettoyez et recompilez:

Dans le terminal:
```bash
mvnw.cmd clean compile
```

---

## 💡 Conseil

**Utilisez toujours `mvnw.cmd javafx:run`** - c'est la méthode la plus fiable et qui fonctionne à 100%!

