package com.group29.controllers;

import java.util.Date;

import com.group29.models.DatabaseManager;
import com.group29.models.APIResponse;

import static spark.Spark.*;

import java.security.SecureRandom;
import java.util.HashMap;

import com.group29.JSONTransformer;
import com.group29.models.Event;
import com.group29.models.User;
import com.group29.models.Feedback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonObject;

import org.bson.Document;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.RouteGroup;

public class APIController {
    public static RouteGroup routes = () -> {
        get("/event/:id/token", "application/json", APIController.getWebSocketToken, new JSONTransformer());
        get("/event/:id", "application/json", APIController.getSession, new JSONTransformer());
        get("/user/:id", "application/json", APIController.getUser, new JSONTransformer());
        /*
         * Makes it return in JSON format. Will automatically convert regular Java
         * classes to JSON. Will keep all fields (private/public/protected) but will
         * discard functions.
         */
        post("/events", APIController.postEvent, new JSONTransformer());
        post("/users", APIController.postUser, new JSONTransformer());
        post("/feedback/:id", APIController.postFeedback, new JSONTransformer());
        post("/event/:id/feedback", "application/json", APIController.checkData);
    };

    private static HashMap<String, String> webSocketTokens = new HashMap<>();
    private static SecureRandom rng = new SecureRandom();
    private static Route getWebSocketToken = (Request req, Response response) -> {
        String eventID = req.params(":id").toLowerCase();
        // TODO: check event code is valid, and that the person requesting is actually
        // the host

        char[] possibleChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        String webSocketToken = "";
        for (int i = 0; i < 32; i++) {
            webSocketToken += possibleChars[rng.nextInt(possibleChars.length)];
        }
        /////////////////////////////////// TODO: remove after development
        response.header("Access-Control-Allow-Origin", "*");

        webSocketTokens.put(webSocketToken, eventID);
        return new WebSocketTokenContainer(webSocketToken);
    };

    public static String checkWebSocketToken(String token) {
        if (webSocketTokens.containsKey(token)) {
            return webSocketTokens.remove(token);
        }
        return null;
    }

    private static class WebSocketTokenContainer {
        protected String token;

        public WebSocketTokenContainer(String token) {
            this.token = token;
        }
    }

    public static Route getSession = (Request req, Response res) -> {
        String eventCode = req.params(":id");
        Event event = DatabaseManager.getDatabaseManager().getEventFromCode(eventCode);

        if (event != null)
            return APIResponse.success(event);

        return APIResponse.error("Could not match event code");
    };

    // Gets the user ID from email and password
    public static Route getUser = (Request req, Response res) -> {
        String userId = req.params(":id");
        User user = DatabaseManager.getDatabaseManager().getUserFromID(userId);

        if (user != null)
            return APIResponse.success(user);
        
        return APIResponse.error("The user requested could not be found");
    };

    // Creates a new event
    public static Route postEvent = (Request req, Response res) -> {
        // Sets the return type to json
        res.type("application/json");
        // Catches parsing errors
        try {
            // Creates a GSON parser that can parse dates and excludes id and eventcode
            // fields
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class,
                            (JsonDeserializer<Date>) (json, typeOfT,
                                    context) -> new Date(json.getAsJsonPrimitive().getAsLong()))
                    .registerTypeAdapter(Date.class,
                            (JsonSerializer<Date>) (date, type,
                                    jsonSerializationContext) -> new JsonPrimitive(date.getTime()))
                    .excludeFieldsWithoutExposeAnnotation().create();

            // Attempts to parse event
            Event event = gson.fromJson(req.body(), Event.class);

            // Generates a new code for the event
            event.generateEventCode();

            // Adds it to the database and returns the event code
            DatabaseManager.getDatabaseManager().addEvent(event);
            return APIResponse.success(new Document("eventCode", event.getEventCode()));
        } catch (Exception e) {
            // Prints the error to console
            e.printStackTrace();
        }
        // Returns an error response
        return APIResponse.error("Could not create the event.");
    };


    // Registers a new user
    public static Route postUser = (Request req, Response res) -> {
        // Sets the return type to json
        res.type("application/json");
        // Catches parsing errors
        try {
            // Creates a GSON parser that can parse dates and excludes id
            Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

            // Attempts to parse the user
            User user = gson.fromJson(req.body(), User.class);

            // Checks if the user already exists
            if (DatabaseManager.getDatabaseManager().checkUser(user.getEmail()))
                return APIResponse.error("A user with the given email already exists.");
            
            // Adds it to the database and returns the user id
            String id = DatabaseManager.getDatabaseManager().addUser(user);
            return APIResponse.success(new Document("id", user.getID()));
        } catch (Exception e) {
            // Prints the error to console
            e.printStackTrace();
        }
        // Returns an error response
        return APIResponse.error("Could not create the user.");
    };


    // Posts a new feedback
    public static Route postFeedback = (Request req, Response res) -> {
        // Gets the event code from the url
        String eventCode = req.params(":id");
        // Sets the return type to json
        res.type("application/json");
        // Catches parsing errors
        try {
            // Creates a GSON parser that can parse dates and excludes id
            Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

            // Attempts to parse the feedback as well as the id of the event
            Feedback feedback = gson.fromJson(req.body(), Feedback.class);


            // Attempts to add the feedback to the database
            boolean result =  DatabaseManager.getDatabaseManager().addFeedback(eventCode, feedback);

            // Responds with whether it worked
            if (result)
                return APIResponse.success(new Document());
            return APIResponse.error("The feedback could not be added.");
        } catch (Exception e) {
            // Prints the error to console
            e.printStackTrace();
        }
        // Returns an error response
        return APIResponse.error("The feedback could not be added.");
    };
    
    public static Route checkData = (Request req, Response res) -> {
        System.out.println(req.body());
        return "success";
    };
}
