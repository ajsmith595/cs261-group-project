package com.group29.controllers;

import static spark.Spark.*;

import com.group29.JSONTransformer;
import com.group29.models.Event;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.RouteGroup;

public class APIController {
    public static RouteGroup routes = () -> {
        get("/event/:id", "application/json", APIController.getSession, new JSONTransformer());
        /*
         * Makes it return in JSON format. Will automatically convert regular Java
         * classes to JSON. Will keep all fields (private/public/protected) but will
         * discard functions.
         */
        post("/event/:id/feedback", "application/json", APIController.checkData);
    };

    public static Route getSession = (Request req, Response res) -> {
        return new Event("ID test", "Event Code Test");
    };

    public static Route checkData = (Request req, Response res) -> {
        System.out.println(req.body());
        return "success";
    };
}
