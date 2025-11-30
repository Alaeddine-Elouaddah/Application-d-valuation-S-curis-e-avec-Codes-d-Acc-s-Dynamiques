package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

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
            stage.setScene(new Scene(root));
            stage.setTitle("Créer un Examen");
            stage.centerOnScreen();
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
            stage.setScene(new Scene(root));
            stage.setTitle("Rejoindre un Examen");
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page de rejoindre un examen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
