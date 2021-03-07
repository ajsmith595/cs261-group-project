package com.group29.models.questiondata;

public class WebSocketData {
    protected Question[] questions;
    protected int totalResponses;
    protected int totalUsers;
    protected int minsLeft;
    protected String title;

    /**
     * Constructs a WebSococketData class
     * 
     * @param questions      The questions being sent
     * @param totalResponses The amount of responses for the event
     * @param totalUsers     The amount of unique users sending feedback for the
     *                       event
     * @param minsLeft       Amount of time for the event left
     * @param title          The title of the event
     */
    public WebSocketData(Question[] questions, int totalResponses, int totalUsers, int minsLeft, String title) {
        this.questions = questions;
        this.totalResponses = totalResponses;
        this.totalUsers = totalUsers;
        this.minsLeft = minsLeft;
        this.title = title;
    }

    /**
     * Gets the questions from the object
     * 
     * @return An array of the questions
     */
    public Question[] getQuestions() {
        return this.questions;
    }

    public int getTotalResponses() {
        return this.totalResponses;
    }
}
