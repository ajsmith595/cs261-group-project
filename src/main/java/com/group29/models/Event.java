package com.group29.models;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

import com.group29.JSONTransformer;
import com.group29.models.temp.ChoiceQuestion;
import com.group29.models.temp.NumericQuestion;
import com.group29.models.temp.OpenQuestion;
import com.group29.models.temp.Option;
import com.group29.models.temp.Point;
import com.group29.models.temp.Question;
import com.group29.models.temp.QuestionResponse;
import com.group29.models.temp.Stats;
import com.group29.models.temp.Trend;
import com.group29.models.temp.WebSocketData;

import org.eclipse.jetty.websocket.api.Session;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import java.lang.StringBuilder;
import org.bson.Document;
import org.bson.BsonWriter;
import org.bson.BsonReader;
import org.bson.codecs.Codec;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import com.google.gson.annotations.Expose;

public class Event {

    // @Expose used to prevent gson from filling the id field and eventCode fields
    // when a new event is created

    private String id; // database ID
    private String hostID; // event host ID
    private String templateID; // event template ID
    @Expose
    private String title; // title of event
    @Expose
    private Date startTime; // Start date-time of event
    @Expose
    private int duration; // Duration of event in minutes
    @Expose
    private String eventCode; // event code used by clients
    @Expose
    private List<Feedback> feedbackList;
    private ArrayList<Session> clients;
    private WebSocketData data;
    private HashMap<String, List<Point>> ratingHistory = new HashMap<>();
    private FeedbackAggregator aggregator;

    // Used to reconstruct an already created event
    private Event(String id, String hostID, String templateID, String title, Date startTime, int duration,
            String eventCode, List<Feedback> feedbackList) {
        this.id = id;
        this.hostID = hostID;
        this.templateID = templateID;
        this.title = title;
        this.startTime = startTime;
        this.duration = duration;
        this.eventCode = eventCode;
        this.feedbackList = feedbackList;
        this.clients = new ArrayList<Session>();
        this.aggregator = FeedbackAggregator.getFeedbackAggregator();
        //System.out.println(this.aggregator.collateFeedback(this));
        //System.out.println("test");
        this.updateData(false);
    }

    /**
     * Gets the ID of the event
     * 
     * @return String
     */
    public String getID() {
        return this.id;
    }

    /**
     * Sets the id of the event
     * 
     * @param id The id to set it to
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Gets the event code for the event
     * 
     * @return String
     */
    public String getEventCode() {
        return this.eventCode;
    }

    /**
     * Gets the host's ID
     * 
     * @return
     */
    public String getHostID() {
        return this.hostID;
    }

    /**
     * Temporal function to set the host ID. Used so that we can set the host ID on
     * the server, as opposed to trusting the client to give us the right host ID.
     * 
     * @param hostID
     */
    public void setHostID(String hostID) {
        this.hostID = hostID;
    }

    public void addClient(Session s) {
        clients.add(s);
        try {
            s.getRemote().sendString((new JSONTransformer()).render(this.data)); // immediately send data to client
        } catch (Exception e) {
            System.out.println("Error occured: " + e.getMessage());
        }
    }

//    private static ArrayList<QuestionResponse> all_responses = new ArrayList<>(
//            Arrays.asList(new QuestionResponse[] { new QuestionResponse("Very interesting and intriguing", null, "a"),
//                    new QuestionResponse("Something else....", "ajsmith595", "b"),
//                    new QuestionResponse("Pretty boring", "haterman443", "c"),
//                    new QuestionResponse("Clearly well-educated", "KrazyKid69", "d"),
//                    new QuestionResponse("Joy is at a low point here", null, "e"),
//                    new QuestionResponse("Confusing", null, "f") }));
//    private static int index = 0;

    /**
     * Updates the data of the event. Currently randomly generated sample data, but
     * in the future should be linked to the DB
     */

    public void updateData() {
        updateData(true);
    }

