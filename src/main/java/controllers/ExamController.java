package controllers;

import database.QuestionRepository;
import javafx.animation.KeyFrame;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCombination;
import javafx.event.EventHandler;

import com.project.models.Exam;
import com.project.models.Question;
import com.project.models.Choice;
import com.project.models.MediaType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.awt.Toolkit;
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
    @FXML private VBox questionMediaContainer;
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
    // Prevent duplicate result calculations / alerts
    private boolean submitted = false;

    private QuestionRepository questionRepository = new QuestionRepository();

    // ESC / fullscreen override state (used to temporarily block ESC during exam)
    private KeyCombination prevFullScreenExitKeyCombination = null;
    private Stage escStage = null;
    private EventHandler<KeyEvent> simpleEscBlocker = null;
    private Scene sceneWithSimpleBlocker = null;

    // Stage-level full-screen-exit override removed: rely on JavaFX defaults

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

            // Rely on JavaFX default ESC behavior; no custom handling.

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
                    // Attach confirmation dialog to the app Stage and center it
                    try {
                        Stage owner = getStage();
                        if (owner == null) {
                            for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                                if (w instanceof Stage && w.isShowing()) { owner = (Stage) w; break; }
                            }
                        }
                        if (owner != null) {
                            conf.initOwner(owner);
                            conf.initModality(Modality.WINDOW_MODAL);
                            Stage finalOwner = owner;
                            conf.setOnShown(evt -> {
                                try {
                                    Stage dialogStage = (Stage) conf.getDialogPane().getScene().getWindow();
                                    dialogStage.setX(finalOwner.getX() + finalOwner.getWidth() / 2 - dialogStage.getWidth() / 2);
                                    dialogStage.setY(finalOwner.getY() + finalOwner.getHeight() / 2 - dialogStage.getHeight() / 2);
                                } catch (Exception ignored) {}
                            });
                        }
                    } catch (Exception ignored) {}

                    conf.showAndWait().ifPresent(resp -> {
                        if (resp == ButtonType.OK) {
                            try { saveCurrentSelection(); } catch (Exception ignored) {}
                            if (!submitted) calculateAndShowResults();
                        }
                    });
                });
            }
            if (btnAbout != null) {
                btnAbout.setOnAction(e -> {
                    try {
                        Stage owner = (Stage) btnAbout.getScene().getWindow();
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
                                    com.project.models.User u = ur.findById(oid);
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
                       Stage owner = (Stage) btnInstructions.getScene().getWindow();
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
        if (questions == null) return;
        int total = questions.size();
        int cntNotVisited = 0, cntVisited = 0, cntAnswered = 0, cntMarked = 0;
        for (int i = 0; i < total; i++) {
            QState st = questionStates.getOrDefault(i, QState.NOT_VISITED);
            switch (st) {
                case NOT_VISITED -> cntNotVisited++;
                case VISITED -> cntVisited++;
                case ANSWERED -> cntAnswered++;
                case MARKED -> cntMarked++;
            }
        }

        final String totalStr = String.valueOf(total);
        final String visitedStr = String.valueOf(cntVisited);
        final String notVisitedStr = String.valueOf(cntNotVisited);
        final String answeredStr = String.valueOf(cntAnswered);
        final String markedStr = String.valueOf(cntMarked);

        Platform.runLater(() -> {
            if (totalLabel != null) totalLabel.setText(totalStr);
            if (visitedLabel != null) visitedLabel.setText(visitedStr);
            if (notVisitedLabel != null) notVisitedLabel.setText(notVisitedStr);
            if (answeredLabel != null) answeredLabel.setText(answeredStr);
            if (markedLabel != null) markedLabel.setText(markedStr);
        });
    }

    private void saveCurrentSelection() {
        int idx = currentQuestionIndex;
        if (idx < 0 || idx >= questions.size()) return;
        // Reconstruct selection from visible option controls (works for ToggleButtons and CheckBoxes)
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < optionsContainer.getChildren().size(); i++) {
            var node = optionsContainer.getChildren().get(i);
            // node may be a wrapper VBox containing a ToggleButton or CheckBox as first child
            Object control = null;
            if (node instanceof ToggleButton) control = node;
            else if (node instanceof CheckBox) control = node;
            else if (node instanceof VBox) {
                VBox vb = (VBox) node;
                if (!vb.getChildren().isEmpty()) control = vb.getChildren().get(0);
            } else if (node instanceof HBox) {
                HBox hb = (HBox) node;
                if (!hb.getChildren().isEmpty()) control = hb.getChildren().get(0);
            }

            if (control instanceof ToggleButton) {
                ToggleButton tb = (ToggleButton) control;
                if (tb.isSelected()) {
                    Object ud = tb.getUserData();
                    if (ud instanceof Integer) selected.add((Integer) ud);
                }
            } else if (control instanceof CheckBox) {
                CheckBox cb = (CheckBox) control;
                if (cb.isSelected()) {
                    Object ud = cb.getUserData();
                    if (ud instanceof Integer) selected.add((Integer) ud);
                }
            }
        }

        if (!selected.isEmpty()) {
            // maintain ordering and immutability for storage
            Collections.sort(selected);
            studentAnswers.put(idx, Collections.unmodifiableList(new ArrayList<>(selected)));
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
            if (!submitted) calculateAndShowResults();
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

        // Display question media
        displayMedia(questionMediaContainer, q.getMediaPath(), q.getMediaType());

        // update progressbar
        if (progressBar != null) {
            double progress = (double) (index + 1) / questions.size();
            progressBar.setProgress(progress);
        }

        // build options: use ToggleButton for single-answer questions, CheckBox for multi-answer
        optionsContainer.getChildren().clear();
        List<Choice> choices = q.getChoices() != null ? q.getChoices() : new ArrayList<>();
        boolean multi = q.getMaxAnswers() > 1;
        ToggleGroup group = multi ? null : new ToggleGroup();

        for (int i = 0; i < choices.size(); i++) {
            int choiceIndex = i;
            Choice c = choices.get(i);

            // graphic HBox shared by both control types
            HBox content = new HBox();
            content.setSpacing(12);
            Label letterLbl = new Label((char) ('a' + i) + ")");
            letterLbl.getStyleClass().add("option-letter");

            VBox choiceContent = new VBox();
            choiceContent.setSpacing(8);
            Label textLbl = new Label(c.getText());
            textLbl.getStyleClass().add("option-text");
            choiceContent.getChildren().add(textLbl);
            if (c.getMediaPath() != null && c.getMediaType() != null && c.getMediaType() != MediaType.NONE) {
                displayChoiceMedia(choiceContent, c.getMediaPath(), c.getMediaType());
            }
            HBox.setHgrow(choiceContent, Priority.ALWAYS);
            content.getChildren().addAll(letterLbl, choiceContent);

            if (multi) {
                CheckBox cb = new CheckBox();
                cb.getStyleClass().add("option-checkbox");
                cb.setMaxWidth(Double.MAX_VALUE);
                cb.setUserData(choiceIndex);
                cb.setGraphic(content);

                // restore previous selection
                List<Integer> prevSelected = studentAnswers.get(index);
                if (prevSelected != null && prevSelected.contains(i)) cb.setSelected(true);

                cb.selectedProperty().addListener((obs, was, isNow) -> {
                    List<Integer> cur = new ArrayList<>(studentAnswers.getOrDefault(index, Collections.emptyList()));
                    if (isNow) {
                        if (!cur.contains(choiceIndex)) cur.add(choiceIndex);
                    } else {
                        cur.removeIf(x -> x == choiceIndex);
                    }
                    if (cur.isEmpty()) {
                        studentAnswers.remove(index);
                        questionStates.putIfAbsent(index, QState.VISITED);
                    } else {
                        Collections.sort(cur);
                        studentAnswers.put(index, Collections.unmodifiableList(cur));
                        questionStates.put(index, QState.ANSWERED);
                    }
                    refreshGridStyles();
                    updateOverviewCounts();
                });

                VBox wrapper = new VBox(cb);
                wrapper.setPadding(new Insets(6, 0, 0, 0));
                VBox.setVgrow(cb, Priority.NEVER);
                optionsContainer.getChildren().add(wrapper);
            } else {
                ToggleButton tb = new ToggleButton();
                tb.getStyleClass().add("option-button");
                tb.setMaxWidth(Double.MAX_VALUE);
                tb.setToggleGroup(group);
                tb.setUserData(choiceIndex);
                tb.setGraphic(content);

                // restore previous selection
                List<Integer> prevSelected = studentAnswers.get(index);
                if (prevSelected != null && !prevSelected.isEmpty() && prevSelected.get(0) == i) {
                    tb.setSelected(true);
                }

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

                VBox wrapper = new VBox(tb);
                wrapper.setPadding(new Insets(6, 0, 0, 0));
                VBox.setVgrow(tb, Priority.NEVER);
                optionsContainer.getChildren().add(wrapper);
            }
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
            // node may be a wrapper VBox containing a ToggleButton or CheckBox as first child
            if (node instanceof ToggleButton) ((ToggleButton) node).setSelected(false);
            else if (node instanceof CheckBox) ((CheckBox) node).setSelected(false);
            else if (node instanceof VBox) {
                VBox vb = (VBox) node;
                if (!vb.getChildren().isEmpty()) {
                    var child = vb.getChildren().get(0);
                    if (child instanceof ToggleButton) ((ToggleButton) child).setSelected(false);
                    else if (child instanceof CheckBox) ((CheckBox) child).setSelected(false);
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

        if (submitted) return;

        if (timeSeconds <= 0) {
            if (!submitted) calculateAndShowResults();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Terminer l'examen");
        alert.setHeaderText("Voulez-vous vraiment terminer l'examen ?");
        alert.setContentText("Vous ne pourrez plus modifier vos réponses.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (!submitted) calculateAndShowResults();
            } else {
                if (timeSeconds > 0 && timeline != null) timeline.play();
            }
        });
    }

    private void calculateAndShowResults() {
        // Ensure this runs only once
        if (submitted) return;
        submitted = true;
        if (timeline != null) {
            try { timeline.stop(); } catch (Exception ignored) {}
        }

        // No ESC restoration needed; JavaFX defaults apply.

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
        // Attach the result alert to the app Stage and center it over the owner
        try {
            Stage owner = getStage();
            if (owner == null) {
                for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                    if (w instanceof Stage && w.isShowing()) { owner = (Stage) w; break; }
                }
            }
            if (owner != null) {
                resultAlert.initOwner(owner);
                resultAlert.initModality(Modality.WINDOW_MODAL);
                Stage finalOwner = owner;
                resultAlert.setOnShown(evt -> {
                    try {
                        Stage dialogStage = (Stage) resultAlert.getDialogPane().getScene().getWindow();
                        dialogStage.setX(finalOwner.getX() + finalOwner.getWidth() / 2 - dialogStage.getWidth() / 2);
                        dialogStage.setY(finalOwner.getY() + finalOwner.getHeight() / 2 - dialogStage.getHeight() / 2);
                    } catch (Exception ignored) {}
                });
            }
        } catch (Exception ignored) {}
        resultAlert.showAndWait();

        // Persist student and answers (existing logic)
        try {
            database.StudentRepository studentRepo = new database.StudentRepository();
            com.project.models.Student student = new com.project.models.Student(studentName, listNumber, filiere);
            student.setExamId(exam.getId());
            student.setScore(scoreOn20);
            student.setStartTime(startTime);
            student.setEndTime(java.time.LocalDateTime.now());
            student.setWarningCount(0);

            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                List<Integer> userIndices = studentAnswers.getOrDefault(i, Collections.emptyList());
                com.project.models.Answer answer = new com.project.models.Answer(q.getId());
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

    private void displayMedia(VBox container, String mediaPath, MediaType mediaType) {
        container.getChildren().clear();
        if (mediaPath == null || mediaType == null || mediaType == MediaType.NONE) {
            return;
        }

        try {
            if (MediaType.IMAGE.equals(mediaType)) {
                // Créer un bouton pour afficher l'image
                Button showImageButton = new Button("📷 Afficher l'image");
                showImageButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px;");
                showImageButton.setOnAction(e -> showImageModal(mediaPath));
                container.getChildren().add(showImageButton);
            } else if (MediaType.VIDEO.equals(mediaType)) {
                // Créer un bouton pour afficher la vidéo
                Button showVideoButton = new Button("🎥 Afficher la vidéo");
                showVideoButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px;");
                showVideoButton.setOnAction(e -> showVideoModal(mediaPath));
                container.getChildren().add(showVideoButton);
            } else if (MediaType.AUDIO.equals(mediaType)) {
                // Créer un bouton pour afficher l'audio
                Button showAudioButton = new Button("🔊 Écouter l'audio");
                showAudioButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px;");
                showAudioButton.setOnAction(e -> showAudioModal(mediaPath));
                container.getChildren().add(showAudioButton);
            }
        } catch (Exception e) {
            System.err.println("Erreur d'affichage du média: " + e.getMessage());
            Label errorLabel = new Label("Erreur de chargement du média");
            errorLabel.setStyle("-fx-text-fill: red;");
            container.getChildren().add(errorLabel);
        }
    }

    private void showImageModal(String imagePath) {
        try {
            // Créer une nouvelle fenêtre modale
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Image");
            modalStage.setResizable(true);

            // Créer l'ImageView avec zoom
            Image image = new Image(imagePath);
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            // Ajuster la taille initiale de l'image (max 80% de l'écran)
            double maxWidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 0.8;
            double maxHeight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 0.8;

            if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
                double scaleX = maxWidth / image.getWidth();
                double scaleY = maxHeight / image.getHeight();
                double scale = Math.min(scaleX, scaleY);
                imageView.setFitWidth(image.getWidth() * scale);
                imageView.setFitHeight(image.getHeight() * scale);
            }

            // Créer un ScrollPane pour permettre le défilement
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(imageView);
            scrollPane.setPannable(true);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            // Créer les boutons de zoom
            Button zoomInButton = new Button("🔍+");
            Button zoomOutButton = new Button("🔍-");
            Button closeButton = new Button("Fermer");

            zoomInButton.setOnAction(e -> {
                double currentWidth = imageView.getFitWidth();
                double currentHeight = imageView.getFitHeight();
                imageView.setFitWidth(currentWidth * 1.2);
                imageView.setFitHeight(currentHeight * 1.2);
            });

            zoomOutButton.setOnAction(e -> {
                double currentWidth = imageView.getFitWidth();
                double currentHeight = imageView.getFitHeight();
                imageView.setFitWidth(Math.max(currentWidth * 0.9, 100));
                imageView.setFitHeight(Math.max(currentHeight * 0.9, 100));
            });

            closeButton.setOnAction(e -> modalStage.close());

            // Layout des contrôles
            HBox controls = new HBox(10);
            controls.setPadding(new Insets(10));
            controls.setStyle("-fx-background-color: #f0f0f0;");
            controls.getChildren().addAll(zoomInButton, zoomOutButton, closeButton);

            // Layout principal
            VBox layout = new VBox();
            layout.getChildren().addAll(controls, scrollPane);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);

            // Créer la scène
            Scene scene = new Scene(layout, 800, 600);
            modalStage.setScene(scene);
            modalStage.show();

        } catch (Exception e) {
            System.err.println("Erreur d'affichage de l'image modale: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'afficher l'image");
            alert.setContentText("Une erreur s'est produite lors du chargement de l'image.");
            alert.showAndWait();
        }
    }

    private void showVideoModal(String videoPath) {
        try {
            // Créer une nouvelle fenêtre modale
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Vidéo");
            modalStage.setResizable(true);

            // Créer le MediaPlayer et MediaView
            Media media = new Media(videoPath);
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setFitWidth(600);
            mediaView.setPreserveRatio(true);

            // Créer un ScrollPane pour permettre le défilement si nécessaire
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(mediaView);
            scrollPane.setPannable(true);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            // Créer les contrôles de lecture
            Button playButton = new Button("▶ Lecture");
            Button pauseButton = new Button("⏸ Pause");
            Button stopButton = new Button("⏹ Stop");
            Button closeButton = new Button("Fermer");

            playButton.setOnAction(e -> mediaPlayer.play());
            pauseButton.setOnAction(e -> mediaPlayer.pause());
            stopButton.setOnAction(e -> {
                mediaPlayer.stop();
                mediaPlayer.seek(Duration.ZERO);
            });
            closeButton.setOnAction(e -> {
                mediaPlayer.stop();
                modalStage.close();
            });

            // Layout des contrôles
            HBox controls = new HBox(10);
            controls.setPadding(new Insets(10));
            controls.setStyle("-fx-background-color: #f0f0f0;");
            controls.getChildren().addAll(playButton, pauseButton, stopButton, closeButton);

            // Layout principal
            VBox layout = new VBox();
            layout.getChildren().addAll(controls, scrollPane);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);

            // Créer la scène
            Scene scene = new Scene(layout, 650, 500);
            modalStage.setScene(scene);
            modalStage.show();

        } catch (Exception e) {
            System.err.println("Erreur d'affichage de la vidéo modale: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'afficher la vidéo");
            alert.setContentText("Une erreur s'est produite lors du chargement de la vidéo.");
            alert.showAndWait();
        }
    }

    private void showAudioModal(String audioPath) {
        try {
            // Créer une nouvelle fenêtre modale
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Audio");
            modalStage.setResizable(false);

            // Créer le MediaPlayer
            Media media = new Media(audioPath);
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            // Créer les contrôles de lecture
            Label titleLabel = new Label("🔊 Lecture audio");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            Button playButton = new Button("▶ Lecture");
            Button pauseButton = new Button("⏸ Pause");
            Button stopButton = new Button("⏹ Stop");
            Button closeButton = new Button("Fermer");

            playButton.setOnAction(e -> mediaPlayer.play());
            pauseButton.setOnAction(e -> mediaPlayer.pause());
            stopButton.setOnAction(e -> {
                mediaPlayer.stop();
                mediaPlayer.seek(Duration.ZERO);
            });
            closeButton.setOnAction(e -> {
                mediaPlayer.stop();
                modalStage.close();
            });

            // Layout des contrôles
            HBox controls = new HBox(10);
            controls.setPadding(new Insets(10));
            controls.setStyle("-fx-background-color: #f0f0f0;");
            controls.getChildren().addAll(playButton, pauseButton, stopButton, closeButton);

            // Layout principal
            VBox layout = new VBox(20);
            layout.setPadding(new Insets(20));
            layout.getChildren().addAll(titleLabel, controls);

            // Créer la scène
            Scene scene = new Scene(layout, 400, 150);
            modalStage.setScene(scene);
            modalStage.show();

        } catch (Exception e) {
            System.err.println("Erreur d'affichage de l'audio modale: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'écouter l'audio");
            alert.setContentText("Une erreur s'est produite lors du chargement de l'audio.");
            alert.showAndWait();
        }
    }

    private void displayChoiceMedia(VBox container, String mediaPath, MediaType mediaType) {
        try {
            if (MediaType.IMAGE.equals(mediaType)) {
                // Créer un bouton pour afficher l'image
                Button showImageButton = new Button("📷 Voir image");
                showImageButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;");
                showImageButton.setOnAction(e -> showImageModal(mediaPath));
                container.getChildren().add(showImageButton);
            } else if (MediaType.VIDEO.equals(mediaType)) {
                // Créer un bouton pour afficher la vidéo
                Button showVideoButton = new Button("🎥 Voir vidéo");
                showVideoButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;");
                showVideoButton.setOnAction(e -> showVideoModal(mediaPath));
                container.getChildren().add(showVideoButton);
            } else if (MediaType.AUDIO.equals(mediaType)) {
                // Créer un bouton pour afficher l'audio
                Button showAudioButton = new Button("🔊 Écouter");
                showAudioButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;");
                showAudioButton.setOnAction(e -> showAudioModal(mediaPath));
                container.getChildren().add(showAudioButton);
            }
        } catch (Exception e) {
            System.err.println("Erreur d'affichage du média pour le choix: " + e.getMessage());
            Label errorLabel = new Label("Erreur média");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
            container.getChildren().add(errorLabel);
        }
    }

    private Stage getStage() {
        try {
            if (submitButton != null && submitButton.getScene() != null) return (Stage) submitButton.getScene().getWindow();
            if (btnReviewSubmit != null && btnReviewSubmit.getScene() != null) return (Stage) btnReviewSubmit.getScene().getWindow();
            if (questionGrid != null && questionGrid.getScene() != null) return (Stage) questionGrid.getScene().getWindow();
            if (timerLabel != null && timerLabel.getScene() != null) return (Stage) timerLabel.getScene().getWindow();
        } catch (Exception ignored) {}
        return null;
    }

    // Disable ESC via stage full-screen exit override while exam is displayed.
    private void disableEscInExam(Stage stage) {
        if (stage == null) return;
        try {
            if (prevFullScreenExitKeyCombination == null) {
                prevFullScreenExitKeyCombination = stage.getFullScreenExitKeyCombination();
            }
            escStage = stage;
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
            System.out.println("[ExamController] disableEscInExam: stage.isFullScreen=" + stage.isFullScreen() + ", prevExit=" + prevFullScreenExitKeyCombination);

            // Fallback: if the stage is not in fullscreen OR ESC still triggers, attach a
            // simple scene-level EventFilter to the current Scene (single, non-fragile attach).
            try {
                Scene sc = stage.getScene();
                if (sc != null) {
                    if (simpleEscBlocker == null) {
                        simpleEscBlocker = evt -> { if (evt.getCode() == KeyCode.ESCAPE) evt.consume(); };
                    }
                    if (sceneWithSimpleBlocker == null) {
                        sc.addEventFilter(KeyEvent.KEY_PRESSED, simpleEscBlocker);
                        sceneWithSimpleBlocker = sc;
                    }
                }
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    // Restore ESC behavior when leaving the exam.
    private void restoreEscAfterExam(Stage stage) {
        try {
            Stage s = (escStage != null) ? escStage : stage;
            if (s != null) {
                if (prevFullScreenExitKeyCombination != null) {
                    s.setFullScreenExitKeyCombination(prevFullScreenExitKeyCombination);
                } else {
                    s.setFullScreenExitKeyCombination(KeyCombination.keyCombination("ESCAPE"));
                }
                s.setFullScreenExitHint("");
            }
        } catch (Exception ignored) {}
        // Remove any scene-level blocker we added as fallback
        try {
            if (sceneWithSimpleBlocker != null && simpleEscBlocker != null) {
                try { sceneWithSimpleBlocker.removeEventFilter(KeyEvent.KEY_PRESSED, simpleEscBlocker); } catch (Exception ignored) {}
                sceneWithSimpleBlocker = null;
                simpleEscBlocker = null;
            }
        } catch (Exception ignored) {}

        escStage = null;
        prevFullScreenExitKeyCombination = null;
    }

    // Remove the previously-installed ESC filter if present.
    // (removed EventFilter-based detach; use restoreEscAfterExam(stage) instead)

    // Helper: try to find a Scene that belongs to this controller's UI
    // (removed EventFilter-based helpers; stage-level approach used instead)


   private void returnToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();

            // try multiple known controls to obtain the current window
            Stage stage = null;
            if (submitButton != null && submitButton.getScene() != null) stage = (Stage) submitButton.getScene().getWindow();
            if (stage == null && btnReviewSubmit != null && btnReviewSubmit.getScene() != null) stage = (Stage) btnReviewSubmit.getScene().getWindow();
            if (stage == null && questionGrid != null && questionGrid.getScene() != null) stage = (Stage) questionGrid.getScene().getWindow();
            if (stage == null && timerLabel != null && timerLabel.getScene() != null) stage = (Stage) timerLabel.getScene().getWindow();

            if (stage != null) {
                Scene scene = stage.getScene();
                if (scene != null && scene.getRoot() != null) {
                    Node currentRoot = scene.getRoot();

                    // capture stage in a final variable so it can be referenced inside the lambda
                    final Stage finalStage = stage;

                    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(event -> {
                        // Replace root on the existing scene to preserve fullscreen and styles
                        scene.setRoot(root);

                        // Fade in the new root
                        root.setOpacity(0.0);
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();

                        // Preserve window state
                        finalStage.setFullScreen(true);
                        finalStage.setTitle("Système de Gestion d'Examens QCM");

                        // No custom ESC/state restoration — rely on JavaFX defaults
                    });
                    fadeOut.play();
                } else {
                    // Fallback: create a new scene if none exists
                    Scene newScene = new Scene(root);
                    stage.setScene(newScene);
                    stage.setFullScreen(true);
                    stage.setTitle("Système de Gestion d'Examens QCM");
                }
            } else {
                // If we couldn't find an existing stage, open a new one instead of exiting the app
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.setTitle("Système de Gestion d'Examens QCM");
                newStage.setFullScreen(true);
                newStage.setFullScreenExitHint("");
                newStage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void returnToJoinExam() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/join_exam.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) (submitButton != null ? submitButton.getScene().getWindow() : null);
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("Rejoindre un Examen");
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
            } else {
                Platform.exit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}