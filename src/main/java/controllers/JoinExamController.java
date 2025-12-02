package controllers;

import database.ExamRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        if (errorLabel != null) {
            errorLabel.setText("");
        }

        String examCode = examCodeField.getText().trim().toUpperCase();

        // Si le code commence par PROF-, on considère que c'est un code professeur
        if (examCode.startsWith("PROF-")) {
            if (examCode.isEmpty()) {
                showError("Veuillez entrer le code professeur!");
                examCodeField.requestFocus();
                return;
            }

            try {
                Exam exam = examRepository.findByProfessorCode(examCode);
                if (exam == null) {
                    showError("Code professeur invalide! Vérifiez le code et réessayez.");
                    examCodeField.selectAll();
                    examCodeField.requestFocus();
                    return;
                }

                // Ouvrir l'écran des résultats filtré sur cet examen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view_results.fxml"));
                Parent root = loader.load();

                ViewResultsController controller = loader.getController();
                controller.showExamResults(exam);

                Stage stage = (Stage) joinButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Résultats - " + exam.getTitle());
                stage.setFullScreen(false);
                stage.setFullScreen(true);
                stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.keyCombination("ESC"));
                stage.setFullScreenExitHint("");

            } catch (IOException e) {
                showError("Erreur lors de l'ouverture des résultats: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                showError("Erreur lors de la recherche de l'examen: " + e.getMessage());
                e.printStackTrace();
            }

            return;
        }

        // Sinon, flux normal pour les étudiants
        String studentName = studentNameField.getText().trim();
        String listNumber = listNumberField.getText().trim();
        String filiere = filiereField.getText().trim();

        // Validation
        if (examCode.isEmpty()) {
            showError("Veuillez entrer le code d'examen!");
            examCodeField.requestFocus();
            return;
        }

        if (studentName.isEmpty()) {
            showError("Veuillez entrer votre nom!");
            studentNameField.requestFocus();
            return;
        }

        if (filiere.isEmpty()) {
            showError("Veuillez entrer votre filière!");
            filiereField.requestFocus();
            return;
        }

        if (examCode.length() != 6) {
            showError("Le code d'examen doit contenir 6 caractères!");
            examCodeField.selectAll();
            examCodeField.requestFocus();
            return;
        }

        try {
            // Chercher l'examen par code étudiant
            Exam exam = examRepository.findByExamId(examCode);

            if (exam == null) {
                showError("Code d'examen invalide! Vérifiez le code et réessayez.");
                examCodeField.selectAll();
                examCodeField.requestFocus();
                return;
            }

            if (!exam.isActive()) {
                showError("Cet examen n'est plus actif!");
                return;
            }

            // Afficher les informations de l'examen pour l'étudiant
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
        if (listNumber != null && !listNumber.isEmpty()) {
            studentInfo += "\nNuméro: " + listNumber;
        }
        studentInfo += "\nFilière: " + filiere;

        alert.setContentText(
                "Titre: " + exam.getTitle() + "\n" +
                        "Description: " + (exam.getDescription().isEmpty() ? "Aucune" : exam.getDescription()) + "\n" +
                        "Durée: " + exam.getDurationMinutes() + " minutes\n" +
                        "Nombre de questions: " + exam.getQuestionIds().size() + "\n\n" +
                        studentInfo + "\n\n" +
                        "Êtes-vous prêt à commencer?");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                startCountdown(exam, studentName, listNumber, filiere);
            }
        });
    }

    private void startCountdown(Exam exam, String studentName, String listNumber, String filiere) {
        if (countdownOverlay != null) {
            countdownOverlay.setVisible(true);
        }

        Thread thread = new Thread(() -> {
            try {
                for (int i = 5; i > 0; i--) {
                    final int count = i;
                    javafx.application.Platform.runLater(() -> {
                        if (countdownLabel != null) {
                            countdownLabel.setText(String.valueOf(count));
                        }
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
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setTitle("Examen en cours - " + exam.getTitle());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible de lancer l'examen: " + e.getMessage());
            if (countdownOverlay != null) {
                countdownOverlay.setVisible(false);
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Système de Gestion d'Examens QCM");
            stage.setFullScreen(false);
            stage.setFullScreen(true);
            stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.keyCombination("ESC"));
            stage.setFullScreenExitHint("");
        } catch (IOException e) {
            System.err.println("Erreur lors du retour à l'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }
}