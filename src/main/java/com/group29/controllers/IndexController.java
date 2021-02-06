package com.group29.controllers;

import static spark.Spark.*;

import java.util.Map;

import com.group29.JSONTransformer;
import com.group29.models.Event;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.RouteGroup;
import spark.template.velocity.VelocityTemplateEngine;

public class IndexController {

    // Front-end routing will be used so that we can do page transitions. Also makes
    // making a PWA a nicer experience.
    // Pretty much means we'll pass the same webpage regardless of the path we get,
    // and React will look at the URL and render the right page

    // That being said, we should do server-side checks when API calls are made e.g.
    // making sure a user is registered
    public static RouteGroup routes = () -> {
        get("/", IndexController.getIndex); // home view
        get("/register", IndexController.getIndex); // register view
        get("/event/create", IndexController.getIndex); // event creation view
        get("/event/:id", IndexController.getIndex); // particular event host/attendee view
        get("/events", IndexController.getIndex); // list of current user's events
        get("*", (req, res) -> {
            return new VelocityTemplateEngine().render(new ModelAndView(Map.of(), "client/error/404.html"));
        });
    };

    public static Route getIndex = (Request req, Response res) -> {
        return new VelocityTemplateEngine().render(new ModelAndView(Map.of(), "client/index.html"));
    };
}
