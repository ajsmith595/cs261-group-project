package com.group29.models.temp;

import org.bson.Document;
import org.bson.types.ObjectId;

public class OpenQuestion extends Question {
    protected QuestionResponse[] recent_responses;
    protected Trend[] trends;
    protected float current_mood;

    public OpenQuestion(Document doc)
    {
        super("open", doc.getString("title"));
    }

    public OpenQuestion(String title, QuestionResponse[] responses, Trend[] trends) {
        super("open", title);
        this.current_mood = (float) (Math.random() * 2 - 1);
        this.recent_responses = responses;
        this.trends = trends;
        this.current_mood = current_mood;
    }

    public OpenQuestion(String title) {
        super("open", title);
    }

    public QuestionResponse[] getRecentResponses() {
        return this.recent_responses;
    }

    public Trend[] getTrends() {
        return this.trends;
    }

    @Override
    public Document getQuestionAsDocument()
    {
        // Creates a blank document
        Document doc = new Document();

        // Fills the document with data
        doc.append("_id", new ObjectId(id));
        doc.append("title", title);
        doc.append("type", type);

        // TODO: ask about storing other data in db or check if it will just be grabbed live

        // Returns this filled document
        return doc;
    }
}
