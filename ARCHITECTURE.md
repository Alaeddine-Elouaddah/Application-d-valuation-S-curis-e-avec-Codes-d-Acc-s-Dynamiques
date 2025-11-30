# Analyse de l'Architecture - Système de Gestion d'Examens QCM

## Vue d'ensemble

Ce projet est une application JavaFX pour la gestion d'examens QCM en ligne avec des fonctionnalités de surveillance et de sécurité avancées.

## Architecture Technique

### 1. Stack Technologique

- **Frontend**: JavaFX 21.0.6 (FXML pour les interfaces)
- **Backend**: Java 21
- **Base de données**: MongoDB (mongodb://localhost:27017/QCM)
- **Build Tool**: Maven
- **Architecture**: Modulaire (Java Modules)

### 2. Structure du Projet

```
ProjetQcm/
├── src/main/java/
│   ├── com/project/projetqcm/
│   │   └── Main.java                    # Point d'entrée de l'application
│   ├── controllers/                     # Contrôleurs FXML
│   │   ├── HomeController.java          # Contrôleur de l'interface principale
│   │   ├── CreateExamController.java    # Création d'examen (professeur)
│   │   ├── JoinExamController.java      # Rejoindre un examen (étudiant)
│   │   ├── ExamController.java          # Interface d'examen avec surveillance
│   │   └── ResultProfessorController.java # Affichage des résultats
│   ├── models/                          # Modèles de données
│   │   ├── User.java                    # Utilisateur (Professeur/Étudiant)
│   │   ├── Exam.java                    # Examen avec ID généré automatiquement
│   │   ├── Question.java                # Question QCM (1 ou 2 réponses)
│   │   ├── Choice.java                  # Choix de réponse
│   │   ├── Answer.java                  # Réponse de l'étudiant
│   │   ├── Attempt.java                 # Tentative d'examen
│   │   └── ProctorEvent.java            # Événements de surveillance
│   ├── database/                        # Accès aux données
│   │   ├── MongoConnection.java          # Singleton de connexion MongoDB
│   │   ├── UserRepository.java
│   │   ├── ExamRepository.java
│   │   ├── QuestionRepository.java
│   │   ├── AttemptRepository.java
│   │   └── ProctorRepository.java
│   ├── security/                        # Surveillance et sécurité
│   │   ├── CameraMonitor.java           # Surveillance caméra
│   │   ├── FocusMonitor.java            # Détection perte de focus
│   │   └── ScreenMonitor.java           # Détection partage d'écran
│   └── utils/                           # Utilitaires
│       ├── TimerUtils.java              # Gestion des timers
│       ├── AlertUtils.java              # Alertes de triche
│       └── SceneLoader.java             # Chargement de scènes
└── src/main/resources/
    ├── fxml/                            # Fichiers FXML
    │   ├── home.fxml                    # Interface principale
    │   ├── create_exam.fxml
    │   ├── join_exam.fxml
    │   ├── exam.fxml
    │   └── results_prof.fxml
    └── css/
        └── style.css
```

## 3. Modèles de Données MongoDB

### User
- `_id`: ObjectId (généré automatiquement)
- `name`: String
- `email`: String
- `role`: String ("PROFESSOR" ou "STUDENT")

### Exam
- `_id`: ObjectId (généré automatiquement)
- `examId`: String (ID unique de 6 caractères généré automatiquement pour rejoindre)
- `title`: String
- `description`: String
- `professorId`: ObjectId
- `questionIds`: List<ObjectId>
- `durationMinutes`: Integer (durée spécifiée par le professeur)
- `createdAt`: Date
- `isActive`: Boolean

### Question
- `_id`: ObjectId (généré automatiquement)
- `text`: String
- `maxAnswers`: Integer (1 pour une seule réponse, 2 pour deux réponses)
- `choices`: List<Choice>

### Choice
- `text`: String
- `isCorrect`: Boolean

### Attempt
- `_id`: ObjectId (généré automatiquement)
- `examId`: ObjectId
- `studentId`: ObjectId
- `answers`: List<Answer>
- `score`: Double
- `startTime`: Date
- `endTime`: Date
- `warningCount`: Integer (compteur d'avertissements de triche)

### Answer
- `questionId`: ObjectId
- `selectedChoiceIndices`: List<Integer> (indices des choix sélectionnés)

### ProctorEvent
- `_id`: ObjectId (généré automatiquement)
- `attemptId`: ObjectId
- `eventType`: String ("CAMERA_OFF", "FOCUS_LOST", "SCREEN_SHARE", "WRITING_DETECTED", "LOOKING_AWAY")
- `timestamp`: Date
- `description`: String

## 4. Flux Fonctionnel

### Interface Principale (Home)
1. L'utilisateur arrive sur l'interface principale
2. Deux options disponibles:
   - **Créer un Examen** (Professeur)
   - **Rejoindre un Examen** (Étudiant)

### Création d'Examen (Professeur)
1. Le professeur saisit les informations de l'examen
2. Spécifie la durée en minutes
3. Ajoute des questions QCM (1 ou 2 réponses possibles)
4. Un `examId` unique de 6 caractères est généré automatiquement
5. L'examen est sauvegardé dans MongoDB

### Rejoindre un Examen (Étudiant)
1. L'étudiant saisit l'`examId` fourni par le professeur
2. Vérification de l'existence de l'examen
3. Préparation de l'interface d'examen

### Passage de l'Examen
1. **Compte à rebours**: 5-4-3-2-1 avant le début
2. **Activation de la surveillance**:
   - Caméra activée
   - Mode plein écran (fenêtre non divisible)
   - Détection de perte de focus
   - Détection de partage d'écran
   - Détection d'écriture sur feuille (via caméra)
   - Détection de regard détourné (via caméra)
3. **Timer**: Affichage du temps restant (durée spécifiée par le professeur)
4. **Avertissements**: 
   - Signal affiché après chaque détection de triche
   - Après 3 signaux, l'étudiant est expulsé de l'examen
5. **Soumission**: L'étudiant soumet ses réponses
6. **Calcul du score**: Automatique basé sur les réponses correctes

### Affichage des Résultats
- **Pour le Professeur**: 
  - Liste de tous les étudiants avec leurs notes
  - Détails des tentatives
  - Événements de surveillance (ProctorEvent)
- **Pour l'Étudiant**: 
  - Score final après soumission

## 5. Fonctionnalités de Sécurité

### Surveillance en Temps Réel
1. **Caméra**: 
   - Activée pendant tout l'examen
   - Détection d'écriture sur feuille
   - Détection de regard détourné
   - Détection de présence d'autres personnes

2. **Focus de la Fenêtre**:
   - Détection de perte de focus (changement d'application)
   - Mode plein écran obligatoire
   - Fenêtre non divisible

3. **Partage d'Écran**:
   - Détection de partage d'écran
   - Détection d'utilisation de deux écrans

4. **Système d'Avertissements**:
   - 1er signal: Avertissement
   - 2ème signal: Avertissement sévère
   - 3ème signal: Expulsion automatique

## 6. Connexion MongoDB

- **URL**: `mongodb://localhost:27017/QCM`
- **Pattern**: Singleton (MongoConnection.getInstance())
- **Collections**:
  - `users`
  - `exams`
  - `questions`
  - `attempts`
  - `proctorEvents`

## 7. Points d'Attention

1. **Configuration Java**: JAVA_HOME doit être configuré
2. **MongoDB**: Doit être démarré sur localhost:27017
3. **Modules Java**: Les modules MongoDB doivent être correctement déclarés dans `module-info.java`
4. **Caméra**: Nécessite des permissions système
5. **Détection IA**: Les fonctionnalités de détection avancées nécessiteront des bibliothèques supplémentaires (OpenCV, TensorFlow, etc.)

## 8. Prochaines Étapes

1. Implémenter les contrôleurs CreateExamController et JoinExamController
2. Implémenter ExamController avec toutes les fonctionnalités de surveillance
3. Implémenter les repositories MongoDB
4. Intégrer les bibliothèques de détection (caméra, focus, écran)
5. Implémenter le système de scoring
6. Créer l'interface de résultats pour le professeur
7. Tests et validation

