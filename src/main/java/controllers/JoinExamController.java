package controllers;

import database.ExamRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.Exam;

import java.io.IOException;

public class JoinExamController {

    @FXML
    private TextField examCodeField;

    @FXML
    private TextField studentNameField;

    @FXML
    private TextField listNumberField;

    @FXML
    private TextField filiereField;

    @FXML
    private StackPane countdownOverlay;

    @FXML
    private Label countdownLabel;

    @FXML
    private Button joinButton;

    @FXML
    private Button backButton;

    @FXML
    private Label errorLabel;

    private ExamRepository examRepository = new ExamRepository();

    @FXML
    private void initialize() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
        if (countdownOverlay != null) {
            countdownOverlay.setVisible(false);
        }
        examCodeField.requestFocus();
    }

 @FXML
    private void handleJoinExam() {
        if (errorLabel != null) errorLabel.setText("");

        String examCode = examCodeField.getText().trim().toUpperCase();
        System.out.println("[JoinExamController] handleJoinExam: user entered code='" + examCode + "'");

        if (examCode.isEmpty()) {
            showError("Veuillez entrer un code!");
            examCodeField.requestFocus();
            return;
        }

        // Essayer d'abord comme code professeur (8 caractères sans PROF-)
        if (examCode.length() == 8) {
            try {
                Exam exam = examRepository.findByProfessorCode(examCode);
                if (exam != null) {
                    // Code professeur trouvé
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view_results.fxml"));
                    Parent root = loader.load();
                    ViewResultsController controller = loader.getController();
                    controller.showExamResults(exam);
                    
                    Stage stage = (Stage) joinButton.getScene().getWindow();
                    setSceneWithFade(stage, root, "Résultats - " + exam.getTitle());
                    return;
                }
            } catch (IOException | RuntimeException e) {
                showError("Erreur lors de l'ouverture des résultats: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        // Essayer comme code étudiant (6 caractères)
        if (examCode.length() != 6) { 
            showError("Le code doit contenir 6 caractères (étudiant) ou 8 caractères (professeur)!"); 
            examCodeField.selectAll(); 
            examCodeField.requestFocus(); 
            return; 
        }

        // Flux étudiant
        String studentName = studentNameField.getText().trim();
        String listNumber = listNumberField.getText().trim();
        String filiere = filiereField.getText().trim();

        if (studentName.isEmpty()) { showError("Veuillez entrer votre nom!"); studentNameField.requestFocus(); return; }
        if (filiere.isEmpty()) { showError("Veuillez entrer votre filière!"); filiereField.requestFocus(); return; }

        try {
            Exam exam = examRepository.findByExamId(examCode);
            System.out.println("[JoinExamController] after findByExamId -> exam=" + (exam == null ? "null" : exam.getTitle()));

            if (exam == null) {
                showError("Code d'examen invalide! Vérifiez le code et réessayez.");
                examCodeField.selectAll();
                examCodeField.requestFocus();
                return;
            }

            if (!exam.isActive()) { showError("Cet examen n'est plus actif!"); return; }

            showExamInfo(exam, studentName, listNumber, filiere);

        } catch (Exception e) {
            showError("Erreur lors de la recherche de l'examen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showExamInfo(Exam exam, String studentName, String listNumber, String filiere) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Vous allez rejoindre l'examen:");

        String studentInfo = "Étudiant: " + studentName;
        if (listNumber != null && !listNumber.isEmpty()) studentInfo += "\nNuméro: " + listNumber;
        studentInfo += "\nFilière: " + filiere;

        alert.setContentText(
                "Titre: " + exam.getTitle() + "\n" +
                "Description: " + (exam.getDescription() == null || exam.getDescription().isEmpty() ? "Aucune" : exam.getDescription()) + "\n" +
                "Durée: " + exam.getDurationMinutes() + " minutes\n" +
                "Nombre de questions: " + (exam.getQuestionIds() != null ? exam.getQuestionIds().size() : 0) + "\n\n" +
                studentInfo + "\n\n" +
                "Êtes-vous prêt à commencer?"
        );

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                startCountdown(exam, studentName, listNumber, filiere);
            }
        });
    }

    private void startCountdown(Exam exam, String studentName, String listNumber, String filiere) {
        if (countdownOverlay != null) countdownOverlay.setVisible(true);

        Thread thread = new Thread(() -> {
            try {
                for (int i = 5; i > 0; i--) {
                    final int count = i;
                    javafx.application.Platform.runLater(() -> {
                        if (countdownLabel != null) countdownLabel.setText(String.valueOf(count));
                    });
                    Thread.sleep(1000);
                }
                javafx.application.Platform.runLater(() -> startExam(exam, studentName, listNumber, filiere));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void startExam(Exam exam, String studentName, String listNumber, String filiere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exam.fxml"));
            Parent root = loader.load();

            ExamController controller = loader.getController();
            controller.initData(exam, studentName, listNumber, filiere);

            Stage stage = (Stage) joinButton.getScene().getWindow();
            setSceneWithFade(stage, root, "Examen en cours - " + exam.getTitle());

            if (countdownOverlay != null) countdownOverlay.setVisible(false);

        } catch (Exception e) {
            // Catch any exception (IOException or runtime exception from controller.initData)
            e.printStackTrace();
            showError("Impossible de lancer l'examen: " + e.getMessage());
            System.err.println("[JoinExamController] startExam failed: " + e.getMessage());
            if (countdownOverlay != null) countdownOverlay.setVisible(false);
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            setSceneWithFade(stage, root, "Système de Gestion d'Examens QCM");
        } catch (IOException e) {
            System.err.println("Erreur lors du retour à l'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) errorLabel.setText(message);
    }

    // --- Gestion centralisée des transitions et plein écran ---
    private void setSceneWithFade(Stage stage, Parent newRoot, String title) {
        Scene scene = stage.getScene();
        if (scene != null && scene.getRoot() != null) {
            Node currentRoot = scene.getRoot();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                scene.setRoot(newRoot);

                newRoot.setOpacity(0.0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

                        stage.setMaximized(true); // Important pour supprimer la barre Windows
                        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.keyCombination("ESC"));
                        stage.setFullScreenExitHint("");
                        stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
                stage.setFullScreen(true);
                
            });
            fadeOut.play();
        } else {
            Scene newScene = new Scene(newRoot);
                    stage.setMaximized(true);
                    stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.keyCombination("ESC"));
                    stage.setFullScreenExitHint("");
                    stage.setFullScreen(true);
            stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.keyCombination("ESC"));
            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);
        }
    }
}
