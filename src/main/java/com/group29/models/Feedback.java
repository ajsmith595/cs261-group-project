package com.group29.models;

import java.util.List;

public class Feedback {
    private String id;
    private String userID;
    private boolean anonymous;
    private List<Response> responses;

    public Feedback(String id, String userID, boolean anonymous, List<Response> responses) {
        this.id = id;
        this.userID = userID;
        this.anonymous = anonymous;
        this.responses = responses;
    }
}