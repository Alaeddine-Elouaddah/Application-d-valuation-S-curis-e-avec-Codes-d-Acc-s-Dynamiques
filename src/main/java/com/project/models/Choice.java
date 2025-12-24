package com.project.models;

import org.bson.Document;

public class Choice {
    private String text;
    private boolean isCorrect;
    private String mediaPath;
    private MediaType mediaType;
    
    public Choice() {
    }
    
    public Choice(String text, boolean isCorrect) {
        this.text = text;
        this.isCorrect = isCorrect;
    }

    public Choice(String text, boolean isCorrect, String mediaPath, MediaType mediaType) {
        this.text = text;
        this.isCorrect = isCorrect;
        this.mediaPath = mediaPath;
        this.mediaType = mediaType;
    }
    
    public Document toDocument() {
        return new Document()
            .append("text", text)
            .append("isCorrect", isCorrect)
            .append("mediaPath", mediaPath)
            .append("mediaType", mediaType != null ? mediaType.name() : null);
    }
    
    public static Choice fromDocument(Document doc) {
        Choice choice = new Choice();
        choice.setText(doc.getString("text"));
        choice.setCorrect(doc.getBoolean("isCorrect", false));
        choice.setMediaPath(doc.getString("mediaPath"));
        String mediaTypeStr = doc.getString("mediaType");
        choice.setMediaType(mediaTypeStr != null ? MediaType.valueOf(mediaTypeStr) : null);
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

    public String getMediaPath() {
        return mediaPath;
    }
    
    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }
    
    public MediaType getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }
}
