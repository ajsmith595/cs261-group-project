package com.group29.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.Data;

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
import com.group29.controllers.WebSocketController;

public class DatabaseManager {
    // Instance of the database manager
    private static final DatabaseManager dbManager = new DatabaseManager();

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDB;

    /**
     * Gets the singleton instance of the database manager
     * 
     * @return The main instance of the database manager
     */
    public static DatabaseManager getDatabaseManager() {
        return dbManager;
    }

    /**
     * Constructor for the database
     */
    private DatabaseManager() {
        // Gets the default codecs and an instance of the model codecs
        CodecRegistry defaultCodecReg = MongoClient.getDefaultCodecRegistry();
        Event.EventCodec eventCodec = new Event.EventCodec();
        User.UserCodec userCodec = new User.UserCodec();
        Feedback.FeedbackCodec feedbackCodec = new Feedback.FeedbackCodec();
        Response.ResponseCodec responseCodec = new Response.ResponseCodec();

        // Adds the event codec to the codec registry and saves it in the options
        CodecRegistry codecReg = CodecRegistries.fromRegistries(
                CodecRegistries.fromCodecs(eventCodec, userCodec, feedbackCodec, responseCodec), defaultCodecReg);
        MongoClientOptions options = MongoClientOptions.builder().codecRegistry(codecReg).build();

        // Creates a connection to the client with the custom codecs and access the
        // database
        mongoClient = new MongoClient("localhost:27017", options);
        mongoDB = mongoClient.getDatabase("App");
    }

    /**
     * Adds a user to the database
     * 
     * @param email The email of the user
     * @param name  The name of the user
     * @return The id of the user or null if the email is taken
     */
    public String addUser(User user) {
        // Returns null if the email is already taken
        if (checkUser(user.getEmail()))
            return null;

        // Gets the collection of users and the user as a document
        MongoCollection users = mongoDB.getCollection("Users");
        Document obj = user.getUserAsDocument();

        // Inserts the user and updates the users id
        users.insertOne(obj);
        user.setID(obj.getObjectId("_id").toHexString());

        // Returns the users id
        return user.getID();
    }

    /**
     * Checks if a user exists in the database
     * 
     * @param email The email of the user to be checked
     * @return True if the user exists
     */
    public boolean checkUser(String email) {
        // Gets the collection of users and creates a query
        MongoCollection users = mongoDB.getCollection("Users");
        Document query = new Document("email", email);

        // Returns whether at least one user with the given email exists
        return users.countDocuments(query) > 0;
    }

    /**
     * Gets the users data from the database
     * 
     * @param email    The email of the user
     * @param username The username of the user
     * @return The user instance with updated data
     */
    public User getUserFromDetails(String email, String username) {
        // Gets the collection of users and creates a query
        MongoCollection users = mongoDB.getCollection("Users");
        Document query = new Document("email", email);
        query.append("username", username);

        // Loops over users found matching the details, returning the first one
        for (User user : (FindIterable<User>) users.find(query, User.class)) {
            return user;
        }

        // Returns null if none are found
        return null;
    }

    /**
     * Gets the users data from the database The user passed in is also returned to
     * allow for chaining While it is not needed, it is done for QoL
     * 
     * @param id The user id to get info from
     * @return The user instance with updated data
     */
    public User getUserFromID(String id) {
        // Gets the collection of users and creates a query
        MongoCollection users = mongoDB.getCollection("Users");
        Document query = new Document("_id", new ObjectId(id));

        // Loops over users found matching the details, returning the first one
        for (User user : (FindIterable<User>) users.find(query, User.class)) {
            return user;
        }

        // Returns null if none are found
        return null;
    }

    /**
     * Adds an event to the database This will create an id for the event if one
     * does not exist
     * 
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
     * 
     * @param eventCode
     * @return True if the event code is taken
     */
    public boolean isEventCodeTaken(String eventCode) {
        // Gets the events collection and creates a query string for the event code
        MongoCollection events = mongoDB.getCollection("Events");
        Document query = new Document("eventCode", eventCode);

        // Returns true if at least one document with the event code is found
        return events.countDocuments(query) > 0;
    }

    /**
     * Gets the event from the event id
     * 
     * @param eventID The id of the event
     * @return The event matching the id or null if none is found
     */
    public Event getEventFromID(String eventID) {
        // Gets the events collection and creates a query string for the event id
        MongoCollection events = mongoDB.getCollection("Events");
        Document query = new Document("_id", new ObjectId(eventID));

        // Loops over events found matching the id, returning the first one
        for (Event event : (FindIterable<Event>) events.find(query, Event.class)) {
            return event;
        }

        // Returns null if none are found
        return null;
    }

    /**
     * Gets the event from the event code
     * 
     * @param code The code for the event
     * @return The event matching the event code or null if none is found
     */
    public Event getEventFromCode(String code) {
        // Gets the events collection and creates a query string for the event code
        MongoCollection events = mongoDB.getCollection("Events");
        Document query = new Document("eventCode", code);

        // Loops over events found matching the id, returning the first one
        for (Event event : (FindIterable<Event>) events.find(query, Event.class)) {
            return event;
        }

        // Returns null if none are found
        return null;
    }

    // Adds a template to the database
    public void addTemplate(String eventID, Object questions) {

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

            // Updates the database and returns true as it was inserted

            // Adds the feedback to the event
            event.addFeedback(feedback);
            events.findOneAndReplace(query, event.getEventAsDocument());
            WebSocketController.updateEvent(event.getEventCode());
            return true;
        }

        // Returns false if an event with the given id was not found
        return false;
    }

    /**
     * Gets a list of the feedback for the given event id
     * 
     * @param eventId The event of the feedback
     * @return List of feedback for the given event
     */
    public List<Feedback> getFeedback(String eventId) {

        Event e = DatabaseManager.getDatabaseManager().getEventFromID(eventId);
        if (e == null)
            return new ArrayList<>();
        return e.getFeedback();
        /*// TODO Actually implement this mess, this is taken from the Event temporary updateData method
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

        return fb;*/
    }

}