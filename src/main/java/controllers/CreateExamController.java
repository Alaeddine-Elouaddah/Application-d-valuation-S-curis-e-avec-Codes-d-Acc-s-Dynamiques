package controllers;

import database.ExamRepository;
import database.QuestionRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import database.MongoConnection;
import com.project.models.MediaType;
import com.project.models.Exam;
import com.project.models.Question;
import com.project.models.Choice;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import java.io.File;
import javafx.scene.media.MediaView;
import java.io.File;
import java.util.prefs.Preferences;

public class CreateExamController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField durationField;
    @FXML
    private VBox questionsContainer;
    @FXML
    private Button addQuestionButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button createButton;
    @FXML
    private Button lastExamButton;
    @FXML
    private Label statusLabel;

    private List<QuestionPanel> questionPanels = new ArrayList<>();
    private ExamRepository examRepository;
    private QuestionRepository questionRepository;

    // ID du professeur (pour l'instant, on utilise un ID par défaut)
    private ObjectId professorId = new ObjectId();

    @FXML
    private void initialize() {
        statusLabel.setText("");
        // Initialize repositories
        examRepository = new ExamRepository();
        questionRepository = new QuestionRepository();
        // Ajouter une première question par défaut
        handleAddQuestion();

        // Initialize last-exam button and load saved last exam (if any)
        loadLastExamFromPrefs();
        if (lastExamButton != null) {
            lastExamButton.setDisable(lastExamInfo == null);
            lastExamButton.setOnAction(e -> showLastExamDialog());
        }
    }

    @FXML
    private void handleLastExamClick() {
        try {
            System.out.println("[CreateExamController] lastExamButton clicked. lastExamInfo=" + (lastExamInfo == null ? "null" : "present"));
        } catch (Exception ignored) {}
        showLastExamDialog();
    }

    @FXML
    private void handleAddQuestion() {
        QuestionPanel panel = new QuestionPanel(questionsContainer.getChildren().size() + 1);
        questionPanels.add(panel);
        questionsContainer.getChildren().add(panel.getContainer());
    }

    @FXML
    private void handleCreateExam() {
        // Validation
        if (titleField.getText().trim().isEmpty()) {
            showError("Le titre de l'examen est requis!");
            return;
        }

        if (durationField.getText().trim().isEmpty()) {
            showError("La durée est requise!");
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationField.getText().trim());
            if (duration <= 0) {
                showError("La durée doit être supérieure à 0!");
                return;
            }
        } catch (NumberFormatException e) {
            showError("La durée doit être un nombre valide!");
            return;
        }

        // Valider les questions
        List<Question> questions = new ArrayList<>();
        for (QuestionPanel panel : questionPanels) {
            Question q = panel.getQuestion();
            if (q != null) {
                if (q.getText().trim().isEmpty()) {
                    showError("Toutes les questions doivent avoir un texte!");
                    return;
                }
                if (q.getChoices().size() < 2) {
                    showError("Chaque question doit avoir au moins 2 choix!");
                    return;
                }
                questions.add(q);
            }
        }

        if (questions.isEmpty()) {
            showError("Vous devez ajouter au moins une question!");
            return;
        }

        try {
            // Vérifier la connexion MongoDB
            try {
                MongoConnection.getInstance().getDatabase().listCollectionNames().first();
            } catch (Exception dbEx) {
                Alert dbErrorAlert = new Alert(Alert.AlertType.ERROR);
                dbErrorAlert.setTitle("Erreur de Connexion");
                dbErrorAlert.setHeaderText("Impossible de se connecter à MongoDB");
                dbErrorAlert.setContentText("Veuillez démarrer MongoDB avant de créer un examen.\n\n" +
                        "Commande: mongod");
                dbErrorAlert.showAndWait();
                return;
            }

            // Créer l'examen
            Exam exam = new Exam(
                    titleField.getText().trim(),
                    descriptionField.getText().trim(),
                    professorId.toString(),
                    duration);

            // Générer un code professeur à partir du code étudiant
            // Exemple : PROF-<examId>
          String professorCode = generateProfessorCode();
          exam.setProfessorCode(professorCode);

            // Sauvegarder les questions
            if (!questions.isEmpty()) {
                questionRepository.saveAll(questions);

                // Lier les questions à l'examen
                for (Question q : questions) {
                    if (q.getId() != null) {
                        exam.getQuestionIds().add(q.getId());
                    }
                }
            }

            // Sauvegarder l'examen
            examRepository.save(exam);

            // Store last-created exam information (kept only in-memory, overwritten on each creation)
            try {
                lastExamInfo = new LastExamInfo(exam.getExamId(), exam.getProfessorCode(), exam.getId() != null ? exam.getId().toString() : "", exam.getTitle(), exam.getDurationMinutes(), exam.getQuestionIds().size());
                if (lastExamButton != null) {
                    lastExamButton.setDisable(false);
                }
                // persist last exam
                saveLastExamToPrefs(lastExamInfo);
            } catch (Exception ignored) {}

            // Afficher le code d'examen dans une boîte de dialogue
            Platform.runLater(() -> {
                try {
                    showExamCodeDialog(exam);
                } catch (Exception ex) {
                    System.err.println("Erreur lors de l'affichage du code: " + ex.getMessage());
                    ex.printStackTrace();
                    showSuccess("Examen créé! Code: " + exam.getExamId());
                }
                // Après la fermeture du dialog, revenir à l'accueil avec la même transition que handleBack()
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) createButton.getScene().getWindow();
                    Scene scene = stage.getScene();

                    if (scene != null && scene.getRoot() != null) {
                        Node currentRoot = scene.getRoot();
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(event -> {
                            // Replace root on the existing scene to preserve fullscreen and styles
                            scene.setRoot(root);

                            // Fade in the new root
                            root.setOpacity(0.0);
                            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
                            fadeIn.setFromValue(0.0);
                            fadeIn.setToValue(1.0);
                            fadeIn.play();

                            // Preserve window state
                            stage.setFullScreen(true);
                            stage.setTitle("Système de Gestion d'Examens QCM");
                           
                        });
                        fadeOut.play();
                    } else {
                        Scene newScene = new Scene(root);
                        stage.setScene(newScene);
                        stage.setFullScreen(true);
                        stage.setTitle("Système de Gestion d'Examens QCM");
                        
                    }
                } catch (IOException ioe) {
                    System.err.println("Erreur lors du retour à l'accueil après création: " + ioe.getMessage());
                    ioe.printStackTrace();
                }
            });

            // Désactiver le bouton de création
            createButton.setDisable(true);

        } catch (Exception e) {
            e.printStackTrace();
            // Afficher l'erreur complète dans une alerte
            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText("Erreur lors de la création de l'examen");
                errorAlert.setContentText("Détails: " + e.getMessage() +
                        "\n\nVérifiez que:\n" +
                        "1. MongoDB est démarré\n" +
                        "2. Tous les champs sont remplis correctement\n" +
                        "3. Au moins une question avec 2 choix minimum");
                errorAlert.showAndWait();
                showError("Erreur: " + e.getMessage());
            });
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            Scene scene = stage.getScene();

            if (scene != null && scene.getRoot() != null) {
                Node currentRoot = scene.getRoot();
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> {
                    // Replace root on the existing scene to preserve fullscreen and styles
                    scene.setRoot(root);

                    // Fade in the new root
                    root.setOpacity(0.0);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();

                    // Preserve window state
                    stage.setFullScreen(true);
                    stage.setTitle("Système de Gestion d'Examens QCM");
                   
                });
                fadeOut.play();
            } else {
                // Fallback: create a new scene if none exists
                Scene newScene = new Scene(root);
                stage.setScene(newScene);
                stage.setFullScreen(true);
                stage.setTitle("Système de Gestion d'Examens QCM");
                
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du retour à l'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px;");
    }
    
    private String generateProfessorCode() {
    // Générer un code professeur spécial de 8 caractères (lettres et chiffres)
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 8; i++) {
        int index = (int) (Math.random() * chars.length());
        sb.append(chars.charAt(index));
    }
    return sb.toString();
}


    private void showExamCodeDialog(Exam exam) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Examen Créé avec Succès!");
        alert.setHeaderText("Votre examen a été créé");
        alert.initOwner(createButton.getScene().getWindow());

        // Créer un contenu avec le code en grand
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(15);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Code Étudiant (pour passer l'examen):");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Code en très grand et copiable
        TextField codeField = new TextField(exam.getExamId());
        codeField.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-alignment: center;");
        codeField.setEditable(false);
        codeField.setPrefWidth(300);
        codeField.setOnMouseClicked(e -> {
            try {
                codeField.selectAll();
            } catch (Exception ex) {
                // Ignorer les erreurs de sélection
            }
        });

        Label professorCodeLabel = new Label("Code Professeur (pour voir les résultats): " + exam.getProfessorCode());
        professorCodeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label instructionLabel = new Label(
                "Partagez le code étudiant avec vos étudiants.\nConservez le code professeur pour consulter les résultats.");
        instructionLabel.setStyle("-fx-font-size: 12px;");
        instructionLabel.setWrapText(true);

        Label examInfoLabel = new Label(
                "Titre: " + exam.getTitle() + "\n" +
                        "Durée: " + exam.getDurationMinutes() + " minutes\n" +
                        "Nombre de questions: " + exam.getQuestionIds().size());
        examInfoLabel.setStyle("-fx-font-size: 12px;");

        Button copyButton = new Button("Copier le Code");
        copyButton.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content2 = new javafx.scene.input.ClipboardContent();
            content2.putString(exam.getExamId());
            clipboard.setContent(content2);
            copyButton.setText("Code Copié!");
            copyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        });

        content.getChildren().addAll(titleLabel, codeField, copyButton, professorCodeLabel, instructionLabel,
                examInfoLabel);
        alert.getDialogPane().setContent(content);

        // Agrandir la boîte de dialogue
        alert.getDialogPane().setPrefWidth(500);

        // Attach to application Stage (prefer the createButton's window) and center the dialog
        Stage owner = null;
        try {
            if (createButton != null && createButton.getScene() != null && createButton.getScene().getWindow() instanceof Stage) {
                owner = (Stage) createButton.getScene().getWindow();
            } else {
                for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                    if (w instanceof Stage && w.isShowing()) { owner = (Stage) w; break; }
                }
            }
        } catch (Exception ignored) {}

        if (owner != null) {
            try {
                alert.initOwner(owner);
                Stage finalOwner = owner;
                alert.setOnShown(event -> {
                    try {
                        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
                        dialogStage.setX(finalOwner.getX() + finalOwner.getWidth() / 2 - dialogStage.getWidth() / 2);
                        dialogStage.setY(finalOwner.getY() + finalOwner.getHeight() / 2 - dialogStage.getHeight() / 2);
                    } catch (Exception ignored) {}
                });
            } catch (Exception ignored) {}
        }

        alert.showAndWait();

        // Afficher aussi dans le label de statut
        showSuccess("Examen créé! Code: " + exam.getExamId());

        // Also update lastExamInfo (ensures we have the info even if called directly)
        try {
            lastExamInfo = new LastExamInfo(exam.getExamId(), exam.getProfessorCode(), exam.getId() != null ? exam.getId().toString() : "", exam.getTitle(), exam.getDurationMinutes(), exam.getQuestionIds().size());
            if (lastExamButton != null) lastExamButton.setDisable(false);
            saveLastExamToPrefs(lastExamInfo);
        } catch (Exception ignored) {}
    }

    // Classe interne pour gérer les médias
    private class MediaHandler {
        private String mediaPath;
        private MediaType mediaType;
        private VBox previewContainer;

        public MediaHandler(VBox previewContainer) {
            this.previewContainer = previewContainer;
        }

        public void addMedia() {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner un média");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tous les médias", "*.png", "*.jpg", "*.jpeg", "*.mp4", "*.mp3", "*.wav"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Vidéos", "*.mp4"),
                new FileChooser.ExtensionFilter("Audios", "*.mp3", "*.wav")
            );
            File selectedFile = fileChooser.showOpenDialog(previewContainer.getScene().getWindow());
            if (selectedFile != null) {
                try {
                    mediaPath = selectedFile.toURI().toString();
                    mediaType = detectMediaType(selectedFile);
                    showPreview();
                } catch (Exception e) {
                    showError("Erreur lors du chargement du média: " + e.getMessage());
                }
            }
        }

        private MediaType detectMediaType(File file) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                return MediaType.IMAGE;
            } else if (name.endsWith(".mp4")) {
                return MediaType.VIDEO;
            } else if (name.endsWith(".mp3") || name.endsWith(".wav")) {
                return MediaType.AUDIO;
            }
            return MediaType.NONE;
        }

        private void showPreview() {
            previewContainer.getChildren().clear();
            if (MediaType.IMAGE.equals(mediaType)) {
                try {
                    ImageView imageView = new ImageView(new Image(mediaPath));
                    imageView.setFitWidth(200);
                    imageView.setPreserveRatio(true);
                    previewContainer.getChildren().add(imageView);
                } catch (Exception e) {
                    showError("Erreur de chargement de l'image: " + e.getMessage());
                }
            } else if (MediaType.VIDEO.equals(mediaType)) {
                try {
                    Media media = new Media(mediaPath);
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    MediaView mediaView = new MediaView(mediaPlayer);
                    mediaView.setFitWidth(200);
                    mediaView.setPreserveRatio(true);
                    HBox controls = new HBox(10);
                    Button playButton = new Button("▶");
                    playButton.setOnAction(e -> mediaPlayer.play());
                    Button pauseButton = new Button("⏸");
                    pauseButton.setOnAction(e -> mediaPlayer.pause());
                    controls.getChildren().addAll(playButton, pauseButton);
                    previewContainer.getChildren().addAll(mediaView, controls);
                } catch (Exception e) {
                    showError("Erreur de chargement de la vidéo: " + e.getMessage());
                }
            } else if (MediaType.AUDIO.equals(mediaType)) {
                try {
                    Media media = new Media(mediaPath);
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    Label audioLabel = new Label("🔊 " + new File(mediaPath).getName());
                    HBox controls = new HBox(10);
                    Button playButton = new Button("▶");
                    playButton.setOnAction(e -> mediaPlayer.play());
                    Button pauseButton = new Button("⏸");
                    pauseButton.setOnAction(e -> mediaPlayer.pause());
                    controls.getChildren().addAll(audioLabel, playButton, pauseButton);
                    previewContainer.getChildren().add(controls);
                } catch (Exception e) {
                    showError("Erreur de chargement de l'audio: " + e.getMessage());
                }
            }
        }

        public String getMediaPath() {
            return mediaPath;
        }

        public MediaType getMediaType() {
            return mediaType;
        }
    }

    // Keep last-created exam info (only the most recent exam is retained)
    private LastExamInfo lastExamInfo = null;

    private void showLastExamDialog() {
        if (lastExamInfo == null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
        
            a.setTitle("Dernier examen créé");
            a.setHeaderText("Aucune information");
            a.setContentText("Aucun examen créé pour le moment.");
            a.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dernier examen créé");
        alert.setHeaderText("Informations du dernier examen");

        VBox content = new VBox(10);
        content.setPadding(new Insets(12));

        Label studCode = new Label("Code Étudiant: " + lastExamInfo.studentCode);
        studCode.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");
        Label profCode = new Label("Code Professeur: " + lastExamInfo.professorCode);
        profCode.setStyle("-fx-font-size: 13px;");
        Label idLbl = new Label("ID interne: " + lastExamInfo.internalId);
        Label titleLbl = new Label("Titre: " + (lastExamInfo.title != null ? lastExamInfo.title : "N/A"));
        Label durLbl = new Label("Durée (minutes): " + lastExamInfo.durationMinutes);
        Label qCount = new Label("Nombre de questions: " + lastExamInfo.questionCount);

        Button copyStudent = new Button("Copier Code Étudiant");
        copyStudent.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent c = new javafx.scene.input.ClipboardContent();
            c.putString(lastExamInfo.studentCode != null ? lastExamInfo.studentCode : "");
            clipboard.setContent(c);
            copyStudent.setText("Copié !");
        });

        Button copyProfessor = new Button("Copier Code Professeur");
        copyProfessor.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent c = new javafx.scene.input.ClipboardContent();
            c.putString(lastExamInfo.professorCode != null ? lastExamInfo.professorCode : "");
            clipboard.setContent(c);
            copyProfessor.setText("Copié !");
        });

        HBox btns = new HBox(8, copyStudent, copyProfessor);

        content.getChildren().addAll(studCode, profCode, idLbl, titleLbl, durLbl, qCount, btns);

        alert.getDialogPane().setContent(content);

        // Try to attach the alert to the application's active Stage so it appears inside the app
        Stage owner = null;
        try {
            if (createButton != null && createButton.getScene() != null && createButton.getScene().getWindow() instanceof Stage) {
                owner = (Stage) createButton.getScene().getWindow();
            } else {
                for (Window w : Window.getWindows()) {
                    if (w instanceof Stage && w.isShowing()) { owner = (Stage) w; break; }
                }
            }
        } catch (Exception ignored) {}

        if (owner != null) {
            try {
                alert.initOwner(owner);
                Stage finalOwner = owner;
                alert.setOnShown(event -> {
                    try {
                        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
                        dialogStage.setX(finalOwner.getX() + finalOwner.getWidth() / 2 - dialogStage.getWidth() / 2);
                        dialogStage.setY(finalOwner.getY() + finalOwner.getHeight() / 2 - dialogStage.getHeight() / 2);
                    } catch (Exception ignored) {}
                });
            } catch (Exception ignored) {}
        }

        alert.showAndWait();
    }

    // Persist last exam info to Java Preferences so it survives controller reloads
    private void saveLastExamToPrefs(LastExamInfo info) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(CreateExamController.class);
            prefs.put("last_student_code", info.studentCode != null ? info.studentCode : "");
            prefs.put("last_professor_code", info.professorCode != null ? info.professorCode : "");
            prefs.put("last_internal_id", info.internalId != null ? info.internalId : "");
            prefs.put("last_title", info.title != null ? info.title : "");
            prefs.putInt("last_duration", info.durationMinutes);
            prefs.putInt("last_qcount", info.questionCount);
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde lastExam prefs: " + e.getMessage());
        }
    }

    private void loadLastExamFromPrefs() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(CreateExamController.class);
            String studentCode = prefs.get("last_student_code", null);
            if (studentCode == null || studentCode.isEmpty()) {
                lastExamInfo = null;
                return;
            }
            String professorCode = prefs.get("last_professor_code", "");
            String internalId = prefs.get("last_internal_id", "");
            String title = prefs.get("last_title", "");
            int duration = prefs.getInt("last_duration", 0);
            int qcount = prefs.getInt("last_qcount", 0);
            lastExamInfo = new LastExamInfo(studentCode, professorCode, internalId, title, duration, qcount);
        } catch (Exception e) {
            System.err.println("Erreur chargement lastExam prefs: " + e.getMessage());
            lastExamInfo = null;
        }
    }

    private static class LastExamInfo {
        final String studentCode;
        final String professorCode;
        final String internalId;
        final String title;
        final int durationMinutes;
        final int questionCount;

        LastExamInfo(String studentCode, String professorCode, String internalId, String title, int durationMinutes, int questionCount) {
            this.studentCode = studentCode;
            this.professorCode = professorCode;
            this.internalId = internalId != null ? internalId.toString() : "";
            this.title = title;
            this.durationMinutes = durationMinutes;
            this.questionCount = questionCount;
        }
    }

    // Classe interne pour gérer chaque question
    private class QuestionPanel {
        private VBox container;
        private TextArea questionText;
        private VBox choicesContainer;
        private ComboBox<Integer> maxAnswersCombo;
        private int questionNumber;
        private Button addMediaButton;
        private VBox mediaContainer;
        private MediaHandler mediaHandler;
        private List<ChoicePanel> choicePanels = new ArrayList<>();

        public QuestionPanel(int number) {
            this.questionNumber = number;
            createUI();
        }

        private void createUI() {
            container = new VBox(10);
            container.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 15; -fx-background-radius: 5;");
            container.setPadding(new Insets(15));

            // En-tête de la question
            HBox header = new HBox(10);
            Label questionLabel = new Label("Question " + questionNumber);
            questionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

            Button removeButton = new Button("Supprimer");
            removeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            removeButton.setOnAction(e -> {
                try {
                    if (questionsContainer.getChildren().contains(container)) {
                        questionsContainer.getChildren().remove(container);
                    }
                    questionPanels.remove(this);
                    updateQuestionNumbers();
                } catch (Exception ex) {
                    System.err.println("Erreur lors de la suppression de la question: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            header.getChildren().addAll(questionLabel, new javafx.scene.layout.Region(), removeButton);
            HBox.setHgrow(header.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);

            // Texte de la question
            questionText = new TextArea();
            questionText.setPromptText("Entrez votre question ici...");
            questionText.setPrefRowCount(2);
            questionText.setWrapText(true);

            // Nombre de réponses
            HBox maxAnswersBox = new HBox(10);
            maxAnswersBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label maxAnswersLabel = new Label("Nombre de réponses possibles:");
            maxAnswersCombo = new ComboBox<>();
            maxAnswersCombo.getItems().addAll(1, 2);
            maxAnswersCombo.setValue(1);
            maxAnswersBox.getChildren().addAll(maxAnswersLabel, maxAnswersCombo);

            // Choix
            Label choicesLabel = new Label("Choix de réponses:");
            choicesLabel.setStyle("-fx-font-weight: bold;");
            choicesContainer = new VBox(10);

            // Ajouter 2 choix par défaut
            addChoice();
            addChoice();

            Button addChoiceButton = new Button("+ Ajouter un choix");
            addChoiceButton.setOnAction(e -> addChoice());

            // Média
            Label mediaLabel = new Label("Média (optionnel):");
            mediaLabel.setStyle("-fx-font-weight: bold;");
            addMediaButton = new Button("📎 Ajouter un média");
            mediaContainer = new VBox(10);
            mediaContainer.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 5;");
            mediaHandler = new MediaHandler(mediaContainer);
            addMediaButton.setOnAction(e -> mediaHandler.addMedia());

            container.getChildren().addAll(header, questionText, maxAnswersBox,
                    choicesLabel, choicesContainer, addChoiceButton, mediaLabel, addMediaButton, mediaContainer);
        }

        private void addChoice() {
            ChoicePanel choicePanel = new ChoicePanel();
            choicePanels.add(choicePanel);
            choicesContainer.getChildren().add(choicePanel.getContainer());
        }

        public Question getQuestion() {
            if (questionText.getText().trim().isEmpty()) {
                return null;
            }

            Question question = new Question(questionText.getText().trim(),
                    maxAnswersCombo.getValue() != null ? maxAnswersCombo.getValue().intValue() : 1, mediaHandler.getMediaPath(), mediaHandler.getMediaType());

            for (ChoicePanel choicePanel : choicePanels) {
                Choice choice = choicePanel.getChoice();
                if (choice != null) {
                    question.getChoices().add(choice);
                }
            }

            return question;
        }

        public VBox getContainer() {
            return container;
        }

        private void updateQuestionNumbers() {
            try {
                for (int i = 0; i < questionPanels.size(); i++) {
                    QuestionPanel panel = questionPanels.get(i);
                    if (panel != null && panel.container != null &&
                            panel.container.getChildren().size() > 0 &&
                            panel.container.getChildren().get(0) instanceof HBox) {
                        HBox header = (HBox) panel.container.getChildren().get(0);
                        if (header.getChildren().size() > 0 &&
                                header.getChildren().get(0) instanceof Label) {
                            ((Label) header.getChildren().get(0)).setText("Question " + (i + 1));
                            panel.questionNumber = i + 1;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la mise à jour des numéros de questions: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Classe interne pour gérer chaque choix
        private class ChoicePanel {
            private HBox container;
            private TextField choiceText;
            private CheckBox isCorrect;
            private Button addMediaButton;
            private VBox mediaContainer;
            private MediaHandler mediaHandler;

            public ChoicePanel() {
                createUI();
            }

            private void createUI() {
                container = new HBox(10);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                choiceText = new TextField();
                choiceText.setPromptText("Texte du choix");
                HBox.setHgrow(choiceText, javafx.scene.layout.Priority.ALWAYS);

                isCorrect = new CheckBox("Correct");

                addMediaButton = new Button("🖼️");
                addMediaButton.setStyle("-fx-font-size: 14px;");
                mediaContainer = new VBox(5);
                mediaContainer.setPrefWidth(200);
                mediaHandler = new MediaHandler(mediaContainer);
                addMediaButton.setOnAction(e -> mediaHandler.addMedia());

                Button removeChoice = new Button("×");
                removeChoice.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                removeChoice.setOnAction(e -> {
                    try {
                        if (choicesContainer.getChildren().contains(container)) {
                            choicesContainer.getChildren().remove(container);
                        }
                    } catch (Exception ex) {
                        System.err.println("Erreur lors de la suppression du choix: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });

                container.getChildren().addAll(choiceText, isCorrect, addMediaButton, mediaContainer, removeChoice);
            }

            public Choice getChoice() {
                if (choiceText.getText().trim().isEmpty()) {
                    return null;
                }
                return new Choice(choiceText.getText().trim(), isCorrect.isSelected(),
                        mediaHandler.getMediaPath(), mediaHandler.getMediaType());
            }

            public HBox getContainer() {
                return container;
            }
        }
    }
}
