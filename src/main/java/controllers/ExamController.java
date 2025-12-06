package controllers;

import database.QuestionRepository;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Exam;
import models.Question;
import models.Choice;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamController {

    @FXML
    private Label examTitleLabel;
    @FXML
    private Label studentInfoLabel;
    @FXML
    private Label timerLabel;
    @FXML
    private Label questionNumberLabel;
    @FXML
    private Label questionTextLabel;
    @FXML
    private VBox optionsContainer;
    @FXML
    private Button previousButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button submitButton;

    private Exam exam;
    private String studentName;
    private String listNumber;
    private String filiere;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private Map<Integer, List<Integer>> studentAnswers = new HashMap<>(); // Question Index -> List of selected choice
                                                                          // indices
    private Timeline timeline;
    private int timeSeconds;
    private LocalDateTime startTime; // Pour enregistrer l'heure de début

    private QuestionRepository questionRepository = new QuestionRepository();

    public void initData(Exam exam, String studentName, String listNumber, String filiere) {
        this.exam = exam;
        this.studentName = studentName;
        this.listNumber = listNumber;
        this.filiere = filiere;
        this.startTime = LocalDateTime.now(); // Enregistrer l'heure de début

        examTitleLabel.setText(exam.getTitle());
        studentInfoLabel.setText("Étudiant: " + studentName +
                (listNumber != null && !listNumber.isEmpty() ? " (" + listNumber + ")" : "") +
                " - " + filiere);

        // Charger les questions
        if (exam.getQuestionIds() != null && !exam.getQuestionIds().isEmpty()) {
            this.questions = questionRepository.findByIds(exam.getQuestionIds());
        } else {
            this.questions = new ArrayList<>();
        }

        if (questions.isEmpty()) {
            questionTextLabel.setText("Aucune question dans cet examen.");
            nextButton.setDisable(true);
            return;
        }

        // Initialiser le timer
        timeSeconds = exam.getDurationMinutes() * 60;
        startTimer();

        // Afficher la première question
        showQuestion(0);
    }

    private void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), evt -> {
            timeSeconds--;
            updateTimerLabel();
            if (timeSeconds <= 0) {
                timeline.stop();
                handleSubmit(); // Auto-submit when time is up
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        updateTimerLabel();
    }

    private void updateTimerLabel() {
        int hours = timeSeconds / 3600;
        int minutes = (timeSeconds % 3600) / 60;
        int seconds = timeSeconds % 60;
        timerLabel.setText(String.format("Temps restant: %02d:%02d:%02d", hours, minutes, seconds));

        // Alerte visuelle si moins de 1 minute
        if (timeSeconds < 60) {
            timerLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold; -fx-font-size: 18px;");
        }
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size())
            return;

        currentQuestionIndex = index;
        Question question = questions.get(index);

        questionNumberLabel.setText("Question " + (index + 1) + "/" + questions.size());
        questionTextLabel.setText(question.getText());

        optionsContainer.getChildren().clear();
        List<Choice> choices = question.getChoices();
        List<Integer> selectedIndices = studentAnswers.getOrDefault(index, new ArrayList<>());

        ToggleGroup toggleGroup = new ToggleGroup(); // For single choice questions

        for (int i = 0; i < choices.size(); i++) {
            Choice choice = choices.get(i);
            final int choiceIndex = i;

            // TODO: Gérer le type de question (Single/Multiple)
            // Pour l'instant on suppose Single Choice (RadioButton) ou Multiple (CheckBox)
            // On va utiliser CheckBox pour tout pour simplifier le modèle de données actuel
            // si le type n'est pas explicite
            // Mais idéalement on devrait vérifier question.getType()

            CheckBox checkBox = new CheckBox(choice.getText());
            checkBox.setStyle(
                    "-fx-font-size: 16px; -fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: white; -fx-pref-width: 600;");
            checkBox.setMaxWidth(Double.MAX_VALUE);

            if (selectedIndices.contains(i)) {
                checkBox.setSelected(true);
            }

            checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                List<Integer> currentSelections = studentAnswers.getOrDefault(currentQuestionIndex, new ArrayList<>());
                if (isSelected) {
                    if (!currentSelections.contains(choiceIndex)) {
                        currentSelections.add(choiceIndex);
                    }
                } else {
                    currentSelections.remove(Integer.valueOf(choiceIndex));
                }
                studentAnswers.put(currentQuestionIndex, currentSelections);
            });

            optionsContainer.getChildren().add(checkBox);
        }

        updateNavigationButtons();
    }

    private void updateNavigationButtons() {
        previousButton.setVisible(currentQuestionIndex > 0);

        if (currentQuestionIndex == questions.size() - 1) {
            nextButton.setVisible(false);
            submitButton.setVisible(true);
        } else {
            nextButton.setVisible(true);
            submitButton.setVisible(false);
        }
    }

    @FXML
    private void handlePrevious() {
        if (currentQuestionIndex > 0) {
            showQuestion(currentQuestionIndex - 1);
        }
    }

    @FXML
    private void handleNext() {
        if (currentQuestionIndex < questions.size() - 1) {
            showQuestion(currentQuestionIndex + 1);
        }
    }

    @FXML
    private void handleSubmit() {
        if (timeline != null) {
            timeline.stop();
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Terminer l'examen");
        alert.setHeaderText("Voulez-vous vraiment terminer l'examen ?");
        alert.setContentText("Vous ne pourrez plus modifier vos réponses.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                calculateAndShowResults();
            } else {
                if (timeSeconds > 0)
                    timeline.play(); // Resume timer if cancelled
            }
        });
    }

    private void calculateAndShowResults() {
        int score = 0;
        int totalQuestions = questions.size();

        for (int i = 0; i < totalQuestions; i++) {
            Question q = questions.get(i);
            List<Integer> userIndices = studentAnswers.getOrDefault(i, new ArrayList<>());

            // Logique de correction simple :
            // Si l'étudiant a coché toutes les bonnes réponses et aucune mauvaise

            boolean isCorrect = true;
            List<Choice> choices = q.getChoices();

            // Vérifier si toutes les réponses cochées sont correctes
            for (Integer index : userIndices) {
                if (!choices.get(index).isCorrect()) {
                    isCorrect = false;
                    break;
                }
            }

            // Vérifier si toutes les réponses correctes sont cochées
            if (isCorrect) {
                for (int j = 0; j < choices.size(); j++) {
                    if (choices.get(j).isCorrect() && !userIndices.contains(j)) {
                        isCorrect = false;
                        break;
                    }
                }
            }

            if (isCorrect) {
                score++;
            }
        }

        // Afficher les résultats (Pour l'instant une simple alerte, plus tard une vue
        // dédiée)
        Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
        resultAlert.setTitle("Résultats de l'examen");
        resultAlert.setHeaderText("Examen Terminé !");

        // Convertir le score en note sur 20
        double scoreOn20 = (score * 20.0) / totalQuestions;

        resultAlert.setContentText("Votre score est de : " + score + " / " + totalQuestions + "\n" +
                "Note sur 20: " + String.format("%.2f", scoreOn20) + "/20\n\n" +
                "Merci d'avoir passé l'examen.");

        // Sauvegarder TOUTES les données dans la collection "Student"
        try {
            database.StudentRepository studentRepo = new database.StudentRepository();
            models.Student student = new models.Student(studentName, listNumber, filiere);

            // Ajouter TOUTES les informations d'examen
            student.setExamId(exam.getId());
            student.setScore(scoreOn20);
            student.setStartTime(startTime);
            student.setEndTime(java.time.LocalDateTime.now());
            student.setWarningCount(0);

            // Sauvegarder TOUTES les réponses
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                List<Integer> userIndices = studentAnswers.getOrDefault(i, new ArrayList<>());
                models.Answer answer = new models.Answer(q.getId());
                answer.setSelectedChoiceIndices(userIndices);
                student.getAnswers().add(answer);
            }

            // Sauvegarder dans MongoDB - collection "Student"
            studentRepo.save(student);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde: " + e.getMessage());
            e.printStackTrace();
        }

        resultAlert.showAndWait();
        returnToHome();
    }

    private void returnToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setScene(new Scene(root));

            stage.setTitle("Système de Gestion d'Examens QCM");
            stage.setFullScreen(false);
            stage.setFullScreen(true);
            stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.keyCombination("ESC"));
            stage.setFullScreenExitHint("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
