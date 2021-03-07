package com.group29.models.questiondata;

import org.bson.Document;
import org.bson.types.ObjectId;

public class NumericQuestion extends Question {
    protected Stats stats;
    protected int minValue;
    protected int maxValue;
    protected long minTime;
    protected long maxTime;
    protected Point[] points;
    protected long currentTime;

    /**
     * Constructs a Numeric Question from a document
     * 
     * @param doc the document
     */
    public NumericQuestion(Document doc) {
        super("numeric", doc.getString("title"));
        minValue = doc.getInteger("minValue");
        maxValue = doc.getInteger("maxValue");
    }

    /**
     * Constructs a Numeric Question
     * 
     * @param title The title of the question
     * @param stats The statistics of the question
     * @param minValue The minimum rating value of the question
     * @param maxValue The maximum rating value of the question
     * @param minTime The minimum time the question can be answered
     * @param maxTime The maximum time the question can be answered
     * @param currentTime The current time the question is created
     * @param points The list of average points for the graph
     */
    public NumericQuestion(String title, Stats stats, int minValue, int maxValue, long minTime, long maxTime,
            long currentTime, Point[] points) {
        super("numeric", title);
        this.stats = stats;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.points = points;
        this.currentTime = currentTime;
    }

    /**
     * Constructs a Numeric Question
     * 
     * @param title The title of the question
     * @param minValue The minimum rating value of the question
     * @param maxValue The maximum rating value of the question
     */
    public NumericQuestion(String title, int minValue, int maxValue) {
        super("numeric", title);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Gets the stats object for the question
     * 
     * @return the stats object
     */
    public Stats getStats() {
        return this.stats;
    }

    /**
     * Gets the minimum value of the question
     * 
     * @return the minimum value
     */
    public int getMinValue() {
        return this.minValue;
    }

    /**
     * Gets the maximum value of the question
     * 
     * @return The maximum value
     */
    public int getMaxValue() {
        return this.maxValue;
    }

    /**
     * Gets the minimum time of the question
     * 
     * @return the minimum time
     */
    public long getMinTime() {
        return this.minTime;
    }

    /**
     * Gets the maximum time of the question
     * 
     * @return The max time
     */
    public long getMaxTime() {
        return this.maxTime;
    }

    /**
     * Gets the array of points for the question
     * 
     * @return The array of points
     */
    public Point[] getPoints() {
        return this.points;
    }

    /**
     * Gets the current time of the question
     * 
     * @return the current time
     */
    public long getCurrentTime() {
        return this.currentTime;
    }

    /**
     * Sets a new minimum time for the question
     * 
     * @param time The new minimum time
     */
    public void setMinTime(long time) {
        this.minTime = time;
    }

    /**
     * Sets a new maximum time for the question
     * 
     * @param time The new maximum time
     */
    public void setMaxTime(long time) {
        this.maxTime = time;
    }

    /**
     * Gets the Numeric question as a Document
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

        doc.append("minValue", minValue);
        doc.append("maxValue", maxValue);

        // Returns this filled document
        return doc;
    }
}
