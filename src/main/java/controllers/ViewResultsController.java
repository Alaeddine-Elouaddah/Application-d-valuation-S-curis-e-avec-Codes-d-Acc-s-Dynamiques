package controllers;

import database.ExamRepository;
import database.StudentRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Exam;
import models.Student;
import models.StudentResult;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewResultsController {

    @FXML
    private TableView<StudentResult> resultsTable;
    @FXML
    private TableColumn<StudentResult, String> nameColumn;
    @FXML
    private TableColumn<StudentResult, String> listNumberColumn;
    @FXML
    private TableColumn<StudentResult, String> filiereColumn;
    @FXML
    private TableColumn<StudentResult, Double> scoreColumn;
    @FXML
    private TableColumn<StudentResult, String> examColumn;
    @FXML
    private TableColumn<StudentResult, String> dateColumn;

    @FXML
    private ComboBox<Exam> examFilterCombo;
    @FXML
    private Label statsLabel;
    @FXML
    private Label totalStudentsLabel;

    private StudentRepository studentRepo = new StudentRepository();
    private ExamRepository examRepo = new ExamRepository();
    private ObservableList<StudentResult> allResults = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadExams();
        loadAllResults();
        setupFilterListener();
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        listNumberColumn.setCellValueFactory(new PropertyValueFactory<>("listNumber"));
        filiereColumn.setCellValueFactory(new PropertyValueFactory<>("filiere"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        examColumn.setCellValueFactory(new PropertyValueFactory<>("examTitle"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("examDate"));

        // Formater la colonne des notes
        scoreColumn.setCellFactory(column -> new TableCell<StudentResult, Double>() {
            @Override
            protected void updateItem(Double score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", score));
                    // Colorer selon la note
                    if (score >= 16) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if (score >= 10) {
                        setStyle("-fx-text-fill: #f39c12;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c;");
                    }
                }
            }
        });
    }

    private void loadExams() {
        try {
            List<Exam> exams = examRepo.findAll();
            examFilterCombo.getItems().clear();
            examFilterCombo.getItems().add(null); // Option "Tous les examens"
            examFilterCombo.getItems().addAll(exams);

            // Affichage personnalisé dans le ComboBox
            examFilterCombo.setButtonCell(new ListCell<Exam>() {
                @Override
                protected void updateItem(Exam exam, boolean empty) {
                    super.updateItem(exam, empty);
                    if (empty || exam == null) {
                        setText("Tous les examens");
                    } else {
                        setText(exam.getTitle());
                    }
                }
            });

            examFilterCombo.setCellFactory(param -> new ListCell<Exam>() {
                @Override
                protected void updateItem(Exam exam, boolean empty) {
                    super.updateItem(exam, empty);
                    if (empty || exam == null) {
                        setText("Tous les examens");
                    } else {
                        setText(exam.getTitle() + " (" + exam.getExamId() + ")");
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des examens: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAllResults() {
        try {
            allResults.clear();
            List<Student> students = studentRepo.findAll();

            // Cache pour les titres d'examens
            Map<ObjectId, String> examTitles = new HashMap<>();

            for (Student student : students) {
                // Récupérer le titre de l'examen (avec cache)
                String examTitle = examTitles.get(student.getExamId());
                if (examTitle == null) {
                    Exam exam = examRepo.findById(student.getExamId());
                    examTitle = exam != null ? exam.getTitle() : "Examen inconnu";
                    examTitles.put(student.getExamId(), examTitle);
                }

                StudentResult result = new StudentResult(
                        student.getStudentName(),
                        student.getStudentListNumber(),
                        student.getStudentFiliere(),
                        student.getScore(),
                        examTitle,
                        student.getEndTime());

                allResults.add(result);
            }

            resultsTable.setItems(allResults);
            updateStatistics(allResults);
            totalStudentsLabel.setText("Total: " + allResults.size() + " étudiant(s)");

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des résultats: " + e.getMessage());
            e.printStackTrace();
            showError("Impossible de charger les résultats: " + e.getMessage());
        }
    }

    private void setupFilterListener() {
        examFilterCombo.setOnAction(event -> {
            Exam selectedExam = examFilterCombo.getValue();
            if (selectedExam == null) {
                // Afficher tous les résultats
                resultsTable.setItems(allResults);
                updateStatistics(allResults);
            } else {
                // Filtrer par examen
                ObservableList<StudentResult> filtered = FXCollections.observableArrayList();
                for (StudentResult result : allResults) {
                    if (result.getExamTitle().equals(selectedExam.getTitle())) {
                        filtered.add(result);
                    }
                }
                resultsTable.setItems(filtered);
                updateStatistics(filtered);
            }
            totalStudentsLabel.setText("Total: " + resultsTable.getItems().size() + " étudiant(s)");
        });
    }

    public void showExamResults(Exam exam) {
        if (exam == null) {
            return;
        }

        // Sélectionner l'examen dans la combo si présent
        if (examFilterCombo != null) {
            examFilterCombo.setValue(exam);
        }

        ObservableList<StudentResult> filtered = FXCollections.observableArrayList();
        for (StudentResult result : allResults) {
            if (result.getExamTitle().equals(exam.getTitle())) {
                filtered.add(result);
            }
        }

        resultsTable.setItems(filtered);
        updateStatistics(filtered);
        totalStudentsLabel.setText("Total: " + resultsTable.getItems().size() + " étudiant(s)");
    }

    private void updateStatistics(ObservableList<StudentResult> results) {
        if (results.isEmpty()) {
            statsLabel.setText("Moyenne: -- | Min: -- | Max: --");
            return;
        }

        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (StudentResult result : results) {
            double score = result.getScore();
            sum += score;
            min = Math.min(min, score);
            max = Math.max(max, score);
        }

        double average = sum / results.size();
        statsLabel.setText(String.format("Moyenne: %.2f | Min: %.2f | Max: %.2f", average, min, max));
    }

    @FXML
    private void handleRefresh() {
        loadAllResults();
        loadExams();
        examFilterCombo.setValue(null);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) resultsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Système de Gestion d'Examens QCM");
            stage.setFullScreen(false);
            stage.setFullScreen(true);
        } catch (IOException e) {
            System.err.println("Erreur lors du retour à l'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
