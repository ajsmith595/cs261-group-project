package com.group29.controllers;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.group29.models.DatabaseManager;
import com.group29.models.Event;

@WebSocket
public class WebSocketController {

    /**
     * A map from event codes to <code>Event</code> instances. This should be used,
     * as data such as the current WebSocket clients is held in a particular
     * <code>Event</code> instance, so needs to be maintained.
     */
    private static HashMap<String, Event> eventMap = new HashMap<>();
    private static ReentrantLock eventMapLock = new ReentrantLock();

    /**
     * Contains a list of sessions which we are waiting on to send us an
     * authentication token for. Will be removed when an authentication token is
     * received. If it's valid, the session will be moved into the relevant
     * <code>Event.clients</code> list. If it's invalid, the WebSocket connection
     * will be closed.
     */
    private static ArrayList<Session> waitingConnections = new ArrayList<>();

    /**
     * Makes a particular event update its data. This should be done after feedback
     * has successfully been added to the database.
     * 
     * @param eventCode The event's code
     */
    public static void sendEventData() {
        sendEventData(false);
    }

    /**
     * Makes a particular event update its data. This should be done after feedback
     * has successfully been added to the database.
     * 
     */
    public static void sendEventData(boolean force) {
        ArrayList<String> emptyEvents = new ArrayList<>(); // Represents events which can be 'closed'
        eventMapLock.lock();
        try {
            for (Event e : eventMap.values()) {
                e.sendData(force);
                if (e.getNumberOfClients() == 0)
                    emptyEvents.add(e.getEventCode());
            }
            for (String e : emptyEvents) {
                eventMap.remove(e); // Cleanup unused events.
            }
        } finally {
            eventMapLock.unlock();
        }
    }

    /**
     * Updates the title, start time and duration of a particular event from the
     * database
     * 
     * @param eventCode
     */
    public static void updateEventDetails(String eventCode) {
        eventMapLock.lock();
        try {
            Event e = eventMap.get(eventCode);
            if (e != null) {
                e.updateEventDetails();
            }
        } finally {
            eventMapLock.unlock();
        }
    }

    /**
     * Sets an event as modified, so that it will send data to WebSocket clients on
     * the next update
     * 
     * @param eventCode
     */
    public static void setEventAsModified(String eventCode) {
        eventMapLock.lock();
        try {
            Event e = eventMap.get(eventCode);
            if (e != null) {
                e.setAsModified();
            }
        } finally {
            eventMapLock.unlock();
        }
    }

    /**
     * When a WebSocket connection is first made
     * 
     * @param session The WebSocket connection
     */
    @OnWebSocketConnect
    public void connected(Session session) {
        waitingConnections.add(session); // Wait for it to send a token
    }

    /**
     * When a WebSocket connection is disconnected
     * 
     * @param session    The WebSocket connection
     * @param statusCode The status of the socket
     * @param reason     The reason for the disconnect
     */
    @OnWebSocketClose
    public void disconnected(Session session, int statusCode, String reason) {
        if (waitingConnections.contains(session)) {
            waitingConnections.remove(session); // Remove it from the waiting list if it's in one.
        } else {
        }
    }

    /**
     * When a WebSocket connection sends a message. This will be the auth token,
     * given the WebSocket is following what we assume.
     * 
     * @param session The WebSocket connection
     * @param message The message beind sent
     * @throws IOException
     */
    @OnWebSocketMessage
    public void message(Session session, String message) {
        // If the WebSocket session is not waiting to be added to an event, do nothing!
        // (they're already in an event)
        if (waitingConnections.contains(session)) {
            String eventCode = APIController.checkWebSocketToken(message); // Will return null if invalid
            waitingConnections.remove(session); // Remove the WebSocket from the waiting list, no matter what
            if (eventCode == null) {
                session.close(); // Close the connection if it's invalid
            } else {
                eventMapLock.lock();
                try {
                    if (!eventMap.containsKey(eventCode)) { // If the event is NOT already known...
                        Event e = DatabaseManager.getDatabaseManager().getEventFromCode(eventCode); // get the event
                                                                                                    // data
                        if (e == null) { // If it's null, close the connection - something's gone wrong somewhere.
                            session.close();
                            return;
                        } else {
                            e.addClient(session);
                            eventMap.put(eventCode, e); // Add the event to the map
                        }
                    } else {
                        eventMap.get(eventCode).addClient(session); // Add the client to the relevant Event.
                    }
                } finally {
                    eventMapLock.unlock();
                }
            }
        }
    }

}
