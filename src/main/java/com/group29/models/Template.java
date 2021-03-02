package com.group29.models;

import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;
import com.group29.models.temp.ChoiceQuestion;
import com.group29.models.temp.NumericQuestion;
import com.group29.models.temp.OpenQuestion;
import com.group29.models.temp.Option;
import com.group29.models.temp.Point;
import com.group29.models.temp.Question;
import com.group29.models.temp.QuestionResponse;
import com.group29.models.temp.Stats;
import com.group29.models.temp.Trend;

import org.bson.Document;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.BsonReader;
import org.bson.codecs.Codec;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.ObjectIdCodec;
import org.bson.types.ObjectId;

import com.google.gson.annotations.Expose;

public class Template
{
    private String id;
    @Expose
    private String userID;
    @Expose
    List<Question> questions;


    // Private constructor for template
    // Should only be created by Mongo DB
    private Template(String id, String userID, List<Question> questions)
    {
        this.id = id;
        this.userID = userID;
        this.questions = questions;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public Question[] getQuestions() {
        return this.questions.toArray(new Question[0]);
    }

    
    /**
     * Gets the template as a MongoDB Document
     * 
     * @return Document
     */
    public Document getTemplateAsDocument() {
        // Creates a blank document
        Document doc = new Document();

        // Fills the document with data
        // Includes the id if one exists
        if (id != null)
            doc.append("_id", new ObjectId(id));
        doc.append("userID", new ObjectId(userID));
        // Creates the array if it is not already created
        if (questions == null)
            questions = new ArrayList<Question>();

        doc.append("questions", (List<Document>) (questions.stream().map(x -> x.getQuestionAsDocument())
            .collect(Collectors.toList())));

        // Returns the filled document
        return doc;
    }

    public static Question[] parseQuestionsFromJSON(Gson gson, String json) throws ClassCastException {
        Map<String, Object> document = (Map<String, Object>) gson.fromJson(json, Map.class);
        if (!document.containsKey("questions")) {
            throw new ClassCastException("Invalid template");
        }
        ArrayList<Map<String, Object>> questions = (ArrayList<Map<String, Object>>) document.get("questions");
        ArrayList<Question> questionObjs = new ArrayList<>();
        if (questions.size() == 0) {
            ;
            throw new ClassCastException("Invalid template");
        }
        boolean valid = true;
        for (Map<String, Object> question : questions) {
            Document d = new Document(question);
            if (!d.containsKey("type") || !d.containsKey("title") || !(d.get("type") instanceof String)
                    || !(d.get("title") instanceof String)) {
                valid = false;
                System.out.println("Invalid keys (general)!");
                break;
            }
            String type = d.getString("type");
            String title = d.getString("title");
            switch (type) {
                case "open":
                    questionObjs.add(new OpenQuestion(title, new QuestionResponse[0], new Trend[0], 0));
                    break;
                case "numeric":
                    if (!d.containsKey("min") || !d.containsKey("max")) {
                        valid = false;
                        System.out.println("Numeric keys invalid!");
                        break;
                    }
                    int min = d.getDouble("min").intValue();
                    int max = d.getDouble("max").intValue();
                    questionObjs
                            .add(new NumericQuestion(title, new Stats(0, 0, 0, 0), min, max, 0, 0, 0, new Point[0]));
                    break;
                case "choice":
                    if (!d.containsKey("choices") || !d.containsKey("allowMultiple")) {
                        valid = false;
                        System.out.println("Choice keys invalid!");
                        break;
                    }
                    ArrayList<String> choices = (ArrayList<String>) d.get("choices");
                    if (choices.size() == 0) {
                        valid = false;
                        System.out.println("No choices!");
                        break;
                    }
                    ArrayList<Option> choiceObjs = new ArrayList<Option>();
                    ArrayList<String> choicesUsed = new ArrayList<String>();
                    for (int i = 0; i < choices.size(); i++) {
                        String c = choices.get(i);
                        if (choicesUsed.contains(c)) {
                            valid = false;
                            System.out.println("Duplicate choices!");
                            break;
                        }
                        if (c != null && c.length() >= 1) {
                            choiceObjs.add(new Option(c, i));
                            choicesUsed.add(c);
                        }
                    }
                    if (!valid)
                        break;
                    boolean multiple = d.getBoolean("allowMultiple");
                    questionObjs.add(new ChoiceQuestion(title, choiceObjs.toArray(new Option[0]), multiple));
                    break;
                default:
                    valid = false;
                    System.out.println("Invalid type!");
                    break;
            }
            if (!valid)
                break;
        }
        if (!valid) {
            throw new ClassCastException("Invalid template");
        }
        return questionObjs.toArray(new Question[0]);
    }
}
