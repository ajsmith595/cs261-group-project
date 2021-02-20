package com.group29.models;

public class Response {
    private String id;
    private String questionId;
    private String response;

    public Response(String id, String questionId, String response) {
        this.id = id;
        this.questionId = questionId;
        this.response = response;
    }
}
