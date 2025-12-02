package models;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Exam {
    private ObjectId id;
    private String examId; // ID généré automatiquement pour rejoindre l'examen
    private String title;
    private String description;
    private String professorId;
    private String professorCode; // Code utilisé par le professeur pour consulter les résultats
    private List<ObjectId> questionIds;
    private int durationMinutes; // Durée en minutes spécifiée par le prof
    private LocalDateTime createdAt;
    private boolean isActive;

    public Exam() {
        this.questionIds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    public Exam(String title, String description, String professorId, int durationMinutes) {
        this();
        this.title = title;
        this.description = description;
        this.professorId = professorId;
        this.durationMinutes = durationMinutes;
        this.examId = generateExamId();
    }

    private String generateExamId() {
        // Génère un ID aléatoire de 6 caractères (lettres et chiffres)
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public Document toDocument() {
        Document doc = new Document();
        if (id != null)
            doc.append("_id", id);
        doc.append("examId", examId)
                .append("title", title)
                .append("description", description)
                .append("professorId", professorId)
                .append("durationMinutes", durationMinutes)
                .append("isActive", isActive);

        if (professorCode != null) {
            doc.append("professorCode", professorCode);
        }

        if (questionIds != null && !questionIds.isEmpty()) {
            doc.append("questionIds", questionIds);
        }

        if (createdAt != null) {
            doc.append("createdAt", Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()));
        }

        return doc;
    }

    public static Exam fromDocument(Document doc) {
        Exam exam = new Exam();
        if (doc.containsKey("_id")) {
            exam.setId(doc.getObjectId("_id"));
        }
        exam.setExamId(doc.getString("examId"));
        exam.setTitle(doc.getString("title"));
        exam.setDescription(doc.getString("description"));
        exam.setProfessorId(doc.getString("professorId"));
        exam.setDurationMinutes(doc.getInteger("durationMinutes", 60));
        exam.setActive(doc.getBoolean("isActive", true));

        exam.setProfessorCode(doc.getString("professorCode"));

        List<ObjectId> questionIds = doc.getList("questionIds", ObjectId.class);
        if (questionIds != null) {
            exam.setQuestionIds(questionIds);
        }

        Date createdAtDate = doc.getDate("createdAt");
        if (createdAtDate != null) {
            exam.setCreatedAt(LocalDateTime.ofInstant(createdAtDate.toInstant(), ZoneId.systemDefault()));
        }

        return exam;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfessorId() {
        return professorId;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }

    public String getProfessorCode() {
        return professorCode;
    }

    public void setProfessorCode(String professorCode) {
        this.professorCode = professorCode;
    }

    public List<ObjectId> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<ObjectId> questionIds) {
        this.questionIds = questionIds;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