    public void updateData(boolean fetchFromDatabase) {
        if (fetchFromDatabase)
            this.feedbackList = DatabaseManager.getDatabaseManager().getFeedback(this.id);
        Question[] results = this.aggregator.collateFeedback(this);
        /*for(Question q : results){
            if(q instanceof OpenQuestion){
                System.out.println(((OpenQuestion) q).getTitle());
                System.out.println(((OpenQuestion) q).getRecentResponses().length);
                for(QuestionResponse qr: ((OpenQuestion) q).getRecentResponses()){
                    System.out.println(qr.getMessage());
                    System.out.println(qr.getUsername());
                    System.out.println(qr.getID());
                }
                for(Trend t: ((OpenQuestion) q).getTrends()){
                    System.out.println(t.getPhrase());
                    System.out.println(t.getProportion());
                }
            }
            if(q instanceof NumericQuestion){
                System.out.println(((NumericQuestion) q).getTitle());
                System.out.println(((NumericQuestion) q).getMinValue());
                System.out.println(((NumericQuestion) q).getMaxValue());
                System.out.println(((NumericQuestion) q).getMinTime());
                System.out.println(((NumericQuestion) q).getMaxTime());
                System.out.println(((NumericQuestion) q).getCurrentTime());
                System.out.println(((NumericQuestion) q).getPoints().length);
                for(Point p: ((NumericQuestion) q).getPoints()){
                    System.out.println(p.getTime());
                    System.out.println(p.getValue());
                }
            }
            if(q instanceof ChoiceQuestion){
                System.out.println(((ChoiceQuestion) q).getTitle());
                System.out.println(((ChoiceQuestion) q).getMultiple());
                System.out.println(((ChoiceQuestion) q).getOptions().length);
                for(Option o: ((ChoiceQuestion) q).getOptions()){
                    System.out.println(o.getName());
                    System.out.println(o.getNumber());
                }
            }
        }
        ArrayList<QuestionResponse> qrs = new ArrayList<>();
        HashMap<String, Integer> choiceMap = new HashMap<>();
        choiceMap.put("Red", 0);
        choiceMap.put("Yellow", 0);
        choiceMap.put("Green", 0);
        choiceMap.put("Blue", 0);

        ArrayList<Point> points = new ArrayList<Point>();

        int feedbackNumber = 0;
        for (Feedback f : this.feedbackList) {
            int responseNumber = 0;
            for (Response r : f.getResponses()) {
                Object response = r.getResponse();
                if (responseNumber == 0) {
                    String key = "feedback_response_" + Integer.toString(feedbackNumber) + "_"
                            + Integer.toString(responseNumber);
                    if (response instanceof String) {
                        qrs.add(new QuestionResponse((String) response, f.getAnonymous() ? null : f.getUserID(), key));
                    } else {
                        // Invalid
                    }
                } else if (responseNumber == 1) {
                    if (response instanceof ArrayList) {
                        ArrayList arr = (ArrayList) response;
                        for (Object a : arr) {
                            if (a instanceof String) {
                                if (choiceMap.containsKey(a)) {
                                    choiceMap.put((String) a, choiceMap.get(a) + 1);
                                }
                            }
                        }
                    } else if (response instanceof Integer) {
                        System.out.println("Integer response");
                    } else {
                        System.out.println("Other response: " + response.toString());
                    }
                } else if (responseNumber == 2) {
                    if (response instanceof Double) {
                        double responseAsFloat = ((double) response);
                        System.out.println("Yes");
                        points.add(new Point(f.getTimestamp() / 1000, responseAsFloat));
                    } else {
                        System.out.println("Nope: " + response.getClass().toString());
                    }
                }
                responseNumber++;
            }
            feedbackNumber++;
        }

        Collections.reverse(qrs);

        Trend energetic = new Trend("Energetic", 30 + (int) ((Math.random() - 0.5) * 20));
        Trend interesting = new Trend("Interesting", 40 + (int) ((Math.random() - 0.5) * 10));
        Trend inspiring = new Trend("Inspiring", 50 + (int) ((Math.random() - 0.5) * 45));

        OpenQuestion generalFeedbackQuestion = new OpenQuestion("General Feedback",
                qrs.toArray(new QuestionResponse[0]), new Trend[] { energetic, interesting, inspiring });

        ArrayList<Option> options = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : choiceMap.entrySet()) {
            options.add(new Option(entry.getKey(), entry.getValue()));
        }
        ChoiceQuestion cq1 = new ChoiceQuestion("What is your favourite colour?", options.toArray(new Option[0]), true);

        Calendar c = Calendar.getInstance();
        long currentTime = c.getTimeInMillis() / 1000;
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.HOUR, c.get(Calendar.HOUR) - 1);
        long startTime = c.getTimeInMillis() / 1000;
        c.set(Calendar.HOUR, c.get(Calendar.HOUR) + 1);
        long endTime = c.getTimeInMillis() / 1000;

        double currentValue = (double) (Math.random() * 10);
        points.add(new Point(currentTime, currentValue));

        NumericQuestion nq = new NumericQuestion("How would you rate this event?", new Stats(currentValue, 5, 10), 0,
                10, startTime, endTime, currentTime, points.toArray(new Point[0]));
        //WebSocketData wsd = new WebSocketData(new Question[] { generalFeedbackQuestion, cq1, nq });*/
        WebSocketData wsd = new WebSocketData(results);
        this.data = wsd;

        // ArrayList<QuestionResponse> qrs = new ArrayList<>();
        // for (int i = 3; i >= 0; i--) {
        // qrs.add(all_responses.get((index + i) % all_responses.size()));
        // }
        // index += 1;

        // OpenQuestion oq = new OpenQuestion("General Feedback", qrs.toArray(new
        // QuestionResponse[0]),
        // new Trend[] { energetic, interesting, inspiring });

        // ChoiceQuestion cq2 = new ChoiceQuestion("What age group are you in?",
        // new Option[] { new Option("18-24", (int) Math.floor(100 * Math.random())),
        // new Option("25-39", (int) Math.floor(100 * Math.random())),
        // new Option("40-59", (int) Math.floor(100 * Math.random())),
        // new Option("60+", (int) Math.floor(100 * Math.random())) });

        // ArrayList<Point> points = new ArrayList<Point>();
        // Calendar c = Calendar.getInstance();
        // long currentTime = c.getTimeInMillis() / 1000;
        // c.set(Calendar.MINUTE, 0);
        // long startTime = c.getTimeInMillis() / 1000;
        // c.set(Calendar.HOUR, c.get(Calendar.HOUR) + 1);
        // long endTime = c.getTimeInMillis() / 1000;
        // for (long i = startTime; i < currentTime; i += 60 * 5) {
        // points.add(new Point(i, (float) (Math.random() * 10)));
        // }
        // float currentValue = (float) (Math.random() * 10);
        // points.add(new Point(currentTime, currentValue));

        // NumericQuestion nq = new NumericQuestion("How would you rate this event?",
        // new Stats(currentValue, 5, 10), 0,
        // 10, startTime, endTime, currentTime, points.toArray(new Point[0]));
    }

