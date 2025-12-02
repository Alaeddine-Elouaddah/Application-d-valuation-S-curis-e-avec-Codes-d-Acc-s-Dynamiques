package models;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO pour afficher les résultats des étudiants dans le TableView
 */
public class StudentResult {
    private final SimpleStringProperty studentName;
    private final SimpleStringProperty listNumber;
    private final SimpleStringProperty filiere;
    private final SimpleDoubleProperty score;
    private final SimpleStringProperty examTitle;
    private final SimpleStringProperty examDate;

    public StudentResult(String studentName, String listNumber, String filiere,
            double score, String examTitle, LocalDateTime examDate) {
        this.studentName = new SimpleStringProperty(studentName);
        this.listNumber = new SimpleStringProperty(listNumber != null ? listNumber : "N/A");
        this.filiere = new SimpleStringProperty(filiere);
        this.score = new SimpleDoubleProperty(score);
        this.examTitle = new SimpleStringProperty(examTitle);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        this.examDate = new SimpleStringProperty(examDate != null ? examDate.format(formatter) : "N/A");
    }

    // Getters pour JavaFX Properties
    public String getStudentName() {
        return studentName.get();
    }

    public SimpleStringProperty studentNameProperty() {
        return studentName;
    }

    public String getListNumber() {
        return listNumber.get();
    }

    public SimpleStringProperty listNumberProperty() {
        return listNumber;
    }

    public String getFiliere() {
        return filiere.get();
    }

    public SimpleStringProperty filiereProperty() {
        return filiere;
    }

    public double getScore() {
        return score.get();
    }

    public SimpleDoubleProperty scoreProperty() {
        return score;
    }

    public String getExamTitle() {
        return examTitle.get();
    }

    public SimpleStringProperty examTitleProperty() {
        return examTitle;
    }

    public String getExamDate() {
        return examDate.get();
    }

    public SimpleStringProperty examDateProperty() {
        return examDate;
    }
}
