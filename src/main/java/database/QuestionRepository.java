package database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.project.models.Question;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class QuestionRepository {
    private MongoCollection<Document> collection;

    public QuestionRepository() {
        MongoDatabase db = MongoConnection.getInstance().getDatabase();
        if (db != null) {
            this.collection = db.getCollection("questions");
        } else {
            System.err.println("Database connection is null. QuestionRepository will not work correctly.");
        }
    }

    public void save(Question question) {
        Document doc = question.toDocument();
        collection.insertOne(doc);
        if (doc.containsKey("_id")) {
            question.setId(doc.getObjectId("_id"));
        }
    }

    public Question findById(ObjectId id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        if (doc != null) {
            return Question.fromDocument(doc);
        }
        return null;
    }

    public List<Question> findAll() {
        List<Question> questions = new ArrayList<>();
        try {
            if (collection != null) {
                for (Document doc : collection.find()) {
                    Question question = Question.fromDocument(doc);
                    if (question != null) {
                        questions.add(question);
                    }
                }
            } else {
                System.err.println("Collection is null in QuestionRepository.findAll()");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture des questions: " + e.getMessage());
            e.printStackTrace();
        }
        return questions;
    }

    public List<Question> findByIds(List<ObjectId> ids) {
        List<Question> questions = new ArrayList<>();
        for (ObjectId id : ids) {
            Question q = findById(id);
            if (q != null) {
                questions.add(q);
            }
        }
        return questions;
    }

    public void saveAll(List<Question> questions) {
        List<Document> docs = new ArrayList<>();
        for (Question question : questions) {
            docs.add(question.toDocument());
        }
        if (!docs.isEmpty()) {
            collection.insertMany(docs);
            // Mettre à jour les IDs
            for (int i = 0; i < questions.size(); i++) {
                if (docs.get(i).containsKey("_id")) {
                    questions.get(i).setId(docs.get(i).getObjectId("_id"));
                }
            }
        }
    }

    public void update(Question question) {
        collection.replaceOne(
                Filters.eq("_id", question.getId()),
                question.toDocument());
    }

    public void delete(ObjectId id) {
        collection.deleteOne(Filters.eq("_id", id));
    }
}