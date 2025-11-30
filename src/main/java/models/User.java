package models;

import org.bson.Document;
import org.bson.types.ObjectId;

public class User {
    private ObjectId id;
    private String name;
    private String email;
    private String role; // "PROFESSOR" or "STUDENT"
    
    public User() {
    }
    
    public User(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }
    
    public Document toDocument() {
        Document doc = new Document();
        if (id != null) doc.append("_id", id);
        doc.append("name", name)
           .append("email", email)
           .append("role", role);
        return doc;
    }
    
    public static User fromDocument(Document doc) {
        User user = new User();
        if (doc.containsKey("_id")) {
            user.setId(doc.getObjectId("_id"));
        }
        user.setName(doc.getString("name"));
        user.setEmail(doc.getString("email"));
        user.setRole(doc.getString("role"));
        return user;
    }
    
    // Getters and Setters
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
}
