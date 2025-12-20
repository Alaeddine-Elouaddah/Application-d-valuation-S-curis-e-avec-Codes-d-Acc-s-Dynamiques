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
        if (examId == null || examId.isEmpty()) {
            System.err.println("❌ findByExamId: examId is null or empty");
            return null;
        }
        
        String searchCode = examId.trim().toUpperCase();
        System.out.println("🔍 Searching for exam with code: " + searchCode);
        
        // Essayer d'abord avec la casse exacte
        Document doc = collection.find(Filters.eq("examId", searchCode)).first();
        
        // Si pas trouvé, chercher en minuscules
        if (doc == null) {
            doc = collection.find(Filters.eq("examId", searchCode.toLowerCase())).first();
        }
        
        // Si toujours pas trouvé, chercher tous les exams avec regex insensible à la casse
        if (doc == null) {
            System.out.println("📋 Fetching all exams to find match...");
            for (Document d : collection.find()) {
                Object examIdObj = d.get("examId");
                if (examIdObj != null) {
                    String dbExamId = examIdObj.toString().trim().toUpperCase();
                    System.out.println("   Found exam code in DB: " + dbExamId);
                    if (dbExamId.equals(searchCode)) {
                        doc = d;
                        break;
                    }
                }
            }
        }
        
        if (doc != null) {
            System.out.println("✅ Exam found: " + doc.get("title"));
            return Exam.fromDocument(doc);
        }
        
        System.err.println("❌ No exam found with code: " + searchCode);
        return null;
    }

    public Exam findById(ObjectId id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        if (doc != null) {
            return Exam.fromDocument(doc);
        }
        return null;
    }

    public Exam findByProfessorCode(String professorCode) {
        Document doc = collection.find(Filters.eq("professorCode", professorCode)).first();
        if (doc != null) {
            return Exam.fromDocument(doc);
        }
        return null;
    }

    public List<Exam> findByProfessorId(String professorId) {
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

    public List<Exam> findAll() {
        List<Exam> exams = new ArrayList<>();
        for (Document doc : collection.find()) {
            exams.add(Exam.fromDocument(doc));
        }
        return exams;
    }

    public boolean examIdExists(String examId) {
        return collection.find(Filters.eq("examId", examId)).first() != null;
    }
}
