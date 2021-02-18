package com.group29.controllers;

import java.util.Date;

import com.group29.models.DatabaseManager;
import com.group29.models.APIResponse;

import static spark.Spark.*;

import java.security.SecureRandom;
import java.util.HashMap;

import com.group29.JSONTransformer;
import com.group29.models.Event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;

import org.bson.Document;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.RouteGroup;

public class APIController {
    public static RouteGroup routes = () -> {

        // #region External Code Copied
        // Code: https://gist.github.com/saeidzebardast/e375b7d17be3e0f4dddf
        // This code handles CORS requests for POST requests. In simple, to prevent
        // cross origin requests, the browser will make sure that the server is okay
        // with giving their data to a different host (e.g. on a dev environment at
        // localhost:3000). With POST requests especially, it'll check what methods are
        // allowed by the server.
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });
        // #endregion External Code Copied

        // Gets run after every request, just before the response is actually sent to
        // the user. Used for development environments.
        after("/*", (req, res) -> {
            res.header("Access-Control-Allow-Origin", "http://localhost:3000");
        });

        // Get WebSocket authentication token for a particular event
        get("/event/:id/token", "application/json", APIController.getWebSocketToken, new JSONTransformer());

        // Get event data
        get("/event/:id", "application/json", APIController.getEvent, new JSONTransformer());

        // Create a new event
        post("/events", APIController.postEvent, new JSONTransformer());

        // Add new feedback for an event
        post("/event/:id/feedback", "application/json", APIController.checkData, new JSONTransformer());
    };

    // Contains a key value map of all WebSocket auth tokens, with the event code
    // that they are associated with
    private static HashMap<String, String> webSocketTokens = new HashMap<>();
    private static SecureRandom rng = new SecureRandom();

    /**
     * GETs a random websocket authentication token for a particular event.
     */
    private static Route getWebSocketToken = (Request req, Response response) -> {
        String eventCode = req.params(":id").toUpperCase();

        Event e = DatabaseManager.getDatabaseManager().getEventFromCode(eventCode);

        if (e == null) { // If the event doesn't exist...
            Document d = new Document();
            d.append("token", ""); // Give them an empty token
            return d;
        }

        // TODO: check if event's host ID = web session's user ID.

        char[] possibleChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

        String webSocketToken = "";
        do {
            webSocketToken = "";
            for (int i = 0; i < 32; i++) {
                webSocketToken += possibleChars[rng.nextInt(possibleChars.length)];
            }
        } while (webSocketTokens.containsKey(webSocketToken)); // Keep generating until it's unique

        webSocketTokens.put(webSocketToken, eventCode);
        Document webSocketTokenContainer = new Document();
        webSocketTokenContainer.append("token", webSocketToken);
        return webSocketTokenContainer;
    };

    /**
     * Checks a particular WebSocket authentication token, and returns the
     * associated event code if valid. Will also remove it from the map, so it
     * cannot be reused. Will return <code>null</code> if the token is invalid.
     * 
     * @param token
     * @return
     */
    public static String checkWebSocketToken(String token) {
        if (webSocketTokens.containsKey(token)) {
            return webSocketTokens.remove(token);
        }
        return null;
    }

    /**
     * GETs the details for a particular event. Will check who the logged in user is
     * to determine whether to return the host data or the attendee data.
     */
    public static Route getEvent = (Request req, Response res) -> {
        // return new Event("0", "Event Code Test");

        String eventCode = req.params(":id").toUpperCase();
        Event event = DatabaseManager.getDatabaseManager().getEventFromCode(eventCode);

        if (event != null) {
            if (event.getHostID().equals(req.session().attribute("uid"))) {
                return APIResponse.success(event.getHostViewDocument());
            }
            return APIResponse.success(event.getAttendeeViewDocument());
        }

        return APIResponse.error("Could not match event code");
    };

    /**
     * Creates a new event from JSON data of an event. WARNING: this has security
     * vulnerabilities, as the user can insert whatever eventCode/hostID, etc, into
     * the event. The current state will just take the data and create an event
     * object out of it. Will also allow for multiple events having the same event
     * code, if the eventCode is supplied in the JSON. For early prototypes, it's
     * fine.
     */
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

            // TODO: add more security, e.g. someone can create an event with a custom code
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

    /**
     * <<<<<<< HEAD Receives the the feedback form from the user to be analysed and
     * then send this data to the host's view ======= When feedback is received.
     * >>>>>>> d94def0978b23c9470a1ff36e0628f19c017738f
     */
    public static Route checkData = (Request req, Response res) -> {
        System.out.println(req.body());
        return APIResponse.success("ok");
    };

}
