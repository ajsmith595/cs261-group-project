package com.group29.controllers;

import static spark.Spark.*;

import java.security.SecureRandom;
import java.util.HashMap;

import com.group29.JSONTransformer;
import com.group29.models.Event;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.RouteGroup;

public class APIController {
    public static RouteGroup routes = () -> {
        get("/event/:id/token", "application/json", APIController.getWebSocketToken, new JSONTransformer());
        get("/event/:id", "application/json", APIController.getSession, new JSONTransformer());
        /*
         * Makes it return in JSON format. Will automatically convert regular Java
         * classes to JSON. Will keep all fields (private/public/protected) but will
         * discard functions.
         */
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

    private static Route getSession = (Request req, Response res) -> {
        return new Event("ID test", "Event Code Test");
    };
}
