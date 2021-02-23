package com.group29.models;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

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

public class Feedback {
    private String id;
    @Expose
    private String userID;
    @Expose
    private boolean anonymous;
    @Expose
    private List<Response> responses;
    // Timestamp in milliseconds?
    private long timestamp;

    private Feedback(String id, String userID, boolean anonymous, List<Response> responses, long timestamp) {
        this.id = id;
        this.userID = userID;
        this.anonymous = anonymous;
        this.responses = responses;
        this.timestamp = timestamp;
    }

    public static Feedback generateFeedbackFromDocument(Document doc) {
        // Grabs the information from the document
        // String id = doc.getObjectId("_id").toHexString();
        String userID = doc.getString("userID");
        boolean anonymous = doc.getBoolean("anonymous");
        long timestamp = doc.getLong("timestamp");
        List<Response> responses = doc.getList("responses", Document.class).stream()
                .map(x -> Response.generateResponseFromDocument(x)).collect(Collectors.toList());
        // Returns a new event using the data obtained
        return new Feedback(null, userID, anonymous, responses, timestamp);
    }

    public List<Response> getResponses() {
        return this.responses;
    }

    public String getUserID() {
        return this.userID;
    }

    public boolean getAnonymous() {
        return this.anonymous;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the feedback as a MongoDB Document
     * 
     * @return Document
     */
    public Document getFeedbackAsDocument() {
        // Creates a blank document
        Document doc = new Document();

        // Fills the document with data
        // Includes the id if one exists
        if (id != null)
            doc.append("_id", new ObjectId(id));
        doc.append("userID", userID);
        doc.append("anonymous", anonymous);
        doc.append("timestamp", timestamp);
        if (responses != null)
            doc.append("responses", (List<Document>) (responses.stream().map(x -> x.getResponseAsDocument())
                    .collect(Collectors.toList())));
        else
            doc.append("responses", new ArrayList<Document>());

        // Returns the filled document
        return doc;
    }

    // Codec class to allow MongoDB to automatically create Feedback classes
    public static class FeedbackCodec implements Codec<Feedback> {
        // Document codec to read raw BSON
        private Codec<Document> documentCodec;

        /**
         * Constructor for the codec
         */
        public FeedbackCodec() {
            this.documentCodec = new DocumentCodec();
        }

        /**
         * Encodes an Feedback into the writer
         * 
         * @param writer         Writer to write into
         * @param value          Feedback to write
         * @param encoderContext Context for the encoding
         */
        @Override
        public void encode(final BsonWriter writer, final Feedback value, final EncoderContext encoderContext) {
            documentCodec.encode(writer, value.getFeedbackAsDocument(), encoderContext);
        }

        /**
         * Decodes an Feedback from a BSON reader
         * 
         * @param reader         The reader to decode
         * @param decoderContext Context for the decoding
         * @return
         */
        @Override
        public Feedback decode(final BsonReader reader, final DecoderContext decoderContext) {
            // Generates a document from the reader
            Document doc = documentCodec.decode(reader, decoderContext);

            return generateFeedbackFromDocument(doc);
        }

        /**
         * Gets the class type for this encoder
         * 
         * @return The Feedback class type
         */
        @Override
        public Class<Feedback> getEncoderClass() {
            return Feedback.class;
        }
    }

}