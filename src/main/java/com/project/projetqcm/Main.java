package com.project.projetqcm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.input.KeyCombination;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger l'interface principale (Home)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            
            // Créer la scène sans dimensions fixes (elle occupera l'écran en fullscreen)
            Scene scene = new Scene(root);
            
            primaryStage.setTitle("Système de Gestion d'Examens QCM");
            primaryStage.setScene(scene);
            
            // Activer le plein écran
            primaryStage.setFullScreen(true);
            
            // Optionnel: permettre à l'utilisateur de quitter le fullscreen avec Escape
            primaryStage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("ESC"));
            
            // Optionnel: masquer le message "Appuyez sur ESC pour quitter"
            primaryStage.setFullScreenExitHint("");
            
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage de l'application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}