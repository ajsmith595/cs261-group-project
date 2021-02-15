package com.group29.controllers;

import java.util.Date;

import com.group29.models.DatabaseManager;
import com.group29.models.APIResponse;

import static spark.Spark.*;

import com.group29.JSONTransformer;
import com.group29.models.Event;
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
        //get("/event/:id", "application/json", APIController.getSession, new JSONTransformer());
        /*
         * Makes it return in JSON format. Will automatically convert regular Java
         * classes to JSON. Will keep all fields (private/public/protected) but will
         * discard functions.
         */
        get("/event/:id", APIController.getSession, new JSONTransformer());
        post("/events", APIController.postEvent, new JSONTransformer());

        post("/templates/", APIController.postTemplate, new JSONTransformer());
    };

    public static Route getSession = (Request req, Response res) -> {
        //return new Event("0", "Event Code Test");
        
        String eventCode = req.params(":id");
        Event event = DatabaseManager.getDatabaseManager().getEventFromCode(eventCode);

        if (event != null)
            return APIResponse.success(event);
        
        return APIResponse.error("Could not match event code");
    };

    // Creates a new event
    public static Route postEvent = (Request req, Response res) -> {
        // Sets the return type to json
        res.type("application/json");
        // Catches parsing errors
        try {
            // Creates a GSON parser that can parse dates and excludes id and eventcode fields
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()))
                .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (date, type, jsonSerializationContext) -> new JsonPrimitive(date.getTime()))
                .excludeFieldsWithoutExposeAnnotation()
                .create();

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

    // Creates a template
    public static Route postTemplate = (Request req, Response res) -> {
        // Sets the return type to json
        res.type("application/json");
        // Catches parsing errors
        try {
            // Creates a GSON parser that can parse dates and excludes id and eventcode fields
            Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

            // Attempts to parse event
            Template template = gson.fromJson(req.body(), Template.class);

            return APIResponse.success(new Gson().toJSON(template));
        } catch (Exception e) {
            // Prints the error to console
            e.printStackTrace();
        }
        // Returns an error response
        return APIResponse.error("Could not create the template.");
    };
}
