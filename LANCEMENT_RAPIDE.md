# 🚀 Lancement Rapide - ProjetQcm

## ⚡ Méthode la plus simple (Recommandée)

### Depuis IntelliJ IDEA:

1. **Ouvrez le terminal intégré:**
   - `Alt + F12` 
   - Ou: View → Tool Windows → Terminal

2. **Lancez avec Maven:**
   ```bash
   mvnw.cmd javafx:run
   ```

✅ **C'est tout!** Le plugin Maven configure automatiquement JavaFX.

---

## 🔧 Si vous voulez lancer depuis le bouton Run d'IntelliJ:

### Configuration nécessaire:

1. **Run** → **Edit Configurations...**
2. Sélectionnez ou créez la configuration "Main"
3. **VM options:** Ajoutez:
   ```
   --add-modules javafx.controls,javafx.fxml
   ```
4. **Use classpath of module:** `ProjetQcm` (doit être coché)

### Pourquoi cette erreur?

IntelliJ lance Java avec le **classpath** mais JavaFX nécessite le **module path** avec `--add-modules`.

Le plugin Maven JavaFX fait cela automatiquement, c'est pourquoi `mvnw.cmd javafx:run` fonctionne toujours.

---

## 📝 Commandes utiles

```bash
# Compiler
mvnw.cmd clean compile

# Lancer
mvnw.cmd javafx:run

# Télécharger dépendances
mvnw.cmd dependency:resolve

# Nettoyer tout
mvnw.cmd clean install
```

---

## ❓ Problèmes fréquents

### "JavaFX runtime components are missing"
→ Utilisez `mvnw.cmd javafx:run` au lieu du bouton Run d'IntelliJ

### "Cannot find module javafx.controls"
→ Ajoutez `--add-modules javafx.controls,javafx.fxml` dans VM options

### Dépendances manquantes
→ `mvnw.cmd dependency:resolve`

---

## ✅ Vérification

Si tout fonctionne, vous devriez voir:
- ✅ L'application JavaFX s'ouvre
- ✅ Interface avec "Créer un Examen" et "Rejoindre un Examen"
- ✅ Aucune erreur dans la console

