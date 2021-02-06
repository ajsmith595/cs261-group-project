package com.group29;

import static spark.Spark.*;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import com.group29.controllers.APIController;
import com.group29.controllers.IndexController;
import com.group29.controllers.WebSocketController;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;

public class Program {

    public static void main(String[] args) {
        // WebSockets must come before HTTP routes
        staticFiles.location("/client");
        // Static Files must come before HTTP routes
        webSocket("/socket", WebSocketController.class);

        path("/api", APIController.routes);
        path("/", IndexController.routes);

    }
}