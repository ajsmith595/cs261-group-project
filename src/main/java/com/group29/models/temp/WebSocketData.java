package com.group29.models.temp;

public class WebSocketData {
    protected Question[] questions;
    protected int total_responses;
    protected int total_users;
    protected int mins_left;

    public WebSocketData(Question[] questions, int total_responses, int total_users, int mins_left) {
        this.questions = questions;
        this.total_responses = total_responses;
        this.total_users = total_users;
        this.mins_left = mins_left;
    }

    public Question[] getQuestions() {
        return this.questions;
    }
}
