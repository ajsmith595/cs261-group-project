package com.group29.controllers;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.group29.JSONTransformer;
import com.group29.models.Event;
import com.group29.models.temp.*;

@WebSocket
public class WebSocketController {

    private static HashMap<String, Event> webSocketConnections = new HashMap<>();
    // a map between event codes and web socket sessions.

    private static ArrayList<Session> waitingConnections = new ArrayList<>();
    // contains any web socket that's connected, but has not yet authenticated

    public static void updateEvent(String eventCode) {
        if (eventCode == null) {
            for (Event e : webSocketConnections.values()) {
                e.updateData();
                e.sendDataToClients();
            }
        }
    }

    @OnWebSocketConnect
    public void connected(Session session) {
        waitingConnections.add(session);
    }

    // @OnWebSocketClose
    // public void closed(Session session, int statusCode, String reason) {

    // }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        if (waitingConnections.contains(session)) {
            String eventCode = APIController.checkWebSocketToken(message);
            waitingConnections.remove(session);
            if (eventCode == null) {
                session.close();
            } else {
                if (!webSocketConnections.containsKey(eventCode)) {
                    //webSocketConnections.put(eventCode, new Event("123", "abcdef")); // TODO: get event data from DB
                }
                webSocketConnections.get(eventCode).addClient(session);
            }
        }
    }

}
