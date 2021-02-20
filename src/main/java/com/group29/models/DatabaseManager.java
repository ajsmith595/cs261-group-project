package com.group29.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.group29.models.temp.*;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.codecs.BsonCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.configuration.CodecRegistries;

import com.google.gson.Gson;

public class DatabaseManager {
    // Instance of the database manager
    private static final DatabaseManager dbManager = new DatabaseManager();

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDB;

    /**
     * Gets the singleton instance of the database manager
     * @return The main instance of the database manager
     */
    public static DatabaseManager getDatabaseManager() {
        return dbManager;
    }

    /**
     * Constructor for the database
     */
    private DatabaseManager() {
        // Gets the default codecs and an instance of the event codec
        CodecRegistry defaultCodecReg = MongoClient.getDefaultCodecRegistry();
        Event.EventCodec eventCodec = new Event.EventCodec();

        // Adds the event codec to the codec registry and saves it in the options
        CodecRegistry codecReg = CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(eventCodec), defaultCodecReg);
        MongoClientOptions options = MongoClientOptions.builder().codecRegistry(codecReg).build();

        // Creates a connection to the client with the custom codecs and access the database
        mongoClient = new MongoClient("localhost:27017", options);
        mongoDB = mongoClient.getDatabase("App");
    }

    /**
     * Adds a user to the database
     * @param email The email of the user
     * @param name The name of the user
     * @return The id of the user
     */
    public String addUser(String email, String name) {
        return "";
    }

    /**
     * Checks if a user exists in the database
     * @param email The email of the user to be checked
     * @return True if the user exists
     */
    public boolean checkUser(String email) {
        return false;
    }

    /**
     * Adds an event to the database
     * This will create an id for the event if one does not exist
     * @param event The event to be added
     * @return The id of the event
     */
    public String addEvent(Event event) {
        // Gets the events collection and the event as a document
        MongoCollection events = mongoDB.getCollection("Events");
        Document obj = event.getEventAsDocument();

        // Inserts the event and sets the id of the event to the one given
        events.insertOne(obj);
        event.setID(obj.getObjectId("_id").toHexString());

        // Returns the id of the event
        return event.getID();
    }

    /**
     * Checks if the given event code has been taken
     * @param eventCode
     * @return True if the event code is taken
     */
    public boolean isEventCodeTaken(String eventCode)
    {
        // Gets the events collection and creates a query string for the event code
        MongoCollection events = mongoDB.getCollection("Events");
        Document query = new Document("eventCode", eventCode);

        // Returns true if at least one document with the event code is found
        return events.countDocuments(query) > 0;
    }

    /**
     * Gets the event from the event id
     * @param eventID The id of the event
     * @return The event matching the id or null if none is found
     */
    public Event getEventFromID(String eventID) {
        // Gets the events collection and creates a query string for the event id
        MongoCollection events = mongoDB.getCollection("Events");
        Document query = new Document("_id", new ObjectId(eventID));

        // Loops over events found matching the id, returning the first one
        for (Event event : (FindIterable<Event>)events.find(query, Event.class)) {
            return event;
        }

        // Returns null if none are found
        return null;
    }

    /**
     * Gets the event from the event code
     * @param code The code for the event
     * @return The event matching the event code or null if none is found
     */
    public Event getEventFromCode(String code) {
        // Gets the events collection and creates a query string for the event code
        MongoCollection events = mongoDB.getCollection("Events");
        Document query = new Document("eventCode", code);
        
        // Loops over events found matching the id, returning the first one
        for (Event event : (FindIterable<Event>)events.find(query, Event.class)) {
            return event;
        }

        // Returns null if none are found
        return null;
    }

    // Adds a template to the database
    public void addTemplate(int eventID, Object questions) {
        // TODO
    }

    // Adds a template to the database
    public Template getTemplate(String templateID) {
        //TODO Actually remove this mess
        return new Template(templateID, "a", new Question[] {
                new OpenQuestion("General, Feedback", new QuestionResponse[0], new Trend[0], 0),

                new ChoiceQuestion("What is your Favourite colour?", new Option[] {
                        new Option("Red", -1),
                        new Option("Yellow", -1),
                        new Option("Green", -1)
                }),
                new ChoiceQuestion("What age group are you in?", new Option[] {
                        new Option("18-24", -1),
                        new Option("25-39", -1),
                        new Option("40-59", -1),
                        new Option("60+", -1)
                }),
                new NumericQuestion("How would you rate this event?", new Stats(-1, -1, -1), -1,
                10, 0L, 60L, 34L, new Point[0])
        });
    }

    // Adds a feedback to the database
    public void addFeedback(String eventID, int templateID, Feedback feedback) {

    }

    /**
     * Gets a list of the feedback for the given event id
     * @param eventId The event of the feedback
     * @return List of feedback for the given event
     */
    public List<Feedback> getFeedback(String eventId) {
        // TODO Actually implement this mess, this is taken from the Event temporary updateData method
        List<Feedback> fb = new ArrayList<>();

        ArrayList<QuestionResponse> all_responses = new ArrayList<>(
            Arrays.asList(new QuestionResponse("Very interesting and intriguing", null, "a"),
                    new QuestionResponse("Something else....", "ajsmith595", "b"),
                    new QuestionResponse("Pretty boring", "haterman443", "c"),
                    new QuestionResponse("Clearly well-educated", "KrazyKid69", "d"),
                    new QuestionResponse("Joy is at a low point here", null, "e"),
                    new QuestionResponse("Confusing", null, "f")));

        int index = 1000;
        for (QuestionResponse qr : all_responses) {
            fb.add(new Feedback(Integer.toString(++index), qr.getUsername() == null ? "x" : qr.getUsername(), qr.getUsername() != null,
                    new ArrayList<>(Arrays.asList(new Response("r" + index + "0", "0", qr.getMessage())))));
        }

        for (int i = 0; i < 200; i++) {
            String colour = new String[] {"Red", "Yellow", "Green"}[(int) Math.floor(3 * Math.random())];
            String age = new String[] {"18-24", "25-39", "40-59", "60+"}[(int) Math.floor(4 * Math.random())];

            fb.add(new Feedback(Integer.toString(i), "u" + i, false,
                    new ArrayList<>(Arrays.asList(
                            new Response("r" + i + "1", "1", colour),
                            new Response("r" + i + "2", "2", age),
                            new Response("r" + i + "3", "3", Integer.toString((int) (10 * Math.random())))
                    ))));
        }

        return fb;
    }

}