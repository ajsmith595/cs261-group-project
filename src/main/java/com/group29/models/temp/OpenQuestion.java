package com.group29.models.temp;

public class OpenQuestion extends Question {
    protected QuestionResponse[] recent_responses;
    protected Trend[] trends;
    protected float current_mood;

    public OpenQuestion(String title, QuestionResponse[] responses, Trend[] trends) {
        this.type = "open";
        this.current_mood = (float) (Math.random() * 2 - 1);
        this.title = title;
        this.recent_responses = responses;
        this.trends = trends;
    }

    public QuestionResponse[] getRecentResponses() {
        return this.recent_responses;
    }

    public Trend[] getTrends() {
        return this.trends;
    }
}
