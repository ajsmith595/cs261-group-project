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

// TODO Temporary implementation, need to replace questions at a minimum (and ofc add Mongo Codec, etc.)
public class Template {
    private String id; // database ID
    private String hostID; // event host ID
    private Question[] questions;

    public Template(String id, String hostID, Question[] questions) {
        this.id = id;
        this.hostID = hostID;
        this.questions = questions;
    }

    public String getId() {
        return id;
    }

    public String getHostID() {
        return hostID;
    }

    public Question[] getQuestions() {
        return questions;
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
