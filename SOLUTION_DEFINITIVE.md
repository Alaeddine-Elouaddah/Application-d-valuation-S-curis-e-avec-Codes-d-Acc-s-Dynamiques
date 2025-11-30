# 🎯 SOLUTION DÉFINITIVE - Comment Lancer le Projet

## ⚠️ PROBLÈME ACTUEL

Vous utilisez:
```bash
cd src/main/java/com/project/projetqcm
javac Main.java
```

**Ça ne fonctionne JAMAIS!** ❌

---

## ✅ SOLUTION - 3 MÉTHODES

### 🚀 MÉTHODE 1: Script Automatique (LE PLUS SIMPLE)

1. **Double-cliquez sur:** `LANCER_APPLICATION.bat`
2. **Attendez** - Le script fait tout automatiquement!
3. **L'application s'ouvre!**

C'est tout! ✅

---

### 🚀 MÉTHODE 2: Terminal IntelliJ (RECOMMANDÉ)

1. **Dans IntelliJ, ouvrez le terminal:**
   - `Alt + F12`
   - OU: View → Tool Windows → Terminal

2. **Vérifiez que vous êtes à la racine:**
   ```bash
   dir
   ```
   Vous devez voir `pom.xml` dans la liste!

3. **Si vous êtes dans le mauvais répertoire:**
   ```bash
   cd C:\Users\hp zbook\Downloads\ProjetQcm\ProjetQcm
   ```

4. **Tapez cette commande:**
   ```bash
   mvnw.cmd javafx:run
   ```

5. **Appuyez sur Entrée**

6. **Attendez** - La première fois, Maven télécharge les dépendances (1-2 minutes)

7. **L'application s'ouvre!** ✅

---

### 🚀 MÉTHODE 3: Depuis PowerShell/CMD

1. **Ouvrez PowerShell ou CMD**

2. **Allez à la racine du projet:**
   ```bash
   cd "C:\Users\hp zbook\Downloads\ProjetQcm\ProjetQcm"
   ```

3. **Vérifiez que pom.xml existe:**
   ```bash
   dir pom.xml
   ```
   (Doit afficher le fichier)

4. **Lancez:**
   ```bash
   .\mvnw.cmd javafx:run
   ```

5. **Attendez et l'application s'ouvre!** ✅

---

## ❌ CE QU'IL NE FAUT JAMAIS FAIRE

```bash
❌ cd src/main/java/com/project/projetqcm
❌ javac Main.java
❌ java Main
```

**Pourquoi ça ne fonctionne pas?**
- `javac` ne sait pas où trouver JavaFX
- JavaFX est dans `~/.m2/repository/org/openjfx/`
- Vous devriez ajouter tous les JARs manuellement au classpath
- C'est très compliqué et source d'erreurs!

---

## ✅ CE QU'IL FAUT FAIRE

```bash
✅ Rester à la racine (où se trouve pom.xml)
✅ mvnw.cmd javafx:run
```

**Pourquoi ça fonctionne?**
- Maven lit `pom.xml`
- Trouve automatiquement tous les JARs JavaFX
- Les ajoute au classpath
- Configure JavaFX correctement
- Compile et lance tout!

---

## 📋 RÉCAPITULATIF VISUEL

```
❌ MAUVAIS CHEMIN:
C:\...\ProjetQcm\src\main\java\com\project\projetqcm>
   javac Main.java  ← 15 erreurs!

✅ BON CHEMIN:
C:\...\ProjetQcm>
   mvnw.cmd javafx:run  ← Ça fonctionne!
```

---

## 🔍 VÉRIFICATION

**Comment savoir si vous êtes au bon endroit?**

Dans le terminal, tapez:
```bash
dir pom.xml
```

Si vous voyez:
```
pom.xml
```
✅ **Vous êtes au bon endroit!**

Si vous voyez:
```
Le fichier est introuvable
```
❌ **Vous êtes dans le mauvais répertoire!**

**Solution:**
```bash
cd C:\Users\hp zbook\Downloads\ProjetQcm\ProjetQcm
```

---

## 💡 CONSEIL FINAL

**Utilisez toujours Maven, jamais javac directement!**

La commande magique:
```bash
mvnw.cmd javafx:run
```

C'est la seule commande dont vous avez besoin! 🎯

