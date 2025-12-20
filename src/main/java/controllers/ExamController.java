package controllers;

import database.QuestionRepository;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import models.Exam;
import models.Question;
import models.Choice;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class ExamController {

    // UI bindings (kept from original + new ones)
    @FXML private Label examTitleLabel;
    @FXML private Label examDescriptionLabel;
    @FXML private Label examDurationLabel;
    @FXML private Label examCodeLabel;
    @FXML private Label studentNameLabel;
    @FXML private Label studentNumberLabel;
    @FXML private Label studentFiliereLabel;
    @FXML private Label timerLabel;
    @FXML private Label questionNumberLabel;
    @FXML private Label questionTextLabel;
    @FXML private VBox optionsContainer;
    @FXML private Button previousButton;
    @FXML private Button submitButton;
    @FXML private ProgressBar progressBar;

    // New UI elements
    @FXML private VBox questionGrid;
    @FXML private Button btnSaveNext;
    @FXML private Button btnClear;
    @FXML private Button btnMarkReview;
    @FXML private Label totalLabel;
    @FXML private Label visitedLabel;
    @FXML private Label notVisitedLabel;
    @FXML private Label answeredLabel;
    @FXML private Label markedLabel;
    @FXML private Button btnReviewSubmit;
    @FXML private Button btnAbout;
    @FXML private Button btnInstructions;

    // Flat list of buttons (index -> button) for easy styling without relying on container children
    private List<Button> questionButtons = new ArrayList<>();
    // Section indicators: section index -> labels (unvisited, answered, marked)
    private static class SectionInfo {
        Label unvisitedLbl;
        Label answeredLbl;
        Label markedLbl;
    }
    private Map<Integer, SectionInfo> sectionIndicators = new HashMap<>();

    private Exam exam;
    private String studentName;
    private String listNumber;
    private String filiere;
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;

    // State storage: answers and per-question state
    private Map<Integer, List<Integer>> studentAnswers = new HashMap<>(); // questionIndex -> selected choice indices
    private enum QState { NOT_VISITED, VISITED, ANSWERED, MARKED }
    private Map<Integer, QState> questionStates = new HashMap<>();

    private Timeline timeline;
    private int timeSeconds;
    private LocalDateTime startTime; // Pour enregistrer l'heure de début

    private QuestionRepository questionRepository = new QuestionRepository();

    // initData called from caller to supply exam + student info
    public void initData(Exam exam, String studentName, String listNumber, String filiere) {
        try {
            this.exam = exam;
            this.studentName = studentName;
            this.listNumber = listNumber;
            this.filiere = filiere;
            this.startTime = LocalDateTime.now();

            // Set UI labels
            examTitleLabel.setText(exam.getTitle() != null ? exam.getTitle() : "Examen sans titre");
            examDescriptionLabel.setText("Description: " + (exam.getDescription() != null && !exam.getDescription().isEmpty() ? exam.getDescription() : "Aucune description"));
            examDurationLabel.setText("Durée: " + exam.getDurationMinutes() + " minutes");
            examCodeLabel.setText("Code: " + (exam.getExamId() != null ? exam.getExamId() : "N/A"));

            studentNameLabel.setText(studentName != null ? studentName : "-");
            studentNumberLabel.setText(listNumber != null ? listNumber : "-");
            studentFiliereLabel.setText(filiere != null ? filiere : "-");

            // Load questions from repository
            if (exam.getQuestionIds() != null && !exam.getQuestionIds().isEmpty()) {
                this.questions = questionRepository.findByIds(exam.getQuestionIds());
            } else {
                this.questions = new ArrayList<>();
            }

            // initialize states
            for (int i = 0; i < questions.size(); i++) {
                questionStates.put(i, QState.NOT_VISITED);
            }

            // build left grid
            buildQuestionGrid();

            if (questions.isEmpty()) {
                questionTextLabel.setText("Aucune question dans cet examen.");
btnSaveNext.setDisable(true);
                btnSaveNext.setDisable(true);
                return;
            }

            // timer
            timeSeconds = exam.getDurationMinutes() * 60;
            // initialize timer label immediately so UI shows numbers even before Timeline fires
            updateTimerLabel();
            startTimer();
            // show first question
            showQuestion(0);

            // attach handlers for new buttons if not wired in FXML
            if (btnSaveNext != null) {
                btnSaveNext.setOnAction(e -> {
                    saveCurrentSelection();
                    goNext();
                });
            }
            if (btnClear != null) {
                btnClear.setOnAction(e -> {
                    clearCurrentSelection();
                });
            }
            if (btnMarkReview != null) {
                btnMarkReview.setOnAction(e -> {
                    toggleMarkForReview(currentQuestionIndex);
                });
            }
            if (btnReviewSubmit != null) {
                btnReviewSubmit.setOnAction(e -> {
                    Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous soumettre l'examen ?", ButtonType.OK, ButtonType.CANCEL);
                    conf.showAndWait().ifPresent(resp -> {
                        if (resp == ButtonType.OK) calculateAndShowResults();
                    });
                });
            }
            if (btnAbout != null) {
                btnAbout.setOnAction(e -> {
                    try {
                        Stage owner = (Stage) (submitButton != null ? submitButton.getScene().getWindow() : null);
                        Stage dialog = new Stage();
                        dialog.initOwner(owner);
                        dialog.initModality(Modality.WINDOW_MODAL);
                        dialog.initStyle(StageStyle.TRANSPARENT);

                        StringBuilder sb = new StringBuilder();
                        sb.append("Titre: ").append(exam.getTitle() != null ? exam.getTitle() : "N/A").append("\n");
                        sb.append("Code examen: ").append(exam.getExamId() != null ? exam.getExamId() : "N/A").append("\n");
                        sb.append("Durée: ").append(exam.getDurationMinutes()).append(" minutes\n");
                        sb.append("Nombre de questions: ").append(questions != null ? questions.size() : 0).append("\n\n");
                        sb.append("Description: \n").append(exam.getDescription() != null ? exam.getDescription() : "Aucune description.").append("\n\n");
                        sb.append("-- Informations étudiant --\n");
                        sb.append("Nom: ").append(studentName != null ? studentName : "-").append("\n");
                        sb.append("Numéro/Inscription: ").append(listNumber != null ? listNumber : "-").append("\n");
                        sb.append("Filière: ").append(filiere != null ? filiere : "-").append("\n\n");
                        sb.append("-- Informations professeur --\n");
                        String profInfo = "N/A";
                        try {
                            String profId = exam.getProfessorId();
                            if (profId != null && !profId.isEmpty()) {
                                try {
                                    org.bson.types.ObjectId oid = new org.bson.types.ObjectId(profId);
                                    database.UserRepository ur = new database.UserRepository();
                                    models.User u = ur.findById(oid);
                                    if (u != null) profInfo = (u.getName() != null ? u.getName() : "(sans nom)") + " (" + (u.getEmail() != null ? u.getEmail() : "-") + ")";
                                    else profInfo = profId;
                                } catch (IllegalArgumentException ex) {
                                    profInfo = profId;
                                }
                            }
                        } catch (Exception ignored) {}
                        sb.append("Professeur: ").append(profInfo).append("\n");

                        VBox rootBox = new VBox();
                        rootBox.setSpacing(12);
                        rootBox.setPadding(new Insets(18));
                        rootBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10;");
                        rootBox.setEffect(new DropShadow(12, Color.gray(0, 0.25)));

                        Label title = new Label("Détails de l'examen");
                        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700;");

                        TextArea ta = new TextArea(sb.toString());
                        ta.setEditable(false);
                        ta.setWrapText(true);
                        ta.setPrefWidth(640);
                        ta.setPrefHeight(360);

                        HBox btnBox = new HBox();
                        btnBox.setSpacing(8);
                        btnBox.setPadding(new Insets(8, 0, 0, 0));
                        btnBox.setStyle("-fx-alignment: center-right;");

                        Button btnClose = new Button("Fermer");
                        btnClose.setDefaultButton(true);
                        btnClose.setOnAction(ev -> dialog.close());

                        btnBox.getChildren().add(btnClose);
                        rootBox.getChildren().addAll(title, ta, btnBox);

                        Scene scene = new Scene(rootBox);
                        scene.setFill(Color.TRANSPARENT);
                        dialog.setScene(scene);
                        dialog.centerOnScreen();
                        dialog.showAndWait();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Alert a = new Alert(Alert.AlertType.INFORMATION, "Détails de l'examen: " + (exam.getTitle() != null ? exam.getTitle() : "N/A"));
                        a.setHeaderText("Détails de l'examen");
                        a.showAndWait();
                    }
                });
            }
            if (btnInstructions != null) {
                btnInstructions.setOnAction(e -> {
                    try {
                        Stage owner = (Stage) (submitButton != null ? submitButton.getScene().getWindow() : null);
                        Stage dialog = new Stage();
                        dialog.initOwner(owner);
                        dialog.initModality(Modality.WINDOW_MODAL);
                        dialog.initStyle(StageStyle.TRANSPARENT);

                        VBox rootBox = new VBox();
                        rootBox.setSpacing(12);
                        rootBox.setPadding(new Insets(18));
                        rootBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10;");
                        rootBox.setEffect(new DropShadow(12, Color.gray(0, 0.25)));

                        Label title = new Label("Instructions de l'examen");
                        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700;");

                        String instructionsText = "Règles de l'examen:\n- Soyez attentif aux consignes.\n\n" +
                                "Durée:\n- La durée de l'examen est indiquée en haut à droite et le chronomètre décompte le temps.\n\n" +
                                "Navigation entre les questions:\n- Utilisez les boutons 'Suivant'/'Précédent' ou la grille de gauche pour naviguer.\n\n" +
                                "Soumission finale:\n- Cliquez sur 'Terminer' ou attendez la fin du temps pour soumettre automatiquement.\n\n" +
                                "Interdictions:\n- Ne rafraîchissez pas la fenêtre, ne quittez pas l'application pendant l'examen (cela peut entraîner la perte ou le blocage de la session).\n\n" +
                                "Bonne chance !";

                        TextArea ta = new TextArea(instructionsText);
                        ta.setWrapText(true);
                        ta.setEditable(false);
                        ta.setPrefWidth(640);
                        ta.setPrefHeight(360);

                        HBox btnBox = new HBox();
                        btnBox.setSpacing(8);
                        btnBox.setPadding(new Insets(8, 0, 0, 0));
                        btnBox.setStyle("-fx-alignment: center-right;");

                        Button btnClose = new Button("J'ai compris");
                        btnClose.setDefaultButton(true);
                        btnClose.setOnAction(ev -> dialog.close());

                        btnBox.getChildren().add(btnClose);

                        rootBox.getChildren().addAll(title, ta, btnBox);

                        Scene scene = new Scene(rootBox);
                        scene.setFill(Color.TRANSPARENT);
                        dialog.setScene(scene);
                        dialog.centerOnScreen();
                        dialog.showAndWait();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Alert a = new Alert(Alert.AlertType.INFORMATION, "Lire les instructions de l'examen.");
                        a.setHeaderText("Instructions");
                        a.showAndWait();
                    }
                });
            }

            updateOverviewCounts();

        } catch (Exception e) {
            System.err.println("❌ Error in ExamController.initData: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Impossible de charger l'examen");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });
        }
    }

    private void buildQuestionGrid() {
        if (questionGrid == null) return;
        questionGrid.getChildren().clear();
        questionButtons.clear();

        int total = questions.size();
        if (total == 0) return;

        int perSection = 25;
        int sections = (total + perSection - 1) / perSection;

        for (int s = 0; s < sections; s++) {
            int start = s * perSection;
            int end = Math.min(start + perSection, total);

            // Section label (A, B, C...)
            char letter = (char) ('A' + s);
            Label sectionLabel = new Label("Section " + letter);
            sectionLabel.setStyle("-fx-font-weight:700; -fx-text-fill:#374151; -fx-padding:6 0 4 0;");

            // Section header with indicators
            HBox header = new HBox();
            header.setSpacing(8);
            Label left = sectionLabel;
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox indicatorsBox = new HBox();
            indicatorsBox.setSpacing(6);
            Label indUnvisited = new Label();
            Label indAnswered = new Label();
            Label indMarked = new Label();
            indUnvisited.getStyleClass().addAll("section-indicator", "ind-unvisited");
            indAnswered.getStyleClass().addAll("section-indicator", "ind-answered");
            indMarked.getStyleClass().addAll("section-indicator", "ind-marked");
            indicatorsBox.getChildren().addAll(indUnvisited, indAnswered, indMarked);

            header.getChildren().addAll(left, spacer, indicatorsBox);
            header.getStyleClass().add("section-header");

            TilePane tp = new TilePane();
            tp.setHgap(8);
            tp.setVgap(8);
            tp.setPrefTileWidth(48);
            tp.setPrefTileHeight(48);
            try { tp.setPrefColumns(5); } catch (Exception ignored) {}

            for (int i = start; i < end; i++) {
                int idx = i;
                Button tile = new Button(String.valueOf(i + 1));
                // slightly larger to match design and ensure 5 fit per row in the left column
                tile.setPrefSize(48, 48);
                tile.getStyleClass().add("question-tile");
                tile.setOnAction(e -> {
                    // navigate to clicked question (do not auto-save here to avoid double-save)
                    showQuestion(idx);
                });
                questionButtons.add(tile);
                tp.getChildren().add(tile);
            }

            // store section indicator labels for later updates
            SectionInfo info = new SectionInfo();
            info.unvisitedLbl = indUnvisited;
            info.answeredLbl = indAnswered;
            info.markedLbl = indMarked;
            sectionIndicators.put(s, info);

            questionGrid.getChildren().addAll(header, tp);
        }

        refreshGridStyles();
    }

    private void styleTile(Button tile, QState state, boolean selected) {
        // Use CSS classes rather than inline styles to allow consistent theming
        tile.getStyleClass().removeAll("not-visited", "visited", "answered", "marked", "selected", "unanswered");
        if (!tile.getStyleClass().contains("question-tile")) tile.getStyleClass().add("question-tile");
        switch (state) {
            case NOT_VISITED -> tile.getStyleClass().add("not-visited");
            case VISITED -> tile.getStyleClass().add("visited");
            case ANSWERED -> tile.getStyleClass().add("answered");
            case MARKED -> tile.getStyleClass().add("marked");
        }
        if (selected) {
            if (!tile.getStyleClass().contains("selected")) tile.getStyleClass().add("selected");
        } else {
            tile.getStyleClass().remove("selected");
        }
    }

    private void refreshGridStyles() {
        if (questionButtons == null) return;
        for (int i = 0; i < questionButtons.size(); i++) {
            Button b = questionButtons.get(i);
            QState s = questionStates.getOrDefault(i, QState.NOT_VISITED);
            boolean selected = (i == currentQuestionIndex);
            styleTile(b, s, selected);
        }
        // update per-section indicators counts
        updateSectionIndicators();
    }

    private void updateSectionIndicators() {
        if (sectionIndicators.isEmpty()) return;
        int perSection = 25;
        int total = questions.size();
        int sections = (total + perSection - 1) / perSection;
        for (int s = 0; s < sections; s++) {
            int start = s * perSection;
            int end = Math.min(start + perSection, total);
            int cntUnvisited = 0, cntAnswered = 0, cntMarked = 0;
            for (int i = start; i < end; i++) {
                QState st = questionStates.getOrDefault(i, QState.NOT_VISITED);
                switch (st) {
                    case NOT_VISITED -> cntUnvisited++;
                    case VISITED -> {/* visited not counted as answered */}
                    case ANSWERED -> cntAnswered++;
                    case MARKED -> cntMarked++;
                }
            }
            SectionInfo info = sectionIndicators.get(s);
            if (info != null) {
                info.unvisitedLbl.setText(String.valueOf(cntUnvisited));
                info.answeredLbl.setText(String.valueOf(cntAnswered));
                info.markedLbl.setText(String.valueOf(cntMarked));
            }
        }
    }

    private void updateOverviewCounts() {
        int total = questions.size();
        int visited = 0, answered = 0, marked = 0, notVisited = 0;
        for (int i = 0; i < total; i++) {
            QState s = questionStates.getOrDefault(i, QState.NOT_VISITED);
            switch (s) {
                case NOT_VISITED -> notVisited++;
                case VISITED -> visited++;
                case ANSWERED -> { answered++; visited++; }
                case MARKED -> marked++;
            }
        }
        if (totalLabel != null) totalLabel.setText(String.valueOf(total));
        if (visitedLabel != null) visitedLabel.setText(String.valueOf(visited));
        if (answeredLabel != null) answeredLabel.setText(String.valueOf(answered));
        if (markedLabel != null) markedLabel.setText(String.valueOf(marked));
        if (notVisitedLabel != null) notVisitedLabel.setText(String.valueOf(notVisited));
    }

    private void saveCurrentSelection() {
        int idx = currentQuestionIndex;
        if (idx < 0 || idx >= questions.size()) return;

        int selIndex = -1;
        // children may be wrapper VBox containing a ToggleButton; find selected ToggleButton
        for (int i = 0; i < optionsContainer.getChildren().size(); i++) {
            var node = optionsContainer.getChildren().get(i);
            ToggleButton tb = null;
            if (node instanceof ToggleButton) tb = (ToggleButton) node;
            else if (node instanceof VBox) {
                VBox vb = (VBox) node;
                if (!vb.getChildren().isEmpty() && vb.getChildren().get(0) instanceof ToggleButton) {
                    tb = (ToggleButton) vb.getChildren().get(0);
                }
            } else if (node instanceof HBox) {
                HBox hb = (HBox) node;
                if (!hb.getChildren().isEmpty() && hb.getChildren().get(0) instanceof ToggleButton) {
                    tb = (ToggleButton) hb.getChildren().get(0);
                }
            }

            if (tb != null && tb.isSelected()) {
                // determine the displayed text for this ToggleButton
                String displayed = tb.getText();
                if (displayed == null || displayed.isEmpty()) {
                    // try to read from graphic (HBox -> Label.option-text)
                    if (tb.getGraphic() instanceof HBox) {
                        HBox g = (HBox) tb.getGraphic();
                        for (var child : g.getChildren()) {
                            if (child instanceof Label) {
                                Label L = (Label) child;
                                // prefer option-text class
                                if (L.getStyleClass().contains("option-text")) {
                                    displayed = L.getText();
                                    break;
                                }
                                // fallback: first label with non-empty text
                                if (displayed == null || displayed.isEmpty()) displayed = L.getText();
                            }
                        }
                    }
                }

                if (displayed != null) {
                    List<Choice> choicesList = questions.get(idx).getChoices();
                    for (int j = 0; j < choicesList.size(); j++) {
                        if (displayed.equals(choicesList.get(j).getText())) {
                            selIndex = j;
                            break;
                        }
                    }
                }

                if (selIndex >= 0) break;
            }
        }

        if (selIndex >= 0) {
            studentAnswers.put(idx, Collections.singletonList(selIndex));
            questionStates.put(idx, QState.ANSWERED);
        } else {
            studentAnswers.remove(idx);
            questionStates.putIfAbsent(idx, QState.VISITED);
        }
        refreshGridStyles();
        updateOverviewCounts();
    }

    private void startTimer() {
        // Simple, reliable timer: run on FX thread and update every second
        if (timeline != null) {
            timeline.stop();
        }

        // defensive fallback for invalid duration
        if (timeSeconds <= 0) {
            int fallbackMinutes = Math.max(1, exam != null ? exam.getDurationMinutes() : 60);
            if (fallbackMinutes <= 0) fallbackMinutes = 60;
            timeSeconds = fallbackMinutes * 60;
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), evt -> {
            timeSeconds = Math.max(0, timeSeconds - 1);
            updateTimerLabel();
            if (timeSeconds <= 0) {
                if (timeline != null) timeline.stop();
                autoSubmitExam();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        // immediate update
        updateTimerLabel();
    }

    private void updateTimerLabel() {
        int hours = timeSeconds / 3600;
        int minutes = (timeSeconds % 3600) / 60;
        int seconds = timeSeconds % 60;
        String formatted = String.format("%02d : %02d : %02d", hours, minutes, seconds);
        if (timerLabel != null) {
            timerLabel.setText(formatted);
            if (timeSeconds < 60) {
                timerLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
            } else {
                timerLabel.setStyle("");
            }
        }
    }

    // Removed previous multi-label binding fallback; timer now uses single `timerLabel` updated by Timeline.

    private void autoSubmitExam() {
        // Ensure timer is stopped, save current visible selection, then show results
        if (timeline != null) {
            timeline.stop();
        }
        Platform.runLater(() -> {
            try {
                saveCurrentSelection();
            } catch (Exception ignored) {}
            calculateAndShowResults();
        });
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        currentQuestionIndex = index;

        // mark visited if first time
        QState prev = questionStates.getOrDefault(index, QState.NOT_VISITED);
        if (prev == QState.NOT_VISITED) questionStates.put(index, QState.VISITED);

        Question q = questions.get(index);
        if (q == null) return;

        questionNumberLabel.setText("Question " + (index + 1) + " / " + questions.size());
        questionTextLabel.setText(q.getText());

        // update progressbar
        if (progressBar != null) {
            double progress = (double) (index + 1) / questions.size();
            progressBar.setProgress(progress);
        }

        // build options as large ToggleButtons to match the target design
        optionsContainer.getChildren().clear();
        ToggleGroup group = new ToggleGroup();
        List<Choice> choices = q.getChoices() != null ? q.getChoices() : new ArrayList<>();

        for (int i = 0; i < choices.size(); i++) {
            int choiceIndex = i;
            Choice c = choices.get(i);

            // create a ToggleButton that contains an HBox (letter + text) as graphic
            ToggleButton tb = new ToggleButton();
            tb.getStyleClass().add("option-button");
            tb.setMaxWidth(Double.MAX_VALUE);
            tb.setToggleGroup(group);

            // graphic HBox
            HBox content = new HBox();
            content.setSpacing(12);
            Label letterLbl = new Label((char) ('a' + i) + ")");
            letterLbl.getStyleClass().add("option-letter");
            Label textLbl = new Label(c.getText());
            textLbl.getStyleClass().add("option-text");
            HBox.setHgrow(textLbl, Priority.ALWAYS);
            content.getChildren().addAll(letterLbl, textLbl);

            tb.setGraphic(content);

            // restore previous selection
            List<Integer> prevSelected = studentAnswers.get(index);
            if (prevSelected != null && !prevSelected.isEmpty() && prevSelected.get(0) == i) {
                tb.setSelected(true);
            }

            // selection listener: update state and answers
            tb.selectedProperty().addListener((obs, was, isNow) -> {
                if (isNow) {
                    studentAnswers.put(index, Collections.singletonList(choiceIndex));
                    questionStates.put(index, QState.ANSWERED);
                } else {
                    if (group.getSelectedToggle() == null) {
                        studentAnswers.remove(index);
                        questionStates.putIfAbsent(index, QState.VISITED);
                    }
                }
                refreshGridStyles();
                updateOverviewCounts();
            });

            // add spacing between options
            VBox wrapper = new VBox(tb);
            wrapper.setPadding(new Insets(6, 0, 0, 0));
            VBox.setVgrow(tb, Priority.NEVER);
            optionsContainer.getChildren().add(wrapper);
        }

        // update buttons visibility / enable
        if (previousButton != null) previousButton.setDisable(index == 0);
        if (btnSaveNext != null) btnSaveNext.setDisable(index == questions.size() - 1);

        // reflect mark-for-review state on the mark button
        if (btnMarkReview != null) {
            QState cur = questionStates.getOrDefault(index, QState.NOT_VISITED);
            if (cur == QState.MARKED) {
                if (!btnMarkReview.getStyleClass().contains("marked-active")) btnMarkReview.getStyleClass().add("marked-active");
            } else {
                btnMarkReview.getStyleClass().remove("marked-active");
            }
        }

        // refresh grid
        refreshGridStyles();
        updateOverviewCounts();
    }

    private void clearCurrentSelection() {
        int idx = currentQuestionIndex;
        if (idx < 0 || idx >= questions.size()) return;
        studentAnswers.remove(idx);
        // if previously answered, change to visited
        questionStates.put(idx, QState.VISITED);
        // clear selection in UI
        for (var node : optionsContainer.getChildren()) {
            // node may be a wrapper VBox containing a ToggleButton as first child
            if (node instanceof ToggleButton) ((ToggleButton) node).setSelected(false);
            else if (node instanceof VBox) {
                VBox vb = (VBox) node;
                if (!vb.getChildren().isEmpty() && vb.getChildren().get(0) instanceof ToggleButton) {
                    ((ToggleButton) vb.getChildren().get(0)).setSelected(false);
                }
            }
        }
        refreshGridStyles();
        updateOverviewCounts();
    }

    private void toggleMarkForReview(int idx) {
        if (idx < 0 || idx >= questions.size()) return;
        QState cur = questionStates.getOrDefault(idx, QState.NOT_VISITED);
        if (cur == QState.MARKED) {
            // unmark -> to answered if has answer else visited
            if (studentAnswers.containsKey(idx)) questionStates.put(idx, QState.ANSWERED);
            else questionStates.put(idx, QState.VISITED);
        } else {
            questionStates.put(idx, QState.MARKED);
        }
        refreshGridStyles();
        updateOverviewCounts();
    }

    private void goNext() {
        if (currentQuestionIndex < questions.size() - 1) {
            showQuestion(currentQuestionIndex + 1);
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
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Quitter l'examen ?");
        alert.setContentText("Votre progression sera sauvegardée. Voulez-vous quitter ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                returnToHome();
            }
        });
    }

    @FXML
    private void handleSubmit() {
        if (timeline != null) {
            timeline.stop();
        }

        if (timeSeconds <= 0) {
            calculateAndShowResults();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Terminer l'examen");
        alert.setHeaderText("Voulez-vous vraiment terminer l'examen ?");
        alert.setContentText("Vous ne pourrez plus modifier vos réponses.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                calculateAndShowResults();
            } else {
                if (timeSeconds > 0 && timeline != null) timeline.play();
            }
        });
    }

    private void calculateAndShowResults() {
        int correctCount = 0;
        int totalQuestions = questions.size();
        for (int i = 0; i < totalQuestions; i++) {
            Question q = questions.get(i);
            List<Integer> userIndices = studentAnswers.getOrDefault(i, Collections.emptyList());

            boolean isCorrect = true;
            List<Choice> choices = q.getChoices();
            // if no answers selected -> not correct
            if (userIndices.isEmpty()) {
                isCorrect = false;
            } else {
                // verify selected indices correspond to correct choices and all correct are selected
                for (Integer idx : userIndices) {
                    if (idx < 0 || idx >= choices.size() || !choices.get(idx).isCorrect()) {
                        isCorrect = false;
                        break;
                    }
                }
                if (isCorrect) {
                    for (int j = 0; j < choices.size(); j++) {
                        if (choices.get(j).isCorrect() && !userIndices.contains(j)) {
                            isCorrect = false;
                            break;
                        }
                    }
                }
            }
            if (isCorrect) correctCount++;
        }

        double scoreOn20 = totalQuestions > 0 ? (correctCount * 20.0 / totalQuestions) : 0.0;

        Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
        resultAlert.setTitle("Résultats de l'examen");
        resultAlert.setHeaderText("Examen Terminé !");
        resultAlert.setContentText("Score: " + correctCount + " / " + totalQuestions + "\nNote sur 20: " + String.format("%.2f", scoreOn20) + "/20");
        resultAlert.showAndWait();

        // Persist student and answers (existing logic)
        try {
            database.StudentRepository studentRepo = new database.StudentRepository();
            models.Student student = new models.Student(studentName, listNumber, filiere);
            student.setExamId(exam.getId());
            student.setScore(scoreOn20);
            student.setStartTime(startTime);
            student.setEndTime(java.time.LocalDateTime.now());
            student.setWarningCount(0);

            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                List<Integer> userIndices = studentAnswers.getOrDefault(i, Collections.emptyList());
                models.Answer answer = new models.Answer(q.getId());
                answer.setSelectedChoiceIndices(userIndices);
                student.getAnswers().add(answer);
            }
            studentRepo.save(student);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde: " + e.getMessage());
            e.printStackTrace();
        }

        // return to home or close
        returnToHome();
    }

    private void returnToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) (submitButton != null ? submitButton.getScene().getWindow() : null);
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setFullScreen(false);
                stage.setFullScreen(true);
                stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.keyCombination("ESC"));
                stage.setFullScreenExitHint("");
            } else {
                Platform.exit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}