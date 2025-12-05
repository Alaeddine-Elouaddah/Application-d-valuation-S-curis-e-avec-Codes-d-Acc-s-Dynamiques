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
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.util.Duration;
import database.MongoConnection;
import models.Choice;
import models.Exam;
import models.Question;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private Label statusLabel;

    private List<QuestionPanel> questionPanels = new ArrayList<>();
    private ExamRepository examRepository = new ExamRepository();
    private QuestionRepository questionRepository = new QuestionRepository();

    // ID du professeur (pour l'instant, on utilise un ID par défaut)
    private ObjectId professorId = new ObjectId();

    @FXML
    private void initialize() {
        statusLabel.setText("");
        // Ajouter une première question par défaut
        handleAddQuestion();
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
            String professorCode = "PROF-" + exam.getExamId();
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

    private void showExamCodeDialog(Exam exam) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Examen Créé avec Succès!");
        alert.setHeaderText("Votre examen a été créé");

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

        alert.showAndWait();

        // Afficher aussi dans le label de statut
        showSuccess("Examen créé! Code: " + exam.getExamId());
    }

    // Classe interne pour gérer chaque question
    private class QuestionPanel {
        private VBox container;
        private TextArea questionText;
        private VBox choicesContainer;
        private ComboBox<Integer> maxAnswersCombo;
        private int questionNumber;

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

            container.getChildren().addAll(header, questionText, maxAnswersBox,
                    choicesLabel, choicesContainer, addChoiceButton);
        }

        private void addChoice() {
            HBox choiceBox = new HBox(10);
            choiceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            TextField choiceText = new TextField();
            choiceText.setPromptText("Texte du choix");
            HBox.setHgrow(choiceText, javafx.scene.layout.Priority.ALWAYS);

            CheckBox isCorrect = new CheckBox("Correct");

            Button removeChoice = new Button("×");
            removeChoice.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            removeChoice.setOnAction(e -> {
                try {
                    if (choicesContainer.getChildren().contains(choiceBox)) {
                        choicesContainer.getChildren().remove(choiceBox);
                    }
                } catch (Exception ex) {
                    System.err.println("Erreur lors de la suppression du choix: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            choiceBox.getChildren().addAll(choiceText, isCorrect, removeChoice);
            choicesContainer.getChildren().add(choiceBox);
        }

        public Question getQuestion() {
            if (questionText.getText().trim().isEmpty()) {
                return null;
            }

            Question question = new Question(questionText.getText().trim(),
                    maxAnswersCombo.getValue());

            for (javafx.scene.Node node : choicesContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox choiceBox = (HBox) node;
                    TextField textField = (TextField) choiceBox.getChildren().get(0);
                    CheckBox isCorrect = (CheckBox) choiceBox.getChildren().get(1);

                    if (!textField.getText().trim().isEmpty()) {
                        Choice choice = new Choice(textField.getText().trim(),
                                isCorrect.isSelected());
                        question.getChoices().add(choice);
                    }
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
    }
}
