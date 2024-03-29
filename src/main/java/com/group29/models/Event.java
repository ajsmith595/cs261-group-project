package com.group29.models;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.group29.JSONTransformer;
import com.group29.models.questiondata.ChoiceQuestion;
import com.group29.models.questiondata.NumericQuestion;
import com.group29.models.questiondata.OpenQuestion;
import com.group29.models.questiondata.Option;
import com.group29.models.questiondata.Point;
import com.group29.models.questiondata.Question;
import com.group29.models.questiondata.QuestionResponse;
import com.group29.models.questiondata.Trend;
import com.group29.models.questiondata.WebSocketData;

import org.eclipse.jetty.websocket.api.Session;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Comparator;

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
    private boolean modified = true;
    private ReentrantLock dataModificationLock;
    private ReentrantLock clientModificationLock;

    /**
     * Constructs an event object
     * 
     * @param id           The ID of the event
     * @param hostID       The ID of the host
     * @param templateID   The ID of the template for the event
     * @param title        The title of the event
     * @param startTime    The start time of the event
     * @param duration     The duration of the event
     * @param eventCode    the code to join the event
     * @param feedbackList The list of feedback for the event
     */
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
        this.feedbackList.sort(Comparator.comparing(Feedback::getTimeStamp));
        this.clients = new ArrayList<Session>();
        this.aggregator = FeedbackAggregator.getFeedbackAggregator();
        this.dataModificationLock = new ReentrantLock();
        this.clientModificationLock = new ReentrantLock();
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
     * @param id The ID to set it to
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Sets the title of the event
     * 
     * @param title the title of the event
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the title of the event
     * 
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Gets the event code for the event
     * 
     * @return The event code
     */
    public String getEventCode() {
        return this.eventCode;
    }

    /**
     * Gets the host's ID
     * 
     * @return Gets the Host ID
     */
    public String getHostID() {
        return this.hostID;
    }

    /**
     * Sets the event's template ID
     * 
     * @param id The template ID
     */
    public void setTemplateID(String id) {
        this.templateID = id;
    }

    /**
     * Sets the start time of the event
     * 
     * @param startTime the start time of the event
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the event start time
     * 
     * @return the start time in unix format
     */
    public Date getStartTime() {
        return this.startTime;
    }

    /**
     * Sets the duration of the event
     * 
     * @param duration the duration of the event
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Gets the duration of the event
     * 
     * @return the Duration
     */
    public int getDuration() {
        return this.duration;
    }

    /**
     * Gets the template ID of the event
     * 
     * @return the template ID
     */
    public String getTemplateID() {
        return this.templateID;
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

    /**
     * Adds a new client to the event
     * 
     * @param s The session of the client
     */
    public void addClient(Session s) {
        this.generateData(false);
        clientModificationLock.lock();

        try {
            clients.add(s);
            s.getRemote().sendString((new JSONTransformer()).render(this.data)); // immediately send data to client
        } catch (Exception e) {
            System.out.println("Error occured: " + e.getMessage());
        } finally {
            clientModificationLock.unlock();
        }
    }

    /**
     * Gets the number of clients this event is serving
     * 
     * @return
     */
    public int getNumberOfClients() {
        clientModificationLock.lock();
        try {
            int num = clients.size();
            return num;
        } finally {
            clientModificationLock.unlock();
        }
    }

    /**
     * Sets the event as modified. Use this if the event has had feedback added to
     * it
     */
    public void setAsModified() {
        dataModificationLock.lock();
        try {
            this.modified = true;
        } finally {
            dataModificationLock.unlock();
        }
    }

    public void uninitialiseEvent() {
        this.aggregator = null;
    }

    /**
     * Generates the new WebSocket data for the event
     * 
     * @param updateFromDB If true, the event will fetch new data from the database
     */
    public void generateData(boolean updateFromDB) {
        dataModificationLock.lock();
        try {
            if (updateFromDB)
                this.feedbackList = DatabaseManager.getDatabaseManager().getFeedback(this.id);
            // Sort feedback based on time stamp
            this.feedbackList.sort(Comparator.comparing(Feedback::getTimeStamp));
            WebSocketData results = this.aggregator.collateFeedback(this);
            this.data = results;
        } finally {
            dataModificationLock.unlock();
        }
    }

    /**
     * Sends data to clients if it has been modified, or if the force flag is true.
     * 
     * @param force If true, it will send the data even if the event has not been
     *              modified
     */
    public void sendData(boolean force) {
        sendData(force, true);
    }

    /**
     * Updates the data for the of the event
     * 
     * @param fetchFromDatabase  whether it needs to fetch event feedback from the
     *                           database
     * @param updateEventDetails whether it needs to update the details of the event
     */
    public void sendData(boolean force, boolean updateFromDB) {
        if (force || modified) {

            this.generateData(modified || updateFromDB); // If it's been modified, we need to get the new data from
                                                         // the DB
            modified = false;
            this.sendDataToClients();

        }
    }

    /**
     * Fetches the new event details (title, startTime and duration) from the
     * database.
     */
    public void updateEventDetails() {
        try {
            dataModificationLock.lock();
            Event e = DatabaseManager.getDatabaseManager().getEventFromID(this.id);
            this.setTitle(e.getTitle());
            this.setStartTime(e.getStartTime());
            this.setDuration(e.getDuration());
        } finally {
            dataModificationLock.unlock();
        }
    }

    /**
     * Send the current event data to every WebSocket client. Should be called after
     * updating the data
     */
    private void sendDataToClients() {
        String data = (new JSONTransformer()).render(this.data);
        clientModificationLock.lock();
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
        } finally {
            clientModificationLock.unlock();
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
     * Gets all feedback for this event
     * 
     * @return Object
     */
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
        d.append("title", title);
        d.append("startTime", startTime.getTime());
        d.append("duration", duration);

        long currentTime = (new Date()).getTime();
        long endTime = startTime.getTime() + duration * 60 * 1000;
        if (currentTime > endTime) {
            d.append("error", "Sorry! This event has ended!");
            return d;
        } else if (currentTime < startTime.getTime()) {
            d.append("error", "This event hasn't started yet!");
            return d;
        }

        Document questionDoc = new Document();
        if (this.data == null) {
            this.generateData(false);
        }
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
                d2.append("value", Math.round((nq.getMaxValue() + nq.getMinValue()) / 2f));
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
        d.append("eventCode", eventCode);
        d.append("title", title);
        d.append("startTime", startTime.getTime());
        d.append("duration", duration);
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

        // Generates 7 random hexadecimal characters
        for (int i = 0; i < 7; i++) {
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
        doc.append("hostID", new ObjectId(hostID));
        doc.append("templateID", new ObjectId(templateID));
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

    /**
     * Gets the rating history
     * 
     * @return the rating history
     */
    public HashMap<String, List<Point>> getRatingHistory() {
        return ratingHistory;
    }

    /**
     * Prints out the information of the question data
     * 
     * @param questions the questions being printed out
     */
    private void printQuestions(Question[] questions) {
        for (Question q : questions) {
            if (q instanceof OpenQuestion) {
                System.out.println(((OpenQuestion) q).getTitle());
                System.out.println(((OpenQuestion) q).getRecentResponses().length);
                for (QuestionResponse qr : ((OpenQuestion) q).getRecentResponses()) {
                    System.out.println(qr.getMessage());
                    System.out.println(qr.getUsername());
                    System.out.println(qr.getID());
                }
                for (Trend t : ((OpenQuestion) q).getTrends()) {
                    System.out.println(t.getPhrase());
                    System.out.println(t.getProportion());
                }
            }
            if (q instanceof NumericQuestion) {
                System.out.println(((NumericQuestion) q).getTitle());
                System.out.println(((NumericQuestion) q).getMinValue());
                System.out.println(((NumericQuestion) q).getMaxValue());
                System.out.println(((NumericQuestion) q).getMinTime());
                System.out.println(((NumericQuestion) q).getMaxTime());
                System.out.println(((NumericQuestion) q).getCurrentTime());
                System.out.println(((NumericQuestion) q).getPoints().length);
                for (Point p : ((NumericQuestion) q).getPoints()) {
                    System.out.println("Point time: " + p.getTime());
                    System.out.println("Point value: " + p.getValue());
                }
            }
            if (q instanceof ChoiceQuestion) {
                System.out.println(((ChoiceQuestion) q).getTitle());
                System.out.println(((ChoiceQuestion) q).getMultiple());
                System.out.println(((ChoiceQuestion) q).getOptions().length);
                for (Option o : ((ChoiceQuestion) q).getOptions()) {
                    System.out.println(o.getName());
                    System.out.println(o.getNumber());
                }
            }
        }
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
            String hostID = doc.getObjectId("hostID").toHexString();
            String templateID = doc.getObjectId("templateID").toHexString();
            String title = doc.getString("title");
            Date startTime = doc.getDate("startTime");
            int duration = doc.getInteger("duration");
            String eventCode = doc.getString("eventCode");
            List<Feedback> feedbackList = doc.getList("feedbackList", Document.class).stream()
                    .map(x -> Feedback.generateFeedbackFromDocument(x)).collect(Collectors.toList());

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
