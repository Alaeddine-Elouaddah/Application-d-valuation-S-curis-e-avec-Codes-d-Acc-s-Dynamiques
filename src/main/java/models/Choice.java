package models;

import org.bson.Document;

public class Choice {
    private String text;
    private boolean isCorrect;
    
    public Choice() {
    }
    
    public Choice(String text, boolean isCorrect) {
        this.text = text;
        this.isCorrect = isCorrect;
    }
    
    public Document toDocument() {
        return new Document()
            .append("text", text)
            .append("isCorrect", isCorrect);
    }
    
    public static Choice fromDocument(Document doc) {
        Choice choice = new Choice();
        choice.setText(doc.getString("text"));
        choice.setCorrect(doc.getBoolean("isCorrect", false));
        return choice;
    }
    
    // Getters and Setters
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public boolean isCorrect() {
        return isCorrect;
    }
    
    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}
