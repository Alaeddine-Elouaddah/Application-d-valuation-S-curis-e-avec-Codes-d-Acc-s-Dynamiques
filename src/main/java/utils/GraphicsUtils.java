package utils;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Classe utilitaire pour créer des graphiques statistiques
 * Affiche la distribution des notes des étudiants
 */
public class GraphicsUtils {

    /**
     * Crée un graphique en barres montrant la distribution des notes
     * 
     * @param scores Liste des scores des étudiants
     * @param maxScore Score maximum possible
     * @return VBox contenant le graphique
     */
    public static VBox createScoreDistributionChart(List<Double> scores, double maxScore) {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10;");

        // Titre
        Label titleLabel = new Label("Distribution des Notes");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f1724;");

        // Calculer les intervalles de notes
        Map<String, Integer> distribution = calculateScoreDistribution(scores, maxScore);

        // Créer le graphique BarChart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Intervalle de Notes");
        xAxis.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre d'Étudiants");
        yAxis.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Répartition des Scores");
        barChart.setStyle("-fx-font-size: 14px;");
        barChart.setPrefHeight(400);
        barChart.setLegendVisible(false);

        // Créer la série de données
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre d'étudiants");

        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(data);

            // Colorer les barres selon le score
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    String interval = entry.getKey();
                    newNode.setStyle(getBarColor(interval));
                }
            });
        }

        barChart.getData().add(series);

        // Statistiques résumées
        Label statsLabel = createStatisticsLabel(scores);

        container.getChildren().addAll(titleLabel, barChart, statsLabel);
        return container;
    }

    /**
     * Crée un graphique circulaire (PieChart) montrant les réussite/échecs
     * 
     * @param scores Liste des scores des étudiants
     * @param passingScore Score minimum pour réussir
     * @return VBox contenant le graphique
     */
    public static VBox createPassFailChart(List<Double> scores, double passingScore) {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10;");

        Label titleLabel = new Label("Taux de Réussite");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f1724;");

        // Compter réussite/échec
        long passed = scores.stream().filter(s -> s >= passingScore).count();
        long failed = scores.size() - passed;

        javafx.scene.chart.PieChart pieChart = new javafx.scene.chart.PieChart();
        pieChart.setTitle("Réussite vs Échec");
        pieChart.setPrefHeight(300);
        pieChart.setStyle("-fx-font-size: 12px;");

        javafx.scene.chart.PieChart.Data passedSlice = new javafx.scene.chart.PieChart.Data(
                "Réussi (" + passed + ")", passed);
        javafx.scene.chart.PieChart.Data failedSlice = new javafx.scene.chart.PieChart.Data(
                "Échec (" + failed + ")", failed);

        pieChart.getData().addAll(passedSlice, failedSlice);

        // Colorer les slices
        passedSlice.getNode().setStyle("-fx-pie-color: #4CAF50;"); // Vert
        failedSlice.getNode().setStyle("-fx-pie-color: #F44336;"); // Rouge

        container.getChildren().addAll(titleLabel, pieChart);
        return container;
    }

    /**
     * Crée un graphique en ligne montrant la tendance des scores
     * 
     * @param scores Liste des scores
     * @return VBox contenant le graphique
     */
    public static VBox createScoreTrendChart(List<Double> scores) {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10;");

        Label titleLabel = new Label("Tendance des Scores");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f1724;");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Numéro Étudiant");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Score");

        javafx.scene.chart.LineChart<String, Number> lineChart = new javafx.scene.chart.LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Scores des Étudiants");
        lineChart.setPrefHeight(350);
        lineChart.setStyle("-fx-font-size: 12px;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Scores");

        for (int i = 0; i < scores.size(); i++) {
            series.getData().add(new XYChart.Data<>(String.valueOf(i + 1), scores.get(i)));
        }

        lineChart.getData().add(series);
        container.getChildren().addAll(titleLabel, lineChart);
        return container;
    }

    // ==================== MÉTHODES PRIVÉES ====================

    /**
     * Calcule la distribution des notes par intervalles
     * 
     * @param scores Liste des scores
     * @param maxScore Score maximum
     * @return Map avec intervalles et compte d'étudiants
     */
    private static Map<String, Integer> calculateScoreDistribution(List<Double> scores, double maxScore) {
        Map<String, Integer> distribution = new TreeMap<>();

        // Créer les intervalles (par exemple : 0-5, 5-10, 10-15, 15-20)
        int intervalSize = (int) (maxScore / 4); // 4 intervalles
        if (intervalSize == 0) intervalSize = 1;

        for (int i = 0; i <= maxScore; i += intervalSize) {
            String key = i + "-" + (i + intervalSize - 1);
            distribution.put(key, 0);
        }

        // Compter les scores dans chaque intervalle
        for (Double score : scores) {
            int interval = (int) (score / intervalSize);
            String key = (interval * intervalSize) + "-" + ((interval + 1) * intervalSize - 1);

            if (distribution.containsKey(key)) {
                distribution.put(key, distribution.get(key) + 1);
            }
        }

        return distribution;
    }

    /**
     * Retourne la couleur CSS pour une barre selon l'intervalle
     * 
     * @param interval Intervalle de notes
     * @return Style CSS avec couleur
     */
    private static String getBarColor(String interval) {
        // Extraire le score minimum de l'intervalle
        int minScore = Integer.parseInt(interval.split("-")[0]);

        if (minScore >= 15) {
            return "-fx-bar-fill: #4CAF50;"; // Vert (excellent)
        } else if (minScore >= 10) {
            return "-fx-bar-fill: #8BC34A;"; // Vert clair (bon)
        } else if (minScore >= 5) {
            return "-fx-bar-fill: #FFC107;"; // Jaune (moyen)
        } else {
            return "-fx-bar-fill: #F44336;"; // Rouge (faible)
        }
    }

    /**
     * Crée un label avec les statistiques résumées
     * 
     * @param scores Liste des scores
     * @return Label avec stats
     */
    private static Label createStatisticsLabel(List<Double> scores) {
        if (scores.isEmpty()) {
            return new Label("Aucune donnée disponible");
        }

        double average = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double max = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double min = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0);

        String stats = String.format(
                "Moyenne : %.2f | Max : %.0f | Min : %.0f | Total : %d étudiants",
                average, max, min, scores.size()
        );

        Label label = new Label(stats);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555; -fx-padding: 10;");
        label.setWrapText(true);
        return label;
    }
}