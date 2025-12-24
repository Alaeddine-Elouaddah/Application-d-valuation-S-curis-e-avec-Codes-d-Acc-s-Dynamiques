package com.project.models;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Student {
    private ObjectId id;
    private ObjectId examId;
    private String studentName;
    private String studentListNumber;
    private String studentFiliere;
    private double score;
    private int warningCount;
    private List<Answer> answers;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;

    public Student() {
        this.answers = new ArrayList<>();
        this.warningCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Student(String studentName, String studentListNumber, String studentFiliere) {
        this();
        this.studentName = studentName;
        this.studentListNumber = studentListNumber;
        this.studentFiliere = studentFiliere;
    }

    public Document toDocument() {
        Document doc = new Document();
        if (id != null)
            doc.append("_id", id);

        doc.append("examId", examId)
                .append("studentName", studentName != null ? studentName : "")
                .append("studentListNumber", studentListNumber != null ? studentListNumber : "")
                .append("studentFiliere", studentFiliere != null ? studentFiliere : "")
                .append("score", score)
                .append("warningCount", warningCount);

        // Sauvegarder les réponses
        List<Document> answersDoc = new ArrayList<>();
        for (Answer answer : answers) {
            answersDoc.add(answer.toDocument());
        }
        doc.append("answers", answersDoc);

        if (startTime != null) {
            doc.append("startTime", Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));
        }

        if (endTime != null) {
            doc.append("endTime", Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()));
        }

        if (createdAt != null) {
            doc.append("createdAt", Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()));
        }

        return doc;
    }

    public static Student fromDocument(Document doc) {
        Student student = new Student();
        if (doc.containsKey("_id")) {
            student.setId(doc.getObjectId("_id"));
        }
        student.setExamId(doc.getObjectId("examId"));
        student.setStudentName(doc.getString("studentName"));
        student.setStudentListNumber(doc.getString("studentListNumber"));
        student.setStudentFiliere(doc.getString("studentFiliere"));
        student.setScore(doc.getDouble("score"));
        student.setWarningCount(doc.getInteger("warningCount", 0));

        // Charger les réponses
        List<Document> answersDoc = doc.getList("answers", Document.class);
        if (answersDoc != null) {
            for (Document answerDoc : answersDoc) {
                student.getAnswers().add(Answer.fromDocument(answerDoc));
            }
        }

        Date startTimeDate = doc.getDate("startTime");
        if (startTimeDate != null) {
            student.setStartTime(LocalDateTime.ofInstant(startTimeDate.toInstant(), ZoneId.systemDefault()));
        }

        Date endTimeDate = doc.getDate("endTime");
        if (endTimeDate != null) {
            student.setEndTime(LocalDateTime.ofInstant(endTimeDate.toInstant(), ZoneId.systemDefault()));
        }

        Date createdAtDate = doc.getDate("createdAt");
        if (createdAtDate != null) {
            student.setCreatedAt(LocalDateTime.ofInstant(createdAtDate.toInstant(), ZoneId.systemDefault()));
        }

        return student;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getExamId() {
        return examId;
    }

    public void setExamId(ObjectId examId) {
        this.examId = examId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentListNumber() {
        return studentListNumber;
    }

    public void setStudentListNumber(String studentListNumber) {
        this.studentListNumber = studentListNumber;
    }

    public String getStudentFiliere() {
        return studentFiliere;
    }

    public void setStudentFiliere(String studentFiliere) {
        this.studentFiliere = studentFiliere;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
