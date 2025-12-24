package com.project.models;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class Answer {
    private ObjectId questionId;
    private List<Integer> selectedChoiceIndices; // Indices des choix sélectionnés
    
    public Answer() {
        this.selectedChoiceIndices = new ArrayList<>();
    }
    
    public Answer(ObjectId questionId) {
        this();
        this.questionId = questionId;
    }
    
    public Document toDocument() {
        return new Document()
            .append("questionId", questionId)
            .append("selectedChoiceIndices", selectedChoiceIndices);
    }
    
    public static Answer fromDocument(Document doc) {
        Answer answer = new Answer();
        answer.setQuestionId(doc.getObjectId("questionId"));
        List<Integer> indices = doc.getList("selectedChoiceIndices", Integer.class);
        if (indices != null) {
            answer.setSelectedChoiceIndices(indices);
        }
        return answer;
    }
    
    // Getters and Setters
    public ObjectId getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(ObjectId questionId) {
        this.questionId = questionId;
    }
    
    public List<Integer> getSelectedChoiceIndices() {
        return selectedChoiceIndices;
    }
    
    public void setSelectedChoiceIndices(List<Integer> selectedChoiceIndices) {
        this.selectedChoiceIndices = selectedChoiceIndices;
    }
}
