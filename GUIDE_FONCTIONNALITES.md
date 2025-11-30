# 📚 Guide des Fonctionnalités Implémentées

## ✅ Fonctionnalités Disponibles

### 1. Créer un Examen

**Accès:** Bouton "Créer un Examen" sur la page d'accueil

**Fonctionnalités:**
- ✅ Saisie du titre de l'examen (obligatoire)
- ✅ Description de l'examen (optionnel)
- ✅ Durée en minutes (obligatoire)
- ✅ Ajout de questions multiples
- ✅ Pour chaque question:
  - Texte de la question
  - Nombre de réponses possibles (1 ou 2)
  - Ajout de choix multiples
  - Marquage des bonnes réponses
- ✅ Génération automatique d'un code d'examen unique (6 caractères)
- ✅ Sauvegarde dans MongoDB

**Comment utiliser:**
1. Cliquez sur "Créer un Examen"
2. Remplissez le titre et la durée
3. Ajoutez des questions avec le bouton "+ Ajouter une Question"
4. Pour chaque question:
   - Entrez le texte
   - Choisissez le nombre de réponses (1 ou 2)
   - Ajoutez des choix avec "+ Ajouter un choix"
   - Cochez les bonnes réponses
5. Cliquez sur "Créer l'Examen"
6. **IMPORTANT:** Notez le code d'examen généré et partagez-le avec vos étudiants!

---

### 2. Rejoindre un Examen

**Accès:** Bouton "Rejoindre un Examen" sur la page d'accueil

**Fonctionnalités:**
- ✅ Saisie du code d'examen (6 caractères)
- ✅ Saisie du nom de l'étudiant
- ✅ Validation du code d'examen
- ✅ Vérification que l'examen existe et est actif
- ✅ Affichage des informations de l'examen avant de commencer

**Comment utiliser:**
1. Cliquez sur "Rejoindre un Examen"
2. Entrez le code d'examen fourni par le professeur
3. Entrez votre nom
4. Cliquez sur "Rejoindre l'Examen"
5. Vérifiez les informations de l'examen
6. Confirmez pour commencer (interface d'examen à venir)

---

## 🗄️ Base de Données MongoDB

**Connexion:** `mongodb://localhost:27017/QCM`

**Collections:**
- `exams` - Stocke les examens créés
- `questions` - Stocke les questions
- `users` - Stocke les utilisateurs (professeurs/étudiants)
- `attempts` - Stocke les tentatives d'examen (à venir)
- `proctorEvents` - Stocke les événements de surveillance (à venir)

---

## 📝 Structure des Données

### Exam
- `_id`: ObjectId (généré automatiquement)
- `examId`: String (code unique de 6 caractères, généré automatiquement)
- `title`: String
- `description`: String
- `professorId`: ObjectId
- `questionIds`: List<ObjectId>
- `durationMinutes`: Integer
- `createdAt`: Date
- `isActive`: Boolean

### Question
- `_id`: ObjectId (généré automatiquement)
- `text`: String
- `maxAnswers`: Integer (1 ou 2)
- `choices`: List<Choice>

### Choice
- `text`: String
- `isCorrect`: Boolean

---

## 🔧 Prérequis

1. **MongoDB doit être démarré:**
   ```bash
   mongod
   ```
   Ou utilisez MongoDB comme service Windows

2. **L'application doit être lancée:**
   ```bash
   mvnw.cmd javafx:run
   ```

---

## 🚀 Prochaines Étapes (À Implémenter)

- [ ] Interface de passage d'examen avec timer
- [ ] Système de surveillance (caméra, focus, écran)
- [ ] Compte à rebours avant le début (5-4-3-2-1)
- [ ] Détection de triche (3 signaux = expulsion)
- [ ] Calcul automatique des scores
- [ ] Interface de résultats pour le professeur
- [ ] Gestion des utilisateurs (connexion/profils)

---

## 💡 Notes Importantes

1. **Code d'examen:** Généré automatiquement, unique, 6 caractères alphanumériques
2. **Validation:** Tous les champs obligatoires sont validés avant la sauvegarde
3. **Questions:** Minimum 2 choix par question, au moins 1 question par examen
4. **Durée:** Doit être un nombre positif en minutes

---

## ❓ Dépannage

### "Erreur de connexion MongoDB"
→ Vérifiez que MongoDB est démarré sur `localhost:27017`

### "Code d'examen invalide"
→ Vérifiez que le code contient exactement 6 caractères et qu'il existe dans la base

### L'application ne se lance pas
→ Voir `COMMENT_FIXER.md` et `FIX_JAVA_HOME.md`

