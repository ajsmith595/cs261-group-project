package com.group29.models;

public class Event {
    private String id; // database ID
    private String eventCode; // event code used by clients

    public Event(String id, String eventCode) {
        this.id = id;
        this.eventCode = eventCode;
    }

    public String getID() {
        return this.id;

    }

    public String getEventCode() {
        return this.eventCode;
    }
}
