# 🔧 Dépannage - Création d'Examen

## ⚠️ Erreurs Courantes et Solutions

### 1. Erreur "MongoDB connection failed"

**Symptôme:**
- Erreur lors de la création d'examen
- Message: "Impossible de se connecter à MongoDB"

**Solution:**
1. Vérifiez que MongoDB est démarré:
   ```bash
   mongod
   ```
2. Vérifiez que MongoDB écoute sur `localhost:27017`
3. Redémarrez l'application après avoir démarré MongoDB

---

### 2. Erreur "GlassViewEventHandler" (Événements JavaFX)

**Symptôme:**
- Stack trace avec `GlassViewEventHandler`
- Erreur lors du clic sur un bouton

**Solution:**
- J'ai ajouté des vérifications de sécurité dans le code
- Redémarrez l'application
- Si le problème persiste, vérifiez que vous utilisez JavaFX 21.0.6

---

### 3. Erreur "NullPointerException"

**Symptôme:**
- Erreur lors de la suppression d'une question
- Erreur lors de la mise à jour des numéros

**Solution:**
- Le code a été corrigé avec des vérifications de nullité
- Redémarrez l'application

---

### 4. Code d'examen non généré

**Symptôme:**
- L'examen est créé mais le code n'apparaît pas

**Solution:**
1. Vérifiez la console pour les erreurs
2. Le code est généré automatiquement et affiché dans une boîte de dialogue
3. Si la boîte de dialogue n'apparaît pas, vérifiez le label de statut en bas

---

## ✅ Vérifications Avant de Créer un Examen

1. **MongoDB est démarré:**
   ```bash
   mongod
   ```

2. **Tous les champs obligatoires sont remplis:**
   - ✅ Titre de l'examen
   - ✅ Durée (nombre positif)
   - ✅ Au moins une question
   - ✅ Chaque question a au moins 2 choix

3. **Au moins une bonne réponse par question:**
   - ✅ Au moins un choix doit être coché "Correct"

---

## 🔍 Comment Vérifier que Tout Fonctionne

1. **Testez la connexion MongoDB:**
   - Créez un examen simple avec 1 question
   - Si ça fonctionne, MongoDB est OK

2. **Vérifiez le code généré:**
   - Une boîte de dialogue doit apparaître avec le code
   - Le code doit être de 6 caractères (ex: ABC123)

3. **Testez de rejoindre l'examen:**
   - Utilisez le code généré dans "Rejoindre un Examen"
   - L'examen doit être trouvé

---

## 💡 Conseils

- **Sauvegardez toujours le code d'examen** avant de fermer la boîte de dialogue
- **Vérifiez MongoDB** avant de créer un examen
- **Testez avec un examen simple** d'abord (1-2 questions)
- **Vérifiez les logs** dans la console pour plus de détails

---

## 📞 Si le Problème Persiste

1. Vérifiez les logs dans la console IntelliJ
2. Vérifiez que MongoDB est bien démarré
3. Redémarrez l'application
4. Vérifiez que toutes les dépendances sont téléchargées:
   ```bash
   mvnw.cmd dependency:resolve
   ```

