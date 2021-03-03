package com.group29.controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.Arrays;

import com.group29.models.DatabaseManager;
import com.group29.models.APIResponse;

import static spark.Spark.*;

import java.security.SecureRandom;
import java.util.HashMap;

import com.group29.JSONTransformer;
import com.group29.models.Event;
import com.group29.models.User;
import com.group29.models.temp.Question;
import com.group29.models.Feedback;
import com.group29.models.Template;
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

        before("/*", (req, res) -> {
            res.header("Access-Control-Allow-Origin", "http://localhost:3000");
            res.header("Access-Control-Allow-Credentials", "true");
            if (!req.requestMethod().equals("OPTIONS")) {
                boolean isRegisterOrLogin = req.uri().equals("/api/register") || req.uri().equals("/api/login");
                boolean isLoggedIn = req.session().attribute("uid") != null;
                // If not logged in, and not trying to login/register, stop!
                if (!isLoggedIn && !isRegisterOrLogin) {
                    halt(401, (new JSONTransformer()).render(APIResponse.error("Not authenticated")));
                } else if (isLoggedIn && isRegisterOrLogin) {
                    halt(401, (new JSONTransformer()).render(APIResponse.error("Already authenticated")));
                }
            }
        });
        // Gets run after every request, just before the response is actually sent to
        // the user. Used for development environments.

        // Get WebSocket authentication token for a particular event
        get("/event/:id/token", "application/json", APIController.getWebSocketToken, new JSONTransformer());

        // Get event data
        get("/event/:id", "application/json", APIController.getEvent, new JSONTransformer());

        // Create a new event
        post("/events", APIController.postEvent, new JSONTransformer());
        // Creates new template
        post("/templates", APIController.postTemplate, new JSONTransformer());
        // Gets a template with the given id
        get("/template/:id", APIController.getTemplate, new JSONTransformer());

        // Add new feedback for an event
        post("/event/:id/feedback", "application/json", APIController.postFeedback, new JSONTransformer());
        /*
         * Makes it return in JSON format. Will automatically convert regular Java
         * classes to JSON. Will keep all fields (private/public/protected) but will
         * discard functions.
         */
        // Register a user
        post("/register", APIController.createUser, new JSONTransformer());
        // Login a user
        post("/login", APIController.loginUser, new JSONTransformer());
        // Logout a user
        post("/logout", APIController.logoutUser, new JSONTransformer());
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

        if (!e.getHostID().equals(req.session().attribute("uid"))) { // If it's not the host
            Document d = new Document();
            d.append("token", ""); // Give them an empty token
            return d;
        }

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
            if (req.queryParams("force_host") != null || event.getHostID().equals(req.session().attribute("uid"))) {
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

            // Attempts to parse event
            System.out.println(req.body());
            Event event = gson.fromJson(req.body(), Event.class);
            event.setHostID(req.session().attribute("uid")); // Set it to the current session's ID
            // Generates a new code for the event
            event.generateEventCode();

            Question[] questions = Template.parseQuestionsFromJSON(gson, req.body());
            Template t = new Template(null, req.session().attribute("uid"), Arrays.asList(questions));
            String templateID = DatabaseManager.getDatabaseManager().addTemplate(t);
            event.setTemplateID(templateID);
            // Adds it to the database and returns the event code
            DatabaseManager.getDatabaseManager().addEvent(event);
            return APIResponse.success(new Document("eventCode", event.getEventCode()));
        } catch (ClassCastException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return APIResponse.error("Invalid event!");
        } catch (Exception e) {
            // Prints the error to console
            e.printStackTrace();
        }
        // Returns an error response
        return APIResponse.error("Could not create the event.");
    };

    // Gets the user ID from email and password
    public static Route getUser = (Request req, Response res) -> {
        String userId = req.params(":id");
        User user = DatabaseManager.getDatabaseManager().getUserFromID(userId);

        if (user != null)
            return APIResponse.success(user);

        return APIResponse.error("The user requested could not be found");
    };

    // Registers a new user
    public static Route createUser = (Request req, Response res) -> {
        // Sets the return type to json
        res.type("application/json");
        // Catches parsing errors
        try {
            // Creates a GSON parser that can parse dates and excludes id
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

            // Attempts to parse the user
            User user = gson.fromJson(req.body(), User.class);

            // Checks if the user already exists
            if (DatabaseManager.getDatabaseManager().checkUser(user.getEmail()))
                return APIResponse.error("A user with the given email already exists.");

            // Adds it to the database and returns the user id
            DatabaseManager.getDatabaseManager().addUser(user);
            req.session().attribute("uid", user.getID());
            return APIResponse.success(new Document());
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
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String userID = req.session().attribute("uid");

            // Attempts to parse the feedback as well as the id of the event
            Feedback feedback = gson.fromJson(req.body(), Feedback.class);
            feedback.setUserID(userID);
            feedback.setTimestamp(Calendar.getInstance().getTimeInMillis());
            if(!DatabaseManager.getDatabaseManager().canSendFeedback(userID,eventCode)){
                return APIResponse.error("Feedback was sent recently");
            }
            // Attempts to add the feedback to the database
            boolean result = DatabaseManager.getDatabaseManager().addFeedback(eventCode, feedback);

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

    

    // Posts a new template
    public static Route postTemplate = (Request req, Response res) -> {
        // Sets the return type to json
        res.type("application/json");
        // Catches parsing errors
        try {
            // Creates a GSON parser that can parse dates and excludes id
            Gson gson = new GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .registerTypeAdapter(Question.class, Question.deserialiser)
                                .create();

            // Attempts to parse the feedback as well as the id of the event
            Template template = gson.fromJson(req.body(), Template.class);

            // Attempts to add the feedback to the database
            String id = DatabaseManager.getDatabaseManager().addTemplate(template);
            // Responds with whether it worked
            if (id == null)
                return APIResponse.error("The template could not be added.");
            return APIResponse.success(new Document("id", id));
        } catch (Exception e) {
            // Prints the error to console
            e.printStackTrace();
        }
        // Returns an error response
        return APIResponse.error("The template could not be added.");
    };


    /**
     * GETs the details for a particular template.
     */
    public static Route getTemplate = (Request req, Response res) -> {

        String templateID = req.params(":id");
        Template template = DatabaseManager.getDatabaseManager().getTemplate(templateID);

        if (template != null) {
            return APIResponse.success(template.getTemplateAsDocument());
        }

        return APIResponse.error("Could not find the template");
    };

    /**
     * Handles the login attempt sent by a user Returns either an error with a
     * message or success
     */
    public static Route loginUser = (Request req, Response res) -> {

        // Creates a GSON parser that can parse dates and excludes id
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        // Attempts to parse the feedback as well as the id of the event
        User user = gson.fromJson(req.body(), User.class);
        User dbUser = DatabaseManager.getDatabaseManager().getUserFromDetails(user.getEmail(), user.getUsername());
        if (dbUser == null) {
            return APIResponse.error("User not found");
        }

        req.session().attribute("uid", dbUser.getID());
        return APIResponse.success(new Document());
    };

    public static Route logoutUser = (Request req, Response res) -> {
        req.session().removeAttribute("uid");
        return APIResponse.success(new Document());
    };
}
