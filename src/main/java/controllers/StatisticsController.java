package controllers;

import database.StudentRepository;
import database.QuestionRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import models.Student;
import models.Question;
import models.Answer;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import models.Exam;

public class StatisticsController {
    @FXML private Button backButton;
    
    // Labels des cartes en haut (déclaration unique)
    @FXML private Label successPercentLabel;
    @FXML private Label averageLabel;
    @FXML private Label maxScoreLabel;
    @FXML private Label minScoreLabel;

    @FXML private PieChart successPieChart;
    @FXML private PieChart questionPieChart;

    @FXML private BarChart<String, Number> scoreBarChart;
    @FXML private CategoryAxis categoryAxis;
    @FXML private NumberAxis numberAxis;

    @FXML private BarChart<String, Number> rangeBarChart;
    @FXML private CategoryAxis rangeCategoryAxis;
    @FXML private NumberAxis rangeNumberAxis;

    @FXML private TextField questionNumberInput;
    private final StudentRepository studentRepo = new StudentRepository();
    private final QuestionRepository questionRepo = new QuestionRepository();
    private List<Student> allStudents;
    private List<Question> allQuestions;
    private Exam currentExam; // Ajout de la variable d'instance pour l'examen courant

    @FXML
    public void initialize() {
        try {
            allStudents = studentRepo.findAll();
            allQuestions = questionRepo.findAll();

            if (allStudents == null || allStudents.isEmpty()) {
                successPieChart.setTitle("Pas de données");
                successPieChart.setData(FXCollections.observableArrayList());
                scoreBarChart.getData().clear();
                rangeBarChart.getData().clear();
                questionPieChart.setData(FXCollections.observableArrayList());
                
                successPercentLabel.setText("0%");
                averageLabel.setText("0.00/20");
                maxScoreLabel.setText("0.00");
                minScoreLabel.setText("0.00");
                return;
            }

            updateStatisticsCards(allStudents);
            populatePieChart(allStudents);
            populateDistributionChart(allStudents);
            populateRangeChart(allStudents);
            loadQuestions();

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

public void loadForExam(Exam exam) {
    try {
        if (exam == null) {
            System.err.println("loadForExam: exam is null");
            return;
        }
        this.currentExam = exam;

        // Charger uniquement les étudiants liés à cet examen (Student.examId stocke ObjectId)
        if (exam.getId() != null) {
            this.allStudents = studentRepo.findByExamId(exam.getId());
        } else {
            // Fallback: si id absent, charger tout (ou utiliser examId string si besoin)
            this.allStudents = studentRepo.findAll();
        }

        // Charger seulement les questions référencées par l'examen (via questionIds)
        if (exam.getQuestionIds() != null && !exam.getQuestionIds().isEmpty()) {
            this.allQuestions = questionRepo.findByIds(exam.getQuestionIds());
        } else {
            // Fallback: si pas de questionIds, on peut charger toutes les questions (selon logique)
            this.allQuestions = questionRepo.findAll();
        }

        // Mettre à jour les composants UI en fonction des données filtrées
        updateStatisticsCards(allStudents);
        populatePieChart(allStudents);
        populateDistributionChart(allStudents);
        populateRangeChart(allStudents);

        // Recharger le contrôle de sélection (ici vous avez un TextField)
        loadQuestions(); // votre implémentation de loadQuestions s'appuie sur allQuestions

    } catch (Exception e) {
        System.err.println("Erreur dans loadForExam: " + e.getMessage());
        e.printStackTrace();
    }
}

private void loadQuestions() {
    try {
        if (allQuestions != null && !allQuestions.isEmpty()) {
            // Afficher la première question par défaut
            if (!allQuestions.isEmpty()) {
                questionNumberInput.setText("1");
                updateQuestionPieChart(allQuestions.get(0));
            }
            
            // Ajouter un listener au TextField pour détecter les changements
            questionNumberInput.textProperty().addListener((obs, oldVal, newVal) -> {
                // Vérifier que la saisie est un nombre valide
                if (newVal != null && !newVal.isEmpty()) {
                    try {
                        int questionNumber = Integer.parseInt(newVal);
                        
                        // Vérifier que le numéro est dans la plage valide (1 à nombre total de questions)
                        if (questionNumber >= 1 && questionNumber <= allQuestions.size()) {
                            // Index est 0-based, numéro de question est 1-based
                            Question selectedQuestion = allQuestions.get(questionNumber - 1);
                            updateQuestionPieChart(selectedQuestion);
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer si ce n'est pas un nombre valide
                    }
                }
            });
        } else {
            questionNumberInput.setDisable(true);
            questionNumberInput.setPromptText("Aucune question disponible");
        }
    } catch (Exception e) {
        System.err.println("Erreur lors du chargement des questions: " + e.getMessage());
        e.printStackTrace();
    }
}
    private void updateQuestionPieChart(Question question) {
        if (allStudents == null || allStudents.isEmpty() || question == null) {
            questionPieChart.setData(FXCollections.observableArrayList());
            return;
        }

        long successCount = 0;
        long failCount = 0;

        ObjectId questionId = question.getId();

        for (Student student : allStudents) {
            if (student.getAnswers() != null && !student.getAnswers().isEmpty()) {
                // Trouver la réponse de l'étudiant à cette question
                Answer studentAnswer = student.getAnswers().stream()
                    .filter(a -> a.getQuestionId() != null && a.getQuestionId().equals(questionId))
                    .findFirst()
                    .orElse(null);

                if (studentAnswer != null) {
                    // Vérifier si la réponse est correcte
                    boolean isCorrect = isAnswerCorrect(studentAnswer, question);
                    if (isCorrect) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } else {
                    // L'étudiant n'a pas répondu à cette question
                    failCount++;
                }
            } else {
                failCount++;
            }
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Réussite", successCount),
                new PieChart.Data("Échec", failCount)
        );

        questionPieChart.setData(pieData);
        questionPieChart.setClockwise(true);
        questionPieChart.setLabelsVisible(true);
        questionPieChart.setLegendVisible(true);
        questionPieChart.setStartAngle(90);

        double total = (double) (successCount + failCount);
        if (total > 0) {
            for (PieChart.Data d : pieData) {
                double value = d.getPieValue();
                String percent = String.format("%.1f%%", (value / total) * 100.0);
                d.setName(d.getName() + " (" + percent + ")");
                Tooltip.install(d.getNode(), new Tooltip(d.getName() + " : " + (int) value));
            }
        }
    }

    private boolean isAnswerCorrect(Answer answer, Question question) {
        if (question == null || question.getChoices() == null || question.getChoices().isEmpty()) {
            return false;
        }

        // Trouver les indices corrects
        List<Integer> correctIndices = new ArrayList<>();
        for (int i = 0; i < question.getChoices().size(); i++) {
            if (question.getChoices().get(i).isCorrect()) {
                correctIndices.add(i);
            }
        }

        // Comparer les réponses sélectionnées avec les réponses correctes
        List<Integer> selectedIndices = answer.getSelectedChoiceIndices();
        
        if (selectedIndices == null || selectedIndices.isEmpty()) {
            return false;
        }

        // Les deux listes doivent être identiques
        return selectedIndices.size() == correctIndices.size() &&
               selectedIndices.stream().allMatch(correctIndices::contains);
    }

    private void updateStatisticsCards(List<Student> students) {
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        long successCount = 0;

        for (Student s : students) {
            double score = s.getScore();
            sum += score;
            min = Math.min(min, score);
            max = Math.max(max, score);
            if (score >= 12.0) successCount++;
        }

        double average = sum / students.size();
        double successPercent = (successCount * 100.0) / students.size();

        successPercentLabel.setText(String.format("%.0f%%", successPercent));
        averageLabel.setText(String.format("%.2f/20", average));
        maxScoreLabel.setText(String.format("%.2f", max));
        minScoreLabel.setText(String.format("%.2f", min));
    }

    private void populatePieChart(List<Student> students) {
        long successCount = students.stream().filter(s -> s.getScore() >= 12.0).count();
        long failCount = students.size() - successCount;

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Réussite", successCount),
                new PieChart.Data("Échec", failCount)
        );

        successPieChart.setData(pieData);
        successPieChart.setClockwise(true);
        successPieChart.setLabelsVisible(true);
        successPieChart.setLegendVisible(true);
        successPieChart.setStartAngle(90);

        double total = (double) students.size();
        for (PieChart.Data d : pieData) {
            double value = d.getPieValue();
            String percent = String.format("%.1f%%", (value / total) * 100.0);
            d.setName(d.getName() + " (" + percent + ")");
            Tooltip.install(d.getNode(), new Tooltip(d.getName() + " : " + (int) value));
        }
    }

    private void populateDistributionChart(List<Student> students) {
        int[] freq = new int[21];
        for (Student s : students) {
            int note = (int) Math.round(s.getScore());
            if (note < 1) note = 1;
            if (note > 20) note = 20;
            freq[note]++;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre d'étudiants");

        ObservableList<String> categories = FXCollections.observableArrayList();
        for (int i = 1; i <= 20; i++) categories.add(String.valueOf(i));
        categoryAxis.setCategories(categories);

        for (int i = 1; i <= 20; i++) {
            series.getData().add(new XYChart.Data<>(String.valueOf(i), freq[i]));
        }

        scoreBarChart.getData().clear();
        scoreBarChart.getData().add(series);

        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> d : series.getData()) {
                if (d.getNode() != null) {
                    Tooltip.install(d.getNode(), new Tooltip(d.getXValue() + " → " + d.getYValue().intValue()));
                }
            }
        });
    }

