package com.group29.models.questiondata;

import org.bson.Document;
import org.bson.types.ObjectId;

public class OpenQuestion extends Question {
    protected QuestionResponse[] recentResponses;
    protected Trend[] trends;
    protected float currentMood;

    /**
     * Constructs an Open question from a Document
     * 
     * @param doc The document
     */
    public OpenQuestion(Document doc) {
        super("open", doc.getString("title"));
    }

    /**
     * Constructs an Open question
     * 
     * @param title The title of the question
     * @param responses The responses to the question
     * @param trends The trending words of the question
     * @param currentMood The current mood of the question
     */
    public OpenQuestion(String title, QuestionResponse[] responses, Trend[] trends, float currentMood) {
        super("open", title);
        this.recentResponses = responses;
        this.trends = trends;
        this.currentMood = currentMood;
    }

    /**
     * Constructs an open question with just a title
     * 
     * @param title The title of the question
     */
    public OpenQuestion(String title) {
        super("open", title);
    }

    /**
     * Gets the responses to the question
     * 
     * @return The array of responses
     */
    public QuestionResponse[] getRecentResponses() {
        return this.recentResponses;
    }

    /**
     * Gets the array of trending words
     * 
     * @return The array of trending words
     */
    public Trend[] getTrends() {
        return this.trends;
    }

    /**
     * Gets the Open question as a Document
     * 
     * @return The created Document
     */
    @Override
    public Document getQuestionAsDocument() {
        // Creates a blank document
        Document doc = new Document();

        // Fills the document with data
        doc.append("_id", new ObjectId(id));
        doc.append("title", title);
        doc.append("type", type);

        // Returns this filled document
        return doc;
    }
}
