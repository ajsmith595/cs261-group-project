package com.group29.controllers;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.*;

@WebSocket
public class WebSocketController {

    @OnWebSocketConnect
    public void connected(Session session) {
        // do something
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        // do something else
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        // do something else else
    }

}
