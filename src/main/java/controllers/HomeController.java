package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

import java.io.IOException;

public class HomeController {

    @FXML
    private Button createExamButton;

    @FXML
    private Button joinExamButton;

    @FXML
    private void handleCreateExam() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create_exam.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) createExamButton.getScene().getWindow();
            Scene scene = stage.getScene();

            // Add keyboard handler for Ctrl+Q
            addKeyEventHandler(root, stage);

            if (scene != null && scene.getRoot() != null) {
                Node currentRoot = scene.getRoot();
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> {
                    scene.setRoot(root);

                    root.setOpacity(0.0);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();

                    // Preserve window state and ensure maximized (undecorated stage set in Main)
                    stage.setTitle("Créer un Examen");
                    // Force maximized after fade transition completes to avoid taskbar flash
                    javafx.application.Platform.runLater(() -> {
                        stage.setMaximized(true);
                    });
                    
                });
                fadeOut.play();
            } else {
                Scene newScene = new Scene(root);
                newScene.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
                    if (evt.isControlDown() && evt.getCode() == KeyCode.Q) {
                        stage.close();
                    }
                });
                stage.setScene(newScene);
                stage.setTitle("Créer un Examen");
                stage.setMaximized(true);
                
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page de création d'examen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleJoinExam() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/join_exam.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) joinExamButton.getScene().getWindow();
            Scene scene = stage.getScene();

            // Add keyboard handler for Ctrl+Q
            addKeyEventHandler(root, stage);

            if (scene != null && scene.getRoot() != null) {
                Node currentRoot = scene.getRoot();
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> {
                    scene.setRoot(root);

                    root.setOpacity(0.0);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();

                    stage.setTitle("Rejoindre un Examen");
                    stage.setFullScreenExitHint("");
                    // Force fullscreen after fade transition completes
                    javafx.application.Platform.runLater(() -> {
                        stage.setMaximized(true);
                    });
                
                });
                fadeOut.play();
            } else {
                Scene newScene = new Scene(root);
                newScene.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
                    if (evt.isControlDown() && evt.getCode() == KeyCode.Q) {
                        stage.close();
                    }
                });
                stage.setScene(newScene);
                stage.setTitle("Rejoindre un Examen");
                stage.setFullScreenExitHint("");
                stage.setFullScreen(true);
               
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page de rejoindre un examen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addKeyEventHandler(Parent root, Stage stage) {
        root.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.isControlDown() && evt.getCode() == KeyCode.Q) {
                stage.close();
            }
        });
    }
}
