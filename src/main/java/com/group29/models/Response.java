package com.group29.models;

import com.google.gson.annotations.Expose;

import org.bson.Document;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.BsonReader;
import org.bson.codecs.Codec;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.ObjectIdCodec;
import org.bson.types.ObjectId;

public class Response {
    private String id;
    private String questionId;
    private String response;

    public Response(String id, String questionId, String response) {
        this.id = id;
        this.questionId = questionId;
        this.response = response;
    }

    public String getId() {
        return id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getResponse() {
        return response;
    }
}
