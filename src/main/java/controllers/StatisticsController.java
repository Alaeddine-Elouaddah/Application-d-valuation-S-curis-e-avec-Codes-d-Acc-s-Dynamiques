package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class StatisticsController {
    @FXML
    private Button backButton;

    @FXML
    private void handleBackToResults() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view_results.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Resultats des Etudiants");
        } catch (IOException e) {
            System.err.println("Impossible de retourner aux résultats: " + e.getMessage());
            e.printStackTrace();
        }
    }
}