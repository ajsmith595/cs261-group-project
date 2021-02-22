package com.group29.models;

import java.util.List;

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
    private static DatabaseManager dbManager = new DatabaseManager();

    private MongoClient mongoClient;
    private MongoDatabase mongoDB;

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
        // Gets the default codecs and an instance of the model codecs
        CodecRegistry defaultCodecReg = MongoClient.getDefaultCodecRegistry();
        Event.EventCodec eventCodec = new Event.EventCodec();
        User.UserCodec userCodec = new User.UserCodec();
        Feedback.FeedbackCodec feedbackCodec = new Feedback.FeedbackCodec();
        Response.ResponseCodec responseCodec = new Response.ResponseCodec();

        // Adds the event codec to the codec registry and saves it in the options
        CodecRegistry codecReg = CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(eventCodec, userCodec, feedbackCodec, responseCodec), defaultCodecReg);
        MongoClientOptions options = MongoClientOptions.builder().codecRegistry(codecReg).build();

        // Creates a connection to the client with the custom codecs and access the database
        mongoClient = new MongoClient("localhost:27017", options);
        mongoDB = mongoClient.getDatabase("App");
    }

    /**
     * Adds a user to the database
     * @param email The email of the user
     * @param name The name of the user
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
     * @param email The email of the user
     * @param username The username of the user
     * @return The user instance with updated data
     */
    public User getUserFromDetails(String email, String username)
    {
        // Gets the collection of users and creates a query
        MongoCollection users = mongoDB.getCollection("Users");
        Document query = new Document("email", email);
        query.append("username", username);

        // Loops over users found matching the details, returning the first one
        for (User user : (FindIterable<User>)users.find(query, User.class)) {
            return user;
        }

        // Returns null if none are found
        return null;
    }

    /**
     * Gets the users data from the database
     * The user passed in is also returned to allow for chaining
     * While it is not needed, it is done for QoL
     * @param id The user id to get info from
     * @return The user instance with updated data
     */
    public User getUserFromID(String id)
    {
        // Gets the collection of users and creates a query
        MongoCollection users = mongoDB.getCollection("Users");
        Document query = new Document("_id", new ObjectId(id));

        // Loops over users found matching the details, returning the first one
        for (User user : (FindIterable<User>)users.find(query, User.class)) {
            return user;
        }

        // Returns null if none are found
        return null;
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
    public void addTemplate(String eventID, Object questions) {

    }

    /**
     * Adds a feedback to the database
     * @param eventID The id of the event the feedback was for
     * @param feedback The feedback to be stored
     * @return True if the feedback was added successfully
     */
    public boolean addFeedback(String eventCode, Feedback feedback) {
        // Gets the events collection and creates a query string for the event id
        MongoCollection events = mongoDB.getCollection("Events");
        Document query = new Document("eventCode", eventCode);

        // Loops over events found matching the id, adding the feedback to the first one found
        for (Event event : (FindIterable<Event>)events.find(query, Event.class)) {
            // Adds the feedback to the event
            event.addFeedback(feedback);

            // Updates the database and returns true as it was inserted
            events.findOneAndReplace(query, event.getEventAsDocument());
            return true;
        }

        // Returns false if an event with the given id was not found
        return false;
    }

    /**
     * Gets a list of the feedback for the given event id
     * @param eventId The event of the feedback
     * @return List of feedback for the given event
     */
    public List<Feedback> getFeedback(String eventId) {
        return null;
    }

}