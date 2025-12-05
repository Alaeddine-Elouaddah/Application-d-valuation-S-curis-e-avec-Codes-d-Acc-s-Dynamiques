package controllers;

import database.ExamRepository;
import database.StudentRepository;
import javafx.animation.FadeTransition;
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

import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

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
    private Spinner<Integer> rowsPerPageSpinner;
    @FXML
    private Label statsLabel;
    @FXML
    private Label totalStudentsLabel;

    // Pagination / controls from FXML
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label currentPageLabel;
    @FXML
    private Label totalPagesLabel;
    @FXML
    private TextField searchField;

    private StudentRepository studentRepo = new StudentRepository();
    private ExamRepository examRepo = new ExamRepository();

    // Full dataset and the filtered dataset used for pagination
    private ObservableList<StudentResult> allResults = FXCollections.observableArrayList();
    private ObservableList<StudentResult> filteredResults = FXCollections.observableArrayList();

    // Pagination state
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadExams();
        loadAllResults();
        setupFilterListener();

        // Initialize rows per page Spinner
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 500, 10, 5);
        rowsPerPageSpinner.setValueFactory(valueFactory);
        rowsPerPageSpinner.setEditable(true);

        // Listener pour appliquer le changement
        rowsPerPageSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal > 0) {
                updateTableWithNewPageSize(newVal);
            }
        });

        // Prev / Next handlers
        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                refreshDisplayedResults();
            }
        });

        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                refreshDisplayedResults();
            }
        });

        // Search field (simple client-side filter)
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                applyFilters();
            });
        }
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

            // Initially filteredResults contains everything
            filteredResults.setAll(allResults);

            // Reset pagination and show first page
            currentPage = 1;
            pageSize = rowsPerPageSpinner.getValue() != null ? rowsPerPageSpinner.getValue() : pageSize;
            refreshDisplayedResults();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des résultats: " + e.getMessage());
            e.printStackTrace();
            showError("Impossible de charger les résultats: " + e.getMessage());
        }
    }

    private void setupFilterListener() {
        examFilterCombo.setOnAction(event -> applyFilters());
    }

    private void applyFilters() {
        // Filter by exam selection and searchField text
        Exam selectedExam = examFilterCombo.getValue();
        String query = (searchField != null && searchField.getText() != null) ? searchField.getText().trim().toLowerCase() : "";

        ObservableList<StudentResult> filtered = FXCollections.observableArrayList();
        for (StudentResult r : allResults) {
            boolean matchesExam = (selectedExam == null) || r.getExamTitle().equals(selectedExam.getTitle());
            boolean matchesSearch = query.isEmpty()
                    || (r.getStudentName() != null && r.getStudentName().toLowerCase().contains(query))
                    || (r.getListNumber() != null && r.getListNumber().toLowerCase().contains(query))
                    || (r.getExamTitle() != null && r.getExamTitle().toLowerCase().contains(query));
            if (matchesExam && matchesSearch) {
                filtered.add(r);
            }
        }
        filteredResults.setAll(filtered);

        // Reset to first page and refresh
        currentPage = 1;
        refreshDisplayedResults();
    }

    private void refreshDisplayedResults() {
        int total = filteredResults.size();
        totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        ObservableList<StudentResult> pageList = FXCollections.observableArrayList();
        if (fromIndex < toIndex) {
            pageList.addAll(filteredResults.subList(fromIndex, toIndex));
        }

        resultsTable.setItems(pageList);
        updateStatistics(pageList);

        totalStudentsLabel.setText("Total: " + total + " étudiant(s)");
        updatePaginationInfo();
    }

    private void updatePaginationInfo() {
        if (currentPageLabel != null) currentPageLabel.setText(String.valueOf(currentPage));
        if (totalPagesLabel != null) totalPagesLabel.setText(String.valueOf(totalPages));
        if (prevButton != null) prevButton.setDisable(currentPage <= 1);
        if (nextButton != null) nextButton.setDisable(currentPage >= totalPages);
    }

    public void showExamResults(Exam exam) {
        if (exam == null) {
            return;
        }

        // Sélectionner l'examen dans la combo si présent
        if (examFilterCombo != null) {
            examFilterCombo.setValue(exam);
        }

        applyFilters();
    }

    private void updateStatistics(ObservableList<StudentResult> results) {
        if (results == null || results.isEmpty()) {
            if (statsLabel != null) {
                statsLabel.setText("Moyenne: -- | Min: -- | Max: --");
            }
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
        if (statsLabel != null) {
            statsLabel.setText(String.format("Moyenne: %.2f | Min: %.2f | Max: %.2f", average, min, max));
        }
    }

    @FXML
    private void handleRefresh() {
        loadAllResults();
        loadExams();
        examFilterCombo.setValue(null);
        if (searchField != null) searchField.clear();
    }

    @FXML
    private void openHome(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) resultsTable.getScene().getWindow();
            Scene scene = stage.getScene();

            // Ajout d'une transition si on a déjà une scene avec un root
            if (scene != null && scene.getRoot() != null) {
                Parent currentRoot = scene.getRoot();
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(evt -> {
                    // Remplacer le root et faire apparaître en fondu
                    scene.setRoot(root);
                    root.setOpacity(0.0);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();

                    stage.setTitle("Accueil");
                    // Permettre sortie plein-écran avec ESC et masquer l'indication
                    stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("ESC"));
                    stage.setFullScreenExitHint("");
                    // Activer le plein-écran après le changement de scène pour éviter clignotements
                    javafx.application.Platform.runLater(() -> stage.setFullScreen(true));
                });
                fadeOut.play();
            } else {
                // Pas de scene existante => nouvelle scene
                Scene newScene = new Scene(root);
                stage.setScene(newScene);
                stage.setTitle("Accueil");
                stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("ESC"));
                stage.setFullScreenExitHint("");
                stage.setFullScreen(true);
            }
        } catch (IOException e) {
            System.err.println("Impossible d'ouvrir la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openStatistics(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/statistics.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) resultsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistiques");
        } catch (IOException e) {
            System.err.println("Impossible d'ouvrir la page des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openJoinExam(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/join_exam.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) resultsTable.getScene().getWindow();
            Scene scene = stage.getScene();

            // Transition fade-out / fade-in
            if (scene != null && scene.getRoot() != null) {
                Parent currentRoot = scene.getRoot();
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(evt -> {
                    scene.setRoot(root);
                    root.setOpacity(0.0);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();

                    stage.setTitle("Rejoindre un Examen");
                    stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("ESC"));
                    stage.setFullScreenExitHint("");
                    javafx.application.Platform.runLater(() -> stage.setFullScreen(true));
                });
                fadeOut.play();
            } else {
                Scene newScene = new Scene(root);
                stage.setScene(newScene);
                stage.setTitle("Rejoindre un Examen");
                stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("ESC"));
                stage.setFullScreenExitHint("");
                stage.setFullScreen(true);
            }
        } catch (IOException e) {
            System.err.println("Impossible d'ouvrir la page de rejoindre un examen: " + e.getMessage());
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

    private void updateTableWithNewPageSize(int newPageSize) {
        // Réinitialiser à la première page avec le nouveau nombre d'éléments
        pageSize = newPageSize;
        currentPage = 1;
        refreshDisplayedResults();
    }
}
