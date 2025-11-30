package models;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class ProctorEvent {
    private ObjectId id;
    private ObjectId attemptId;
    private String eventType; // "CAMERA_OFF", "FOCUS_LOST", "SCREEN_SHARE", "WRITING_DETECTED", "LOOKING_AWAY"
    private LocalDateTime timestamp;
    private String description;
    
    public ProctorEvent() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ProctorEvent(ObjectId attemptId, String eventType, String description) {
        this();
        this.attemptId = attemptId;
        this.eventType = eventType;
        this.description = description;
    }
    
    public Document toDocument() {
        Document doc = new Document();
        if (id != null) doc.append("_id", id);
        doc.append("attemptId", attemptId)
           .append("eventType", eventType)
           .append("description", description);
        
        if (timestamp != null) {
            doc.append("timestamp", Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        return doc;
    }
    
    public static ProctorEvent fromDocument(Document doc) {
        ProctorEvent event = new ProctorEvent();
        if (doc.containsKey("_id")) {
            event.setId(doc.getObjectId("_id"));
        }
        event.setAttemptId(doc.getObjectId("attemptId"));
        event.setEventType(doc.getString("eventType"));
        event.setDescription(doc.getString("description"));
        
        Date timestampDate = doc.getDate("timestamp");
        if (timestampDate != null) {
            event.setTimestamp(LocalDateTime.ofInstant(timestampDate.toInstant(), ZoneId.systemDefault()));
        }
        
        return event;
    }
    
    // Getters and Setters
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public ObjectId getAttemptId() {
        return attemptId;
    }
    
    public void setAttemptId(ObjectId attemptId) {
        this.attemptId = attemptId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
