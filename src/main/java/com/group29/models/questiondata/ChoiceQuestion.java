package com.group29.models.questiondata;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;

public class ChoiceQuestion extends Question {
    protected Option[] options;
    protected boolean multiple;

    /**
     * Constructor for the choice question from a document
     * 
     * @param doc The document the question is built from
     */
    public ChoiceQuestion(Document doc) {
        super("choice", doc.getString("title"));
        multiple = doc.getBoolean("multiple");

        options = doc.getList("options", Document.class).stream()
                .map(x -> new Option(x.getString("name"), x.getInteger("number"))).toArray(Option[]::new);

    }

    /**
     * Constructor for a choice question
     * 
     * @param title Title of the question
     * @param options The different options an attendee can choose from
     * @param multiple whether a user can choose multiple answers or not
     */
    public ChoiceQuestion(String title, Option[] options, boolean multiple) {
        super("choice", title);
        this.options = options;
        this.multiple = multiple;
    }

    /**
     * Gets whether the attendee can choose multiple
     * answers or not
     * 
     * @return whether multiple is True or False
     */
    public boolean getMultiple() {
        return this.multiple;
    }

    /**
     * Gets the different options of the question
     * 
     * @return The list of options
     */
    public Option[] getOptions() {
        return this.options;
    }

    /**
     * Gets the question as a Document
     * 
     * @return the created Document
     */
    @Override
    public Document getQuestionAsDocument() {
        // Creates a blank document
        Document doc = new Document();

        // Fills the document with data
        doc.append("_id", new ObjectId(id));
        doc.append("title", title);
        doc.append("type", type);

        doc.append("multiple", multiple);
        doc.append("options", Arrays.stream(options).map(x -> x.getAsDocument()).collect(Collectors.toList()));

        // Returns this filled document
        return doc;
    }
}
