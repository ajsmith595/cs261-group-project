package com.group29.controllers;

import static spark.Spark.*;

import java.util.Map;

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
        before("/*", (req, res) -> {
            // For some reason, the Spark framework checks other routes when using before().
            // This just makes sure we don't redirect an API request.
            if (!req.uri().startsWith("/api/")) {
                boolean isRegisterOrLogin = req.uri().equals("/register") || req.uri().equals("/login");
                String uid = req.session().attribute("uid");
                boolean isLoggedIn = uid != null;
                // If you're not logged in, and you're not trying to login/register, force it
                if (!isLoggedIn && !isRegisterOrLogin) {
                    res.redirect("/login");
                    halt();
                }
                // If you're logged in, don't register/login again!
                else if (isLoggedIn && isRegisterOrLogin) {
                    res.redirect("/");
                    halt();
                }
            }
        });
        get("/register", IndexController.getIndex); // register view
        get("/login", IndexController.getIndex); // register view
        get("/logout", IndexController.getIndex); // register view
        get("/", IndexController.getIndex); // home view
        get("/event/create", IndexController.getIndex); // event creation view
        get("/event/:id", IndexController.getIndex); // particular event host/attendee view
        get("/event/:id/edit", IndexController.getIndex); // event editing

        get("/events", IndexController.getIndex); // list of current user's events
    };

    /**
     * Gets the index page
     */
    public static Route getIndex = (Request req, Response res) -> {
        return new VelocityTemplateEngine().render(new ModelAndView(Map.of(), "client/static/index.html"));
    };

}
