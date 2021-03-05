package com.group29.models.questiondata;

import org.bson.Document;

public class Option {
    protected String name;
    protected int number;

    public Option(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return this.name;
    }

    public int getNumber() {
        return this.number;
    }

    /**
     * Gets the option as a MongoDB Document
     * 
     * @return The document containing information about the option
     */
    public Document getAsDocument() {
        Document doc = new Document("name", name);
        doc.append("number", number);
        return doc;
    }
}
