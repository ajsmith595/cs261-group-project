package com.group29.models.temp;

public class OpenQuestion extends Question {
    protected QuestionResponse[] recent_responses;
    protected Trend[] trends;
    protected float current_mood;

    public OpenQuestion(String title, QuestionResponse[] responses, Trend[] trends, float current_mood) {
        this.type = "open";
        this.title = title;
        this.recent_responses = responses;
        this.trends = trends;
        this.current_mood = current_mood;
    }
}
