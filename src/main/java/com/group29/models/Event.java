package com.group29.models;

import java.util.*;

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

import java.lang.StringBuilder;

import javax.lang.model.util.ElementScanner6;
import javax.xml.crypto.Data;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;

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

public class Event {

    // @Expose used to prevent gson from filling the id field and eventCode fields
    // when a new event is created

    private String id; // database ID
    @Expose
    private String hostID; // event host ID
    @Expose
    private String templateID; // event template ID
    @Expose
    private String title; // title of event
    @Expose
    private Date startTime; // Start date-time of event
    @Expose
    private int duration; // Duration of event in minutes
    @Expose
    private String eventCode; // event code used by clients
    private ArrayList<Session> clients;
    private WebSocketData data;
    private HashMap<String, List<Point>> ratingHistory = new HashMap<>();

    // Used to reconstruct an already created event
    private Event(String id, String hostID, String templateID, String title, Date startTime, int duration,
            String eventCode) {
        this.id = id;
        this.hostID = hostID;
        this.templateID = templateID;
        this.title = title;
        this.startTime = startTime;
        this.duration = duration;
        this.eventCode = eventCode;
        this.clients = new ArrayList<Session>();

        this.updateData();
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

    public void updateData() {
//        Trend energetic = new Trend("Energetic", 30 + (int) ((Math.random() - 0.5) * 20));
//        Trend interesting = new Trend("Interesting", 40 + (int) ((Math.random() - 0.5) * 10));
//        Trend inspiring = new Trend("Inspiring", 50 + (int) ((Math.random() - 0.5) * 45));
//
//        ArrayList<QuestionResponse> qrs = new ArrayList<>();
//        for (int i = 3; i >= 0; i--) {
//            qrs.add(all_responses.get((index + i) % all_responses.size()));
//        }
//        index += 1;
//
//        OpenQuestion oq = new OpenQuestion("General Feedback", qrs.toArray(new QuestionResponse[0]),
//                new Trend[] { energetic, interesting, inspiring });
//        ChoiceQuestion cq1 = new ChoiceQuestion("What is your favourite colour?",
//                new Option[] { new Option("Red", (int) Math.floor(100 * Math.random())),
//                        new Option("Yellow", (int) Math.floor(100 * Math.random())),
//                        new Option("Green", (int) Math.floor(100 * Math.random())) });
//        ChoiceQuestion cq2 = new ChoiceQuestion("What age group are you in?",
//                new Option[] { new Option("18-24", (int) Math.floor(100 * Math.random())),
//                        new Option("25-39", (int) Math.floor(100 * Math.random())),
//                        new Option("40-59", (int) Math.floor(100 * Math.random())),
//                        new Option("60+", (int) Math.floor(100 * Math.random())) });
//
//        ArrayList<Point> points = new ArrayList<Point>();
//        Calendar c = Calendar.getInstance();
//        long currentTime = c.getTimeInMillis() / 1000;
//        c.set(Calendar.MINUTE, 0);
//        long startTime = c.getTimeInMillis() / 1000;
//        c.set(Calendar.HOUR, c.get(Calendar.HOUR) + 1);
//        long endTime = c.getTimeInMillis() / 1000;
//        for (long i = startTime; i < currentTime; i += 60 * 5) {
//            points.add(new Point(i, (float) (Math.random() * 10)));
//        }
//        float currentValue = (float) (Math.random() * 10);
//        points.add(new Point(currentTime, currentValue));
//
//        NumericQuestion nq = new NumericQuestion("How would you rate this event?", new Stats(currentValue, 5, 10), 0,
//                10, startTime, endTime, currentTime, points.toArray(new Point[0]));



//        WebSocketData wsd = new WebSocketData(new Question[] { oq, cq1, cq2, nq });
//        this.data = wsd;
        this.data = new WebSocketData(FeedbackAggregator.getFeedbackAggregator().collateFeedback(id));
    }

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
     * Gets all feedback for this event TODO: this
     * 
     * @return Object
     */
    // Gets all the feedback from the event
    public Object getFeedback() {
        return null;
    }

    /**
     * Generates an event code for the event If one has already been generated, this
     * does nothing
     * 
     * @return true if a code was generated
     */
    public boolean generateEventCode() {
        // Checks if the event has a code
        if (eventCode != null)
            return false;

        // Creates a new RNG and string builder
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // Generates 5 random hexadecimal characters
        for (int i = 0; i < 5; i++) {
            int val = random.nextInt(16);
            System.out.println(val);
            if (val < 10)
                sb.append((char) ('0' + val));
            else
                sb.append((char) ('A' + val));
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

            // Returns a new event using the data obtained
            return new Event(id, hostID, templateID, title, startTime, duration, eventCode);
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