    /**
     * Send the current event data to every WebSocket client. Should be called after
     * updating the data
     */
    public void sendDataToClients() {

        String data = (new JSONTransformer()).render(this.data);
        try {
            ArrayList<Session> closedSessions = new ArrayList<Session>();
            for (Session s : clients) {
                if (!s.isOpen()) {
                    closedSessions.add(s);
                } else {
                    s.getRemote().sendString(data);
                }
            }
            for (Session s : closedSessions) {
                clients.remove(s);
            }
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage() + " => " + e.getCause() + " => "
                    + e.getStackTrace().toString());
        }
    }

    /**
     * Adds a feedback to the event
     * 
     * @param feedback The feedback to be added
     */
    public void addFeedback(Feedback feedback) {
        feedbackList.add(feedback);
    }

    /**
     * Gets all feedback for this event TODO: this
     * 
     * @return Object
     */
    // Gets all the feedback from the event
    public List<Feedback> getFeedback() {
        return this.feedbackList;
    }

    /**
     * Gets the Document object to be converted into JSON for the event when
     * requested by an attendee. Contains all questions with default values, and
     * mins/maxes for numeric questions, and choices for multiple choice questions.
     * 
     * @return The document with the associated values
     */
    public Document getAttendeeViewDocument() {
        Document d = new Document();

        d.append("isHost", false);
        d.append("hostID", hostID);
        d.append("title", title);
        d.append("startTime", startTime.getTime());
        d.append("duration", duration);
        Document questionDoc = new Document();
        Question[] questions = this.data.getQuestions();
        for (int i = 0; i < questions.length; i++) {
            Question q = questions[i];
            Document d2 = new Document();
            d2.append("title", q.getTitle());
            d2.append("type", q.getType());
            d2.append("id", i);
            switch (q.getType()) {
                case "open":
                    d2.append("value", "");
                    break;
                case "choice":
                    ChoiceQuestion cq = (ChoiceQuestion) q;
                    ArrayList<String> options = new ArrayList<>();
                    for (Option o : cq.getOptions()) {
                        options.add(o.getName());
                    }
                    d2.append("multiple", cq.getMultiple());
                    if (cq.getMultiple()) {
                        d2.append("value", new int[0]);
                    } else {
                        d2.append("value", -1);
                    }
                    d2.append("choices", options.toArray());
                    break;
                case "numeric":
                    NumericQuestion nq = (NumericQuestion) q;
                    d2.append("min", nq.getMinValue());
                    d2.append("max", nq.getMaxValue());
                    d2.append("value", (int) Math.round((nq.getMaxValue() + nq.getMinValue()) / 2));
                    break;
            }
            questionDoc.append(Integer.toString(i), d2);
        }
        d.append("questions", questionDoc);

        return d;
    }

    /**
     * Gets the document object to be converted into JSON for the event when
     * requested by a host. It only needs the isHost field since all other data is
     * obtained via WebSockets
     * 
     * @return The document with the associated values
     */
    public Document getHostViewDocument() {
        Document d = new Document();
        d.append("isHost", true);
        return d;
    }

    /**
     * Generates an event code for the event If one has already been generated, this
     * does nothing
     * 
     * @return true if a code was generated
     */
    public boolean generateEventCode() {
        // Checks if the event has a code
        if (eventCode != null) {
            return false;
        }

        // Creates a new RNG and string builder
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // Generates 5 random hexadecimal characters
        for (int i = 0; i < 5; i++) {
            int val = random.nextInt(16);
            if (val < 10)
                sb.append((char) ('0' + val));
            else
                sb.append((char) ('A' + (val - 10)));
        }
        // Puts the event code together and checks if it currently exists
        eventCode = sb.toString();
        if (DatabaseManager.getDatabaseManager().isEventCodeTaken(eventCode)) {
            // Resets the event code and generates another one
            eventCode = null;
            generateEventCode();
        }

        // Returns true as an event code was generated
        return true;
    }

    /**
     * Gets the event as a MongoDB Document
     * 
     * @return Document
     */
    public Document getEventAsDocument() {
        // Creates a blank document
        Document doc = new Document();

        // Fills the document with data
        // Includes the id if one exists
        if (id != null)
            doc.append("_id", new ObjectId(id));
        doc.append("hostID", hostID);
        doc.append("templateID", templateID);
        doc.append("title", title);
        doc.append("startTime", startTime);
        doc.append("duration", duration);
        // Generates an event code if one does not exist
        if (eventCode == null)
            generateEventCode();
        doc.append("eventCode", eventCode);
        if (feedbackList != null)
            doc.append("feedbackList", feedbackList);
        else
            doc.append("feedbackList", new ArrayList<Feedback>());

        // Returns the filled document
        return doc;
    }

    public String getTemplateID() {
        return templateID;
    }

    public HashMap<String, List<Point>> getRatingHistory() {
        return ratingHistory;
    }

    // Codec class to allow MongoDB to automatically create Event classes
    public static class EventCodec implements Codec<Event> {
        // Document codec to read raw BSON
        private Codec<Document> documentCodec;

        /**
         * Constructor for the codec
         */
        public EventCodec() {
            this.documentCodec = new DocumentCodec();
        }

        /**
         * Encodes an Event into the writer
         * 
         * @param writer         Writer to write into
         * @param value          Event to write
         * @param encoderContext Context for the encoding
         */
        @Override
        public void encode(final BsonWriter writer, final Event value, final EncoderContext encoderContext) {
            documentCodec.encode(writer, value.getEventAsDocument(), encoderContext);
        }

        /**
         * Decodes an Event from a BSON reader
         * 
         * @param reader         The reader to decode
         * @param decoderContext Context for the decoding
         * @return
         */
        @Override
        public Event decode(final BsonReader reader, final DecoderContext decoderContext) {
            // Generates a document from the reader
            Document doc = documentCodec.decode(reader, decoderContext);

            // Grabs the information from the document
            String id = doc.getObjectId("_id").toHexString();
            // TODO change these to getObjectId
            String hostID = doc.getString("hostID");
            String templateID = doc.getString("templateID");
            String title = doc.getString("title");
            Date startTime = doc.getDate("startTime");
            int duration = doc.getInteger("duration");
            String eventCode = doc.getString("eventCode");
            List<Feedback> feedbackList = doc.getList("feedbackList", Document.class).stream()
                    .map(x -> Feedback.generateFeedbackFromDocument(x)).collect(Collectors.toList());
            if (feedbackList != null) {
                System.out.println(feedbackList.size());
            }

            // Returns a new event using the data obtained
            return new Event(id, hostID, templateID, title, startTime, duration, eventCode, feedbackList);
        }

        /**
         * Gets the class type for this encoder
         * 
         * @return The Event class type
         */
        @Override
        public Class<Event> getEncoderClass() {
            return Event.class;
        }
    }
}
