package com.project.projetqcm;

/**
 * Launcher pour IntelliJ IDEA
 * Utilisez cette classe comme point d'entrée depuis IntelliJ
 */
public class Launcher {
    public static void main(String[] args) {
        // Vérifier et ajouter les modules JavaFX si nécessaire
        try {
            // Essayer de charger JavaFX
            Class.forName("javafx.application.Application");
            Main.main(args);
        } catch (ClassNotFoundException e) {
            System.err.println("==========================================");
            System.err.println("ERREUR: JavaFX n'est pas trouvé!");
            System.err.println("==========================================");
            System.err.println();
            System.err.println("SOLUTION: Utilisez Maven pour lancer:");
            System.err.println("  mvnw.cmd javafx:run");
            System.err.println();
            System.err.println("Ou configurez IntelliJ avec ces VM options:");
            System.err.println("  --module-path \"$USER_HOME$/.m2/repository/org/openjfx/javafx-controls/21.0.6/javafx-controls-21.0.6.jar;$USER_HOME$/.m2/repository/org/openjfx/javafx-controls/21.0.6/javafx-controls-21.0.6-win.jar;...\" --add-modules javafx.controls,javafx.fxml");
            System.err.println();
            System.err.println("Voir INTELLIJ_SETUP.md pour plus de détails");
            System.exit(1);
        }
    }
}

