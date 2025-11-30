# 🔧 Configurer JAVA_HOME - Solution Rapide

## ⚠️ Problème
```
Error: JAVA_HOME not found in your environment.
```

## ✅ SOLUTION 1: Configurer JAVA_HOME (Recommandé)

### Étape 1: Trouver où Java est installé

D'après vos messages précédents, vous avez Java ici:
```
C:\Users\hp zbook\.jdks\openjdk-23.0.1
```

### Étape 2: Configurer JAVA_HOME dans Windows

#### Méthode A: Via l'Interface Windows (Permanent)

1. **Ouvrez les Variables d'Environnement:**
   - Appuyez sur `Windows + R`
   - Tapez: `sysdm.cpl`
   - Appuyez sur Entrée
   - Allez dans l'onglet **Avancé**
   - Cliquez sur **Variables d'environnement**

2. **Ajoutez JAVA_HOME:**
   - Dans **Variables système**, cliquez sur **Nouveau**
   - **Nom de la variable:** `JAVA_HOME`
   - **Valeur de la variable:** `C:\Users\hp zbook\.jdks\openjdk-23.0.1`
   - Cliquez **OK**

3. **Vérifiez que Java est dans le PATH:**
   - Trouvez la variable **Path** dans **Variables système**
   - Cliquez sur **Modifier**
   - Vérifiez qu'il y a: `%JAVA_HOME%\bin`
   - Si ce n'est pas là, ajoutez-le
   - Cliquez **OK** partout

4. **Redémarrez IntelliJ** pour que les changements prennent effet

#### Méthode B: Via PowerShell (Temporaire - Pour cette session seulement)

Dans le terminal IntelliJ, tapez:

```powershell
$env:JAVA_HOME = "C:\Users\hp zbook\.jdks\openjdk-23.0.1"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

Puis relancez:
```bash
mvnw.cmd javafx:run
```

---

## ✅ SOLUTION 2: Utiliser Java Directement (Sans Maven Wrapper)

Si JAVA_HOME pose problème, vous pouvez utiliser Java directement:

1. **Vérifiez que Java fonctionne:**
   ```bash
   "C:\Users\hp zbook\.jdks\openjdk-23.0.1\bin\java.exe" -version
   ```

2. **Lancez Maven avec Java explicite:**
   ```bash
   set JAVA_HOME=C:\Users\hp zbook\.jdks\openjdk-23.0.1
   mvnw.cmd javafx:run
   ```

---

## ✅ SOLUTION 3: Utiliser IntelliJ pour Lancer (Le Plus Simple!)

Au lieu d'utiliser le terminal, utilisez IntelliJ directement:

1. **Configurez la Run Configuration:**
   - **Run** → **Edit Configurations...**
   - Sélectionnez "Main" (ou créez-en une)
   - **VM options:** Ajoutez:
     ```
     --add-modules javafx.controls,javafx.fxml
     ```
   - Cliquez **OK**

2. **Lancez avec le bouton Run** (bouton vert)

IntelliJ utilise son propre Java, donc pas besoin de JAVA_HOME!

---

## 🔍 Vérification

Pour vérifier que JAVA_HOME est configuré:

```bash
echo %JAVA_HOME%
```

Vous devriez voir:
```
C:\Users\hp zbook\.jdks\openjdk-23.0.1
```

---

## 💡 Solution la Plus Simple

**Utilisez IntelliJ directement au lieu du terminal!**

1. **Run** → **Edit Configurations...**
2. Ajoutez dans **VM options:** `--add-modules javafx.controls,javafx.fxml`
3. Cliquez sur le bouton **Run** vert

Pas besoin de JAVA_HOME! ✅

