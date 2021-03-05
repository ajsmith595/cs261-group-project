package com.group29.models.questiondata;

public class QuestionResponse {
    protected String message;
    protected String username;
    protected String id;

    public QuestionResponse(String message, String username, String id) {
        this.message = message;
        this.username = username;
        this.id = id;
    }

    public String getMessage() {
        return this.message;
    }

    public String getUsername() {
        return this.username;
    }

    public String getID() {
        return this.id;
    }
}
