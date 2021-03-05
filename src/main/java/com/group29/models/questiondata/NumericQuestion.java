package com.group29.models.questiondata;

import org.bson.Document;
import org.bson.types.ObjectId;

public class NumericQuestion extends Question {
    protected Stats stats;
    protected int min_value;
    protected int max_value;
    protected long min_time;
    protected long max_time;
    protected Point[] points;
    protected long current_time;

    public NumericQuestion(Document doc) {
        super("numeric", doc.getString("title"));
        min_value = doc.getInteger("min_value");
        max_value = doc.getInteger("max_value");
    }

    public NumericQuestion(String title, Stats stats, int min_value, int max_value, long min_time, long max_time,
            long current_time, Point[] points) {
        super("numeric", title);
        this.stats = stats;
        this.min_value = min_value;
        this.max_value = max_value;
        this.min_time = min_time;
        this.max_time = max_time;
        this.points = points;
        this.current_time = current_time;
    }

    public NumericQuestion(String title, int min_value, int max_value) {
        super("numeric", title);
        this.min_value = min_value;
        this.max_value = max_value;
    }

    public Stats getStats() {
        return this.stats;
    }

    public int getMinValue() {
        return this.min_value;
    }

    public int getMaxValue() {
        return this.max_value;
    }

    public long getMinTime() {
        return this.min_time;
    }

    public long getMaxTime() {
        return this.max_time;
    }

    public Point[] getPoints() {
        return this.points;
    }

    public long getCurrentTime() {
        return this.current_time;
    }

    public void setMinTime(long time) {
        this.min_time = time;
    }

    public void setMaxTime(long time) {
        this.max_time = time;
    }

    @Override
    public Document getQuestionAsDocument() {
        // Creates a blank document
        Document doc = new Document();

        // Fills the document with data
        doc.append("_id", new ObjectId(id));
        doc.append("title", title);
        doc.append("type", type);

        doc.append("min_value", min_value);
        doc.append("max_value", max_value);

        // TODO: ask about storing other data in db or check if it will just be grabbed
        // live

        // Returns this filled document
        return doc;
    }
}
