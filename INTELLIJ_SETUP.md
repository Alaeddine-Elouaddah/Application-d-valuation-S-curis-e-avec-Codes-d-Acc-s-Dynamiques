# Configuration IntelliJ IDEA pour JavaFX

## ⚠️ Problème
IntelliJ IDEA lance l'application sans les arguments de module JavaFX nécessaires (`--add-modules`).

## ✅ Solution 1: Utiliser Maven (LE PLUS SIMPLE - RECOMMANDÉ)

**C'est la méthode la plus fiable!**

1. Dans IntelliJ, ouvrez le terminal intégré (Alt+F12 ou View → Tool Windows → Terminal)
2. Exécutez:
   ```bash
   mvnw.cmd javafx:run
   ```

Le plugin Maven configure automatiquement tout ce qui est nécessaire. ✅

## ✅ Solution 2: Configuration de lancement IntelliJ

### Étape 1: Créer/modifier la configuration
1. Dans IntelliJ, cliquez sur **Run** → **Edit Configurations...** (ou cliquez sur la configuration actuelle)
2. Si la configuration "Main" existe déjà, sélectionnez-la, sinon cliquez **+** → **Application**
3. Configurez:

**Name:** `Main`

**Main class:** `com.project.projetqcm.Main`

**VM options:** (IMPORTANT - Copiez-collez exactement):
```
--add-modules javafx.controls,javafx.fxml
```

**Use classpath of module:** `ProjetQcm` (doit être coché)

### Étape 2: Vérifier les dépendances
1. **File** → **Project Structure** (Ctrl+Alt+Shift+S)
2. **Libraries** → Vérifiez que les modules JavaFX sont présents
3. Si absents: **File** → **Invalidate Caches / Restart** → **Invalidate and Restart**

### Étape 3: Lancer
Cliquez sur **Run** (Shift+F10)

## Solution 2: Utiliser Maven (Plus simple)

Au lieu de lancer directement depuis IntelliJ, utilisez le terminal intégré:

1. Ouvrez le terminal dans IntelliJ (Alt+F12)
2. Exécutez:
   ```bash
   mvnw.cmd javafx:run
   ```

Cette méthode utilise le plugin Maven qui configure automatiquement tout.

## Solution 3: Script de lancement

Utilisez le script `launch.bat` depuis le terminal IntelliJ:
```bash
launch.bat
```

## Vérification

Si tout fonctionne, vous devriez voir:
- ✅ L'application JavaFX s'ouvre
- ✅ L'interface principale avec les boutons "Créer un Examen" et "Rejoindre un Examen"
- ✅ Aucune erreur dans la console

## Si ça ne fonctionne toujours pas

1. **Vérifiez que les dépendances sont téléchargées:**
   - Allez dans **File** → **Project Structure** → **Libraries**
   - Vous devriez voir les modules JavaFX

2. **Reimportez le projet Maven:**
   - Clic droit sur `pom.xml` → **Maven** → **Reload Project**

3. **Nettoyez et recompilez:**
   - **Build** → **Rebuild Project**

