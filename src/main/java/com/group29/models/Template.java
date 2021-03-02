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


    public Template(String id, String userID, List<Question> questions)
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

    // Codec class to allow MongoDB to automatically create Template classes
    public static class TemplateCodec implements Codec<Template> {
        // Document codec to read raw BSON
        private Codec<Document> documentCodec;

        /**
         * Constructor for the codec
         */
        public TemplateCodec() {
            this.documentCodec = new DocumentCodec();
        }

        /**
         * Encodes an Template into the writer
         * 
         * @param writer         Writer to write into
         * @param value          Template to write
         * @param encoderContext Context for the encoding
         */
        @Override
        public void encode(final BsonWriter writer, final Template value, final EncoderContext encoderContext) {
            documentCodec.encode(writer, value.getTemplateAsDocument(), encoderContext);
        }

        /**
         * Decodes an Template from a BSON reader
         * 
         * @param reader         The reader to decode
         * @param decoderContext Context for the decoding
         * @return
         */
        @Override
        public Template decode(final BsonReader reader, final DecoderContext decoderContext) {
            // Generates a document from the reader
            Document doc = documentCodec.decode(reader, decoderContext);

            String id = doc.getObjectId("_id").toHexString();
            String userID = doc.getObjectId("userID").toHexString();
            List<Question> questions = doc.getList("questions", Document.class).stream().map(x -> Question.generateQuestionFromDocument(x)).collect(Collectors.toList());


            return new Template(id, userID, questions);
        }

        /**
         * Gets the class type for this encoder
         * 
         * @return The Template class type
         */
        @Override
        public Class<Template> getEncoderClass() {
            return Template.class;
        }
    }
}
