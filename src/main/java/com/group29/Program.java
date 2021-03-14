package com.group29;

import static spark.Spark.*;
import spark.template.velocity.VelocityTemplateEngine;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import javax.servlet.DispatcherType;
import com.group29.controllers.APIController;
import com.group29.controllers.IndexController;
import com.group29.controllers.WebSocketController;
import com.group29.models.APIResponse;
import com.group29.models.DatabaseManager;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import spark.ExceptionMapper;
import spark.ModelAndView;
import spark.Service;
import spark.Spark;
import spark.embeddedserver.jetty.EmbeddedJettyServer;
import spark.http.matching.MatcherFilter;
import spark.route.Routes;
import spark.staticfiles.StaticFilesConfiguration;

public class Program {
    // The below fix was got from
    // https://gist.github.com/concision/f175bb5dd42c524bedd633af80903b9f
    // It fixes the 404 not being handled via the notFound method, when both static
    // files and WebSockets are used.
    // #region 404 FIX

    /**
     * Call {@link Spark#awaitInitialization()} prior to attempting an injection
     */
    public static void inject() {
        try {
            // retrieve Spark instance
            Method getInstanceMethod = Spark.class.getDeclaredMethod("getInstance");
            getInstanceMethod.setAccessible(true);
            Service service = (Service) getInstanceMethod.invoke(null);

            // retrieve embedded server wrapper
            Field serverField = Service.class.getDeclaredField("server");
            serverField.setAccessible(true);
            Object embeddedServer = serverField.get(service);

            // ensure it is a instance of a EmbeddedJettyServer
            if (!(embeddedServer instanceof EmbeddedJettyServer)) {
                throw new UnsupportedOperationException("Only EmbeddedJettyServer is supported");
            }
            EmbeddedJettyServer embeddedJettyServer = (EmbeddedJettyServer) embeddedServer;

            // retrieve the real server
            Field jettyServerField = EmbeddedJettyServer.class.getDeclaredField("server");
            jettyServerField.setAccessible(true);
            Server server = (Server) jettyServerField.get(embeddedJettyServer);

            // steal some handlers
            HandlerList handler = (HandlerList) server.getHandler();
            Handler[] handlers = handler.getHandlers();

            // check if a websocket handler has been registered
            // index 0 is the basic web handler
            // index 1 only exists when there is a websocket registered
            if (2 <= handlers.length) {
                // retrieve handler
                Handler websocketHandler = handlers[1];
                ServletContextHandler websocketContextHandler = (ServletContextHandler) websocketHandler;
                // inject the default web handler
                websocketContextHandler
                        .addFilter(
                                new FilterHolder(new MatcherFilter(Routes.create(), new StaticFilesConfiguration(),
                                        new ExceptionMapper(), false, false)),
                                "/*", EnumSet.of(DispatcherType.REQUEST));
            }
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("failed to inject 404 route handling", exception);
        }
    }

    public static void blockingInject() {
        awaitInitialization();
        inject();
    }

    public static void asyncInject() {
        CompletableFuture.runAsync(Program::blockingInject);
    }

    // #endregion 404 FIX

    public static void main(String[] args) {
        try {

            // Read the properties file
            PropertiesReader pr = new PropertiesReader("server.properties");
            port(Integer.parseInt(pr.getProperty("port")));
            DatabaseManager.getDatabaseManager().init(pr.getProperty("databaseHost"));
        } catch (Exception e) {
            System.out.println("Invalid or missing server.properties file!");
            return;
        }
        // WebSockets must come before HTTP routes
        staticFiles.location("/client");
        // Static Files must come before HTTP routes

        notFound((req, res) -> {
            if (req.uri().startsWith("/api/")) {
                res.header("Content-Type", "application/json");
                return (new JSONTransformer()).render(APIResponse.error("Method not found"));
            }
            return new VelocityTemplateEngine().render(new ModelAndView(Map.of(), "client/static/error/404.html"));
        });

        webSocket("/socket", WebSocketController.class);
        path("/api", APIController.routes);
        path("/", IndexController.routes);

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                if (i == 60) {
                    i = 0;
                    WebSocketController.sendEventData(true); // Force send to all clients every minute
                } else {
                    WebSocketController.sendEventData();
                }
                i++;
            }
        }, 0, 1000); // Update events every second

        asyncInject(); // 404 fix

    }
}