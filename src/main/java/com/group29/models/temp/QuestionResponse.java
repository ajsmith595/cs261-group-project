package com.group29.models.temp;

public class QuestionResponse {
    protected String message;
    protected String username;
    protected String id;

    public QuestionResponse(String message, String username, String id) {
        this.message = message;
        this.username = username;
        this.id = id;
    }
}
