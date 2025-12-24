package database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.project.models.Student;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class StudentRepository {
    private MongoCollection<Document> collection;

    public StudentRepository() {
        MongoDatabase db = MongoConnection.getInstance().getDatabase();
        if (db != null) {
            this.collection = db.getCollection("students");
        } else {
            System.err.println("Database connection is null. StudentRepository will not work correctly.");
        }
    }

    public void save(Student student) {
        Document doc = student.toDocument();
        collection.insertOne(doc);
        if (doc.containsKey("_id")) {
            student.setId(doc.getObjectId("_id"));
        }
    }

    public Student findById(ObjectId id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        if (doc != null) {
            return Student.fromDocument(doc);
        }
        return null;
    }

    public List<Student> findByName(String name) {
        List<Student> students = new ArrayList<>();
        for (Document doc : collection.find(Filters.regex("fullName", name, "i"))) {
            students.add(Student.fromDocument(doc));
        }
        return students;
    }

    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        for (Document doc : collection.find()) {
            students.add(Student.fromDocument(doc));
        }
        return students;
    }

    public void update(Student student) {
        collection.replaceOne(
                Filters.eq("_id", student.getId()),
                student.toDocument());
    }

    public List<Student> findByExamId(ObjectId examId) {
        List<Student> students = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("examId", examId))) {
            students.add(Student.fromDocument(doc));
        }
        return students;
    }

    public void delete(ObjectId id) {
        collection.deleteOne(Filters.eq("_id", id));
    }
}
