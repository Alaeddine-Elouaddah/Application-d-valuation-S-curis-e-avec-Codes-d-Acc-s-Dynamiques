package database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import models.User;
import org.bson.Document;
import org.bson.types.ObjectId;

public class UserRepository {
    private MongoCollection<Document> collection;
    
    public UserRepository() {
        this.collection = MongoConnection.getInstance()
            .getDatabase()
            .getCollection("users");
    }
    
    public void save(User user) {
        Document doc = user.toDocument();
        collection.insertOne(doc);
        if (doc.containsKey("_id")) {
            user.setId(doc.getObjectId("_id"));
        }
    }
    
    public User findById(ObjectId id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        if (doc != null) {
            return User.fromDocument(doc);
        }
        return null;
    }
    
    public User findByEmail(String email) {
        Document doc = collection.find(Filters.eq("email", email)).first();
        if (doc != null) {
            return User.fromDocument(doc);
        }
        return null;
    }
    
    public void update(User user) {
        collection.replaceOne(
            Filters.eq("_id", user.getId()),
            user.toDocument()
        );
    }
    
    public boolean emailExists(String email) {
        return collection.find(Filters.eq("email", email)).first() != null;
    }
}
