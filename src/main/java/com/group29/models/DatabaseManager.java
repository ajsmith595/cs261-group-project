package com.group29.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.configuration.CodecRegistries;

import com.group29.controllers.WebSocketController;

public class DatabaseManager {
    // Instance of the database manager
    private static final DatabaseManager dbManager = new DatabaseManager();

    private MongoClient mongoClient;
    private MongoDatabase mongoDB;

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
    }

    public void init(String databaseHost) {
        // Gets the default codecs and an instance of the model codecs
        CodecRegistry defaultCodecReg = MongoClient.getDefaultCodecRegistry();
        Event.EventCodec eventCodec = new Event.EventCodec();
        User.UserCodec userCodec = new User.UserCodec();
        Feedback.FeedbackCodec feedbackCodec = new Feedback.FeedbackCodec();
        Response.ResponseCodec responseCodec = new Response.ResponseCodec();
        Template.TemplateCodec templateCodec = new Template.TemplateCodec();

        // Adds the event codec to the codec registry and saves it in the options
        CodecRegistry codecReg = CodecRegistries.fromRegistries(
                CodecRegistries.fromCodecs(eventCodec, userCodec, feedbackCodec, responseCodec, templateCodec),
                defaultCodecReg);
        MongoClientOptions options = MongoClientOptions.builder().codecRegistry(codecReg).build();

        // Creates a connection to the client with the custom codecs and access the
        // database
        mongoClient = new MongoClient(databaseHost, options);
        mongoDB = mongoClient.getDatabase("App");
        boolean hasEvents = false;
        for (String name : mongoDB.listCollectionNames()) {
            if (name.equals("Events")) {
                hasEvents = true;
                break;
            }
        }
        if (!hasEvents) {
            mongoDB.createCollection("Events");
        }
        IndexOptions io = new IndexOptions();
        io.unique(true);
        mongoDB.getCollection("Events").createIndex(new Document("eventCode", 1), io);
    }

    /**
     * Adds a user to the database
     * 
     * @param user The user to add
     * @return The id of the user or null if the email is taken
     */
    public String addUser(User user) {
        // Returns null if the email is already taken
        if (checkUser(user.getEmail()))
            return null;

        // Gets the collection of users and the user as a document
        MongoCollection<Document> users = mongoDB.getCollection("Users");
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
        MongoCollection<Document> users = mongoDB.getCollection("Users");
        Document query = new Document("email", email);

        // Returns whether at least one user with the given email exists
        return users.countDocuments(query) > 0;
    }

    /**
     * Checks if a user exists in the database with the given username
     * 
     * @param username The username of the user to be checked
     * @return True if the user exists
     */
    public boolean checkUsername(String username) {
        // Gets the collection of users and creates a query
        MongoCollection<Document> users = mongoDB.getCollection("Users");
        Document query = new Document("username", username);

        // Returns whether at least one user with the given username exists
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
        MongoCollection<Document> users = mongoDB.getCollection("Users");
        Document query = new Document("email", email);
        query.append("username", username);

        // Loops over users found matching the details, returning the first one
        for (User user : users.find(query, User.class)) {
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
        MongoCollection<Document> users = mongoDB.getCollection("Users");
        Document query = new Document("_id", new ObjectId(id));

        // Loops over users found matching the details, returning the first one
        for (User user : users.find(query, User.class)) {
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
        MongoCollection<Document> events = mongoDB.getCollection("Events");
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
     * @param eventCode code of event to check
     * @return True if the event code is taken
     */
    public boolean isEventCodeTaken(String eventCode) {
        // Gets the events collection and creates a query string for the event code
        MongoCollection<Document> events = mongoDB.getCollection("Events");
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
        MongoCollection<Document> events = mongoDB.getCollection("Events");
        Document query = new Document("_id", new ObjectId(eventID));

        // Loops over events found matching the id, returning the first one
        for (Event event : events.find(query, Event.class)) {
            return event;
        }

        // Returns null if none are found
        return null;
    }

    public boolean updateEvent(String eventCode, String newTitle, Date newStartTime, int newDuration) {
        // Gets the events collection and creates a query string for the event id
        MongoCollection<Document> events = mongoDB.getCollection("Events");
        Document query = new Document("eventCode", eventCode);

        // Loops over events found matching the id, adding the feedback to the first one
        // found
        for (Event event : events.find(query, Event.class)) {
            event.setTitle(newTitle);
            event.setStartTime(newStartTime);
            event.setDuration(newDuration);

            // Updates the database and returns true as it was inserted
            events.findOneAndReplace(query, event.getEventAsDocument());
            WebSocketController.updateEventDetails(event.getEventCode());
            return true;
        }

        return false;
    }

    /**
     * Gets the event from the event code
     * 
     * @param code The code for the event
     * @return The event matching the event code or null if none is found
     */
    public Event getEventFromCode(String code) {
        // Gets the events collection and creates a query string for the event code
        MongoCollection<Document> events = mongoDB.getCollection("Events");
        Document query = new Document("eventCode", code);

        // Loops over events found matching the id, returning the first one
        for (Event event : events.find(query, Event.class)) {
            return event;
        }

        // Returns null if none are found
        return null;
    }

    /**
     * Gets the events for a specific user
     * 
     * @param uid The user's ID
     * @return The events that the user os the host of
     */
    public ArrayList<Event> getEventsForUser(String uid) {
        // Gets the events collection and creates a query string for host ID
        MongoCollection<Document> events = mongoDB.getCollection("Events");
        Document query = new Document("hostID", new ObjectId(uid));
        ArrayList<Event> list = new ArrayList<>();
        for (Event e : events.find(query, Event.class)) {
            list.add(e);
        }
        return list;
    }

    /**
     * Adds a template to the database
     * 
     * @param template The template to be added
     * @return The id of the added template (also now stored inside of the template)
     */
    public String addTemplate(Template template) {
        // Gets the events collection and the event as a document
        MongoCollection templates = mongoDB.getCollection("Templates");
        Document obj = template.getTemplateAsDocument();

        // Inserts the event and sets the id of the event to the one given
        templates.insertOne(obj);
        template.setID(obj.getObjectId("_id").toHexString());

        // Returns the id of the event
        return template.getID();
    }

    /**
     * Gets the template matching the given template id
     * 
     * @param templateID The id of the template to find
     * @return The template with the given id, or null if none are found
     */
    public Template getTemplate(String templateID) {
        // Gets the templates collection and creates a query string for the template id
        MongoCollection templates = mongoDB.getCollection("Templates");
        Document query = new Document("_id", new ObjectId(templateID));

        // Loops over templates found matching the id, returning the first one
        for (Template template : (FindIterable<Template>) templates.find(query, Template.class)) {
            return template;
        }

        // Returns null if none are found
        return null;
    }

    /**
     * Adds a feedback to the database
     * 
     * @param eventCode The code of the event the feedback was for
     * @param feedback  The feedback to be stored
     * @return True if the feedback was added successfully
     */
    public boolean addFeedback(String eventCode, Feedback feedback) {
        // Gets the events collection and creates a query string for the event id
        MongoCollection<Document> events = mongoDB.getCollection("Events");

        UpdateResult ur = events.updateOne(new Document("eventCode", eventCode),
                Updates.addToSet("feedbackList", feedback));
        WebSocketController.setEventAsModified(eventCode);
        if (ur.getMatchedCount() > 0)
            return true;

        return false;
    }

    /**
     * Checks if a user has sent a feedback to an event within a minute
     * 
     * @param userID    The ID of the user to be checked
     * @param eventCode The event of the feedback
     * @return Whether a minute has passed since the last feedback was sent by the
     *         user
     */
    public boolean canSendFeedback(String userID, String eventCode) {
        // Get the event from the database
        Event e = DatabaseManager.getDatabaseManager().getEventFromCode(eventCode);

        // Return false if the event could not be found
        if (e == null)
            return false;

        // Get the current time and a placeholder for the time of the latest feedback
        long now = Calendar.getInstance().getTimeInMillis();
        long latest = 0;

        // Iterate through the feedback for all sent by the given user
        for (Feedback feedback : e.getFeedback()) {
            if (feedback.getUserID().equals(userID)) {
                // Update the latest time if needed
                if (latest < feedback.getTimeStamp())
                    latest = feedback.getTimeStamp();
            }
        }
        // Return true if the latest is over a minute ago
        return now - 1000 * 60 > latest;
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
    }

}