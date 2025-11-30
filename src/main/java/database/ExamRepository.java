package database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import models.Exam;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class ExamRepository {
    private MongoCollection<Document> collection;

    public ExamRepository() {
        MongoDatabase db = MongoConnection.getInstance().getDatabase();
        if (db != null) {
            this.collection = db.getCollection("exams");
        } else {
            System.err.println("Database connection is null. ExamRepository will not work correctly.");
        }
    }

    public void save(Exam exam) {
        // Générer un code unique si nécessaire
        if (exam.getExamId() == null || exam.getExamId().isEmpty()) {
            String examId;
            do {
                examId = generateUniqueExamId();
            } while (examIdExists(examId));
            exam.setExamId(examId);
        }

        Document doc = exam.toDocument();
        collection.insertOne(doc);
        if (doc.containsKey("_id")) {
            exam.setId(doc.getObjectId("_id"));
        }
    }

    private String generateUniqueExamId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public Exam findByExamId(String examId) {
        Document doc = collection.find(Filters.eq("examId", examId)).first();
        if (doc != null) {
            return Exam.fromDocument(doc);
        }
        return null;
    }

    public Exam findById(ObjectId id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        if (doc != null) {
            return Exam.fromDocument(doc);
        }
        return null;
    }

    public List<Exam> findByProfessorId(ObjectId professorId) {
        List<Exam> exams = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("professorId", professorId))) {
            exams.add(Exam.fromDocument(doc));
        }
        return exams;
    }

    public List<Exam> findAllActive() {
        List<Exam> exams = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("isActive", true))) {
            exams.add(Exam.fromDocument(doc));
        }
        return exams;
    }

    public void update(Exam exam) {
        collection.replaceOne(
                Filters.eq("_id", exam.getId()),
                exam.toDocument());
    }

    public void delete(ObjectId id) {
        collection.deleteOne(Filters.eq("_id", id));
    }

    public boolean examIdExists(String examId) {
        return collection.find(Filters.eq("examId", examId)).first() != null;
    }
}