    private void populateRangeChart(List<Student> students) {
        int[] ranges = new int[4];
        for (Student s : students) {
            double score = s.getScore();
            if (score >= 0 && score < 5) ranges[0]++;
            else if (score >= 5 && score < 10) ranges[1]++;
            else if (score >= 10 && score < 15) ranges[2]++;
            else if (score >= 15) ranges[3]++;
        }

        XYChart.Series<String, Number> rangeSeries = new XYChart.Series<>();
        rangeSeries.setName("Nombre d'étudiants");
        rangeSeries.getData().add(new XYChart.Data<>("0-5", ranges[0]));
        rangeSeries.getData().add(new XYChart.Data<>("5-10", ranges[1]));
        rangeSeries.getData().add(new XYChart.Data<>("10-15", ranges[2]));
        rangeSeries.getData().add(new XYChart.Data<>("15-20", ranges[3]));

        rangeBarChart.getData().clear();
        rangeBarChart.getData().add(rangeSeries);

        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> d : rangeSeries.getData()) {
                if (d.getNode() != null) {
                    Tooltip.install(d.getNode(), new Tooltip(d.getXValue() + " → " + d.getYValue().intValue()));
                }
            }
        });
    }

@FXML
private void handleBackToResults() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view_results.fxml"));
        Parent root = loader.load();

        // Transférer l'examen courant (si présent) au controller de la page résultats
        controllers.ViewResultsController resultsController = loader.getController();
        if (resultsController != null && this.currentExam != null) {
            resultsController.showExamResults(this.currentExam);
        }

        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = stage.getScene();
        if (scene != null && scene.getRoot() != null) {
            scene.setRoot(root);
            stage.setTitle("Resultats des Etudiants");
        } else {
            stage.setScene(new Scene(root));
            stage.setTitle("Resultats des Etudiants");
        }
    } catch (IOException e) {
        System.err.println("Impossible de retourner aux résultats: " + e.getMessage());
        e.printStackTrace();
    }
}

    // Classe interne pour afficher les numéros de question
    private static class QuestionDisplay {
        private final int questionNumber;
        private final Question question;

        public QuestionDisplay(int number, Question question) {
            this.questionNumber = number;
            this.question = question;
        }

        public Question getQuestion() {
            return question;
        }

        @Override
        public String toString() {
            return "Question " + questionNumber;
        }
    }
}