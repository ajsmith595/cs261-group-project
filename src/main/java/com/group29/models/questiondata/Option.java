package com.group29.models.questiondata;

import org.bson.Document;

public class Option {
    protected String name;
    protected int number;

    /**
     * Creates an option for a choice question
     * 
     * @param name The name of the option
     * @param number The number times the option was selected
     */
    public Option(String name, int number) {
        this.name = name;
        this.number = number;
    }

    /**
     * Gets the name of the option
     *
     * @return the name 
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the number of times the option
     * was selected
     * 
     * @return the number of times selected 
     */
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
