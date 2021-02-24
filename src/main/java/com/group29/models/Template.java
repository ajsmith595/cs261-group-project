package com.group29.models;


import com.group29.models.temp.Question;

// TODO Temporary implementation, need to replace questions at a minimum (and ofc add Mongo Codec, etc.)
public class Template {
    private String id; // database ID
    private String hostID; // event host ID
    private Question[] questions;

    public Template(String id, String hostID, Question[] questions) {
        this.id = id;
        this.hostID = hostID;
        this.questions = questions;
    }

    public String getId() {
        return id;
    }

    public String getHostID() {
        return hostID;
    }

    public Question[] getQuestions() {
        return questions;
    }
}
