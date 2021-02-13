package com.group29.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import com.group29.JSONTransformer;
import com.group29.models.temp.ChoiceQuestion;
import com.group29.models.temp.NumericQuestion;
import com.group29.models.temp.OpenQuestion;
import com.group29.models.temp.Option;
import com.group29.models.temp.Point;
import com.group29.models.temp.Question;
import com.group29.models.temp.QuestionResponse;
import com.group29.models.temp.Stats;
import com.group29.models.temp.Trend;
import com.group29.models.temp.WebSocketData;

import org.eclipse.jetty.websocket.api.Session;

public class Event {
    private String id; // database ID
    private String eventCode; // event code used by clients
    private ArrayList<Session> clients;
    private WebSocketData data;

    public Event(String id, String eventCode) {
        this.id = id;
        this.eventCode = eventCode;
        this.clients = new ArrayList<Session>();

        this.updateData();
    }

    public String getID() {
        return this.id;
    }

    public String getEventCode() {
        return this.eventCode;
    }

    public void addClient(Session s) {
        clients.add(s);
        try {
            s.getRemote().sendString((new JSONTransformer()).render(this.data)); // immediately send data to client
        } catch (Exception e) {
            System.out.println("Error occured: " + e.getMessage());
        }
    }

    private static ArrayList<QuestionResponse> all_responses = new ArrayList<>(
            Arrays.asList(new QuestionResponse[] { new QuestionResponse("Very interesting and intriguing", null, "a"),
                    new QuestionResponse("Something else....", "ajsmith595", "b"),
                    new QuestionResponse("Pretty boring", "haterman443", "c"),
                    new QuestionResponse("Clearly well-educated", "KrazyKid69", "d"),
                    new QuestionResponse("Joy is at a low point here", null, "e"),
                    new QuestionResponse("Confusing", null, "f") }));
    private static int index = 0;

    public void updateData() {
        Trend energetic = new Trend("Energetic", 30 + (int) ((Math.random() - 0.5) * 20));
        Trend interesting = new Trend("Interesting", 40 + (int) ((Math.random() - 0.5) * 10));
        Trend inspiring = new Trend("Inspiring", 50 + (int) ((Math.random() - 0.5) * 45));

        ArrayList<QuestionResponse> qrs = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            qrs.add(all_responses.get((index + i) % all_responses.size()));
        }
        index += 1;

        OpenQuestion oq = new OpenQuestion("General Feedback", qrs.toArray(new QuestionResponse[0]),
                new Trend[] { energetic, interesting, inspiring });
        ChoiceQuestion cq1 = new ChoiceQuestion("What is your favourite colour?",
                new Option[] { new Option("Red", (int) Math.floor(100 * Math.random())),
                        new Option("Yellow", (int) Math.floor(100 * Math.random())),
                        new Option("Green", (int) Math.floor(100 * Math.random())) });
        ChoiceQuestion cq2 = new ChoiceQuestion("What age group are you in?",
                new Option[] { new Option("18-24", (int) Math.floor(100 * Math.random())),
                        new Option("25-39", (int) Math.floor(100 * Math.random())),
                        new Option("40-59", (int) Math.floor(100 * Math.random())),
                        new Option("60+", (int) Math.floor(100 * Math.random())) });

        ArrayList<Point> points = new ArrayList<Point>();
        Calendar c = Calendar.getInstance();
        long currentTime = c.getTimeInMillis() / 1000;
        c.set(Calendar.MINUTE, 0);
        long startTime = c.getTimeInMillis() / 1000;
        c.set(Calendar.HOUR, c.get(Calendar.HOUR) + 1);
        long endTime = c.getTimeInMillis() / 1000;
        for (long i = startTime; i < currentTime; i += 60 * 5) {
            points.add(new Point(i, (float) (Math.random() * 10)));
        }
        float currentValue = (float) (Math.random() * 10);
        points.add(new Point(currentTime, currentValue));

        NumericQuestion nq = new NumericQuestion("How would you rate this event?", new Stats(currentValue, 5, 10), 0,
                10, startTime, endTime, currentTime, points.toArray(new Point[0]));
        WebSocketData wsd = new WebSocketData(new Question[] { oq, cq1, cq2, nq });
        this.data = wsd;
    }

    public void sendDataToClients() {

        String data = (new JSONTransformer()).render(this.data);
        try {
            ArrayList<Session> closedSessions = new ArrayList<Session>();
            for (Session s : clients) {
                if (!s.isOpen()) {
                    closedSessions.add(s);
                } else {
                    s.getRemote().sendString(data);
                }
            }
            for (Session s : closedSessions) {
                clients.remove(s);
            }
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage() + " => " + e.getCause() + " => "
                    + e.getStackTrace().toString());
        }
    }
}
