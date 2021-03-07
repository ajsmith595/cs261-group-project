package com.group29.models.questiondata;

import java.lang.reflect.Type;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

public abstract class Question {
    protected String id;
    protected String type;
    protected String title;

    /**
     * Abstract constructor that auto-generates id
     * 
     * @param type The type of the question
     * @param title The title of the question
     */
    public Question(String type, String title) {
        this(new ObjectId().toHexString(), type, title);
    }

    // Abstract constructor to force values to be inserted
    /**
     * Abstract constructor that auto-generates id
     * 
     * @param id The id of the question
     * @param type The type of the question
     * @param title The title of the question
     */
    public Question(String id, String type, String title) {
        this.id = id;
        this.type = type;
        this.title = title;
    }

    /**
     * Gets the ID of the question
     * 
     * @return the ID
     */
    public String getID() {
        return this.id;
    }

    /**
     * Gets the type of the question
     * 
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Gets the title of the question
     * 
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Generates the given question from a MongoDB Document Will automatically
     * determine the type of question
     * 
     * @param doc The doc containing the question
     * @return The question containted in the doc or null if no type was matched
     */
    public static final Question generateQuestionFromDocument(Document doc) {
        switch (doc.getString("type")) {
            case "choice":
                return new ChoiceQuestion(doc);
            case "open":
                return new OpenQuestion(doc);
            case "numeric":
                return new NumericQuestion(doc);
        }
        return null;
    }

    /**
     * Deserialiser for questions to allow GSON to construct questions automatically
     */
    public static final JsonDeserializer<Question> deserialiser = new JsonDeserializer<Question>() {
        @Override
        public Question deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            // Convert the gson object to a document
            JsonObject obj = json.getAsJsonObject();
            Document doc = Document.parse(obj.toString());

            // Create and return the question from the json
            return generateQuestionFromDocument(doc);
        }
    };

    /**
     * Gets the question as a MongoDB Document
     * 
     * @return Document containing the data about the question
     */
    public abstract Document getQuestionAsDocument();

}
