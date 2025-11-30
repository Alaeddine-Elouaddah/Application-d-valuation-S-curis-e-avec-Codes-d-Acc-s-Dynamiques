package models;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class Question {
    private ObjectId id;
    private String text;
    private List<Choice> choices;
    private int maxAnswers; // 1 pour une seule réponse, 2 pour deux réponses
    
    public Question() {
        this.choices = new ArrayList<>();
    }
    
    public Question(String text, int maxAnswers) {
        this.text = text;
        this.maxAnswers = maxAnswers;
        this.choices = new ArrayList<>();
    }
    
    public Document toDocument() {
        Document doc = new Document();
        if (id != null) doc.append("_id", id);
        doc.append("text", text)
           .append("maxAnswers", maxAnswers);
        
        List<Document> choicesDoc = new ArrayList<>();
        for (Choice choice : choices) {
            choicesDoc.add(choice.toDocument());
        }
        doc.append("choices", choicesDoc);
        
        return doc;
    }
    
    public static Question fromDocument(Document doc) {
        Question question = new Question();
        if (doc.containsKey("_id")) {
            question.setId(doc.getObjectId("_id"));
        }
        question.setText(doc.getString("text"));
        question.setMaxAnswers(doc.getInteger("maxAnswers", 1));
        
        List<Document> choicesDoc = doc.getList("choices", Document.class);
        if (choicesDoc != null) {
            for (Document choiceDoc : choicesDoc) {
                question.getChoices().add(Choice.fromDocument(choiceDoc));
            }
        }
        
        return question;
    }
    
    // Getters and Setters
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public List<Choice> getChoices() {
        return choices;
    }
    
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
    
    public int getMaxAnswers() {
        return maxAnswers;
    }
    
    public void setMaxAnswers(int maxAnswers) {
        this.maxAnswers = maxAnswers;
    }
}
