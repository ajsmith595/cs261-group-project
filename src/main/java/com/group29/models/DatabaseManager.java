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

    }

    // Adds a feedback to the database
    public void addFeedback(int eventID, int templateID, Feedback feedback) {

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