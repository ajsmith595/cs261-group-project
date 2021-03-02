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

    /**
     * Takes a GSON instance and the JSON body, and parses the "questions"
     * attribute.
     * 
     * @param gson The GSON instance used to initially parse the JSON
     * @param json The actual JSON string
     * @return An array of questions derived from the JSON
     * @throws ClassCastException If the "questions" attribute is not in the correct
     *                            format. It is strict on this, so a small error can
     *                            cause failure.
     */
    public static Question[] parseQuestionsFromJSON(Gson gson, String json) throws ClassCastException {
        Map<String, Object> document = (Map<String, Object>) gson.fromJson(json, Map.class);
        if (!document.containsKey("questions")) {
            throw new ClassCastException("Invalid template");
        }
        ArrayList<Map<String, Object>> questions = (ArrayList<Map<String, Object>>) document.get("questions");
        ArrayList<Question> questionObjs = new ArrayList<>();
        if (questions.size() == 0) {
            throw new ClassCastException("Invalid template");
        }
        boolean valid = true;
        for (Map<String, Object> question : questions) {
            Document d = new Document(question); // Convert it to a Document so we can cast automatically (and throw
                                                 // ClassCastException on failure)
            if (!d.containsKey("type") || !d.containsKey("title") || !(d.get("type") instanceof String)
                    || !(d.get("title") instanceof String)) {
                valid = false;
                break;
            }
            // All questions must have a type and a title
            String type = d.getString("type");
            String title = d.getString("title");
            switch (type) {
                case "open":
                    questionObjs.add(new OpenQuestion(title, new QuestionResponse[0], new Trend[0], 0));
                    break;
                case "numeric":
                    if (!d.containsKey("min") || !d.containsKey("max")) {
                        valid = false;
                        break;
                    }
                    int min = d.getDouble("min").intValue(); // Numbers are always detected as doubles
                    int max = d.getDouble("max").intValue();
                    questionObjs
                            .add(new NumericQuestion(title, new Stats(0, 0, 0, 0), min, max, 0, 0, 0, new Point[0]));
                    break;
                case "choice":
                    if (!d.containsKey("choices") || !d.containsKey("allowMultiple")) {
                        valid = false;
                        break;
                    }
                    ArrayList<String> choices = (ArrayList<String>) d.getList("choices", String.class);
                    if (choices.size() == 0) {
                        valid = false;
                        break;
                    }
                    ArrayList<Option> choiceObjs = new ArrayList<Option>();
                    ArrayList<String> choicesUsed = new ArrayList<String>(); // Don't allow multiple occurences of the
                                                                             // same choice.
                    for (int i = 0; i < choices.size(); i++) {
                        String c = choices.get(i);
                        if (choicesUsed.contains(c)) {
                            valid = false;
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
