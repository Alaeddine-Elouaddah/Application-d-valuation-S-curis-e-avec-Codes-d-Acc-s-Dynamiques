package models;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Attempt {
    private ObjectId id;
    private ObjectId examId;
    private ObjectId studentId;
    private List<Answer> answers;
    private double score;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int warningCount; // Nombre d'avertissements de triche
    
    public Attempt() {
        this.answers = new ArrayList<>();
        this.warningCount = 0;
    }
    
    public Attempt(ObjectId examId, ObjectId studentId) {
        this();
        this.examId = examId;
        this.studentId = studentId;
        this.startTime = LocalDateTime.now();
    }
    
    public Document toDocument() {
        Document doc = new Document();
        if (id != null) doc.append("_id", id);
        doc.append("examId", examId)
           .append("studentId", studentId)
           .append("score", score)
           .append("warningCount", warningCount);
        
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
        
        return doc;
    }
    
    public static Attempt fromDocument(Document doc) {
        Attempt attempt = new Attempt();
        if (doc.containsKey("_id")) {
            attempt.setId(doc.getObjectId("_id"));
        }
        attempt.setExamId(doc.getObjectId("examId"));
        attempt.setStudentId(doc.getObjectId("studentId"));
        attempt.setScore(doc.getDouble("score"));
        attempt.setWarningCount(doc.getInteger("warningCount", 0));
        
        List<Document> answersDoc = doc.getList("answers", Document.class);
        if (answersDoc != null) {
            for (Document answerDoc : answersDoc) {
                attempt.getAnswers().add(Answer.fromDocument(answerDoc));
            }
        }
        
        Date startTimeDate = doc.getDate("startTime");
        if (startTimeDate != null) {
            attempt.setStartTime(LocalDateTime.ofInstant(startTimeDate.toInstant(), ZoneId.systemDefault()));
        }
        
        Date endTimeDate = doc.getDate("endTime");
        if (endTimeDate != null) {
            attempt.setEndTime(LocalDateTime.ofInstant(endTimeDate.toInstant(), ZoneId.systemDefault()));
        }
        
        return attempt;
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
    
    public ObjectId getStudentId() {
        return studentId;
    }
    
    public void setStudentId(ObjectId studentId) {
        this.studentId = studentId;
    }
    
    public List<Answer> getAnswers() {
        return answers;
    }
    
    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = score;
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
    
    public int getWarningCount() {
        return warningCount;
    }
    
    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }
    
    public void incrementWarning() {
        this.warningCount++;
    }
}
