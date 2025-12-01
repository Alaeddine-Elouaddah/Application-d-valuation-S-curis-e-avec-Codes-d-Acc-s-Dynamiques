package utils;

import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SceneTransition {
    
    /**
     * Transition fluide entre deux scènes avec fade in/out
     * @param stage La fenêtre principale
     * @param newRoot Le nouveau root FXML chargé
     * @param title Le titre de la fenêtre
     * @param fadeDuration Durée du fade en millisecondes
     */
    public static void transitionTo(Stage stage, Parent newRoot, String title, int fadeDuration) {
        try {
            Parent oldRoot = stage.getScene() != null ? stage.getScene().getRoot() : null;
            
            if (oldRoot != null) {
                // Fade out de l'ancienne scène
                FadeTransition fadeOut = new FadeTransition(Duration.millis(fadeDuration), oldRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                
                fadeOut.setOnFinished(event -> {
                    // Créer la nouvelle scène après le fade out
                    Scene newScene = new Scene(newRoot);
                    stage.setScene(newScene);
                    stage.setTitle(title);
                    
                    // Fade in de la nouvelle scène
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(fadeDuration), newRoot);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                
                fadeOut.play();
            } else {
                // Première scène : fade in direct
                Scene newScene = new Scene(newRoot);
                stage.setScene(newScene);
                stage.setTitle(title);
                
                FadeTransition fadeIn = new FadeTransition(Duration.millis(fadeDuration), newRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Version simplifiée : transition + fullscreen
     */
    public static void transitionToFullscreen(Stage stage, Parent newRoot, String title, int fadeDuration) {
        transitionTo(stage, newRoot, title, fadeDuration);
        
        // Activer fullscreen
        stage.setFullScreen(false);
        stage.setFullScreen(true);
        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.keyCombination("ESC"));
        stage.setFullScreenExitHint("");
    }
}