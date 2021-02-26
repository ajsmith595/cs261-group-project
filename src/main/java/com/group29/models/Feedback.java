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
    private String userID;
    @Expose
    private boolean anonymous;
    @Expose
    private List<Response> responses;

    private Feedback(String id, String userID, boolean anonymous, List<Response> responses) {
        this.id = id;
        this.userID = userID;
        this.anonymous = anonymous;
        this.responses = responses;
    }

    public static Feedback generateFeedbackFromDocument(Document doc) {
        // Grabs the information from the document
        // String id = doc.getObjectId("_id").toHexString();
        String userID = doc.getString("userID");
        boolean anonymous = doc.getBoolean("anonymous");
        List<Response> responses = doc.getList("responses", Document.class).stream()
                .map(x -> Response.generateResponseFromDocument(x)).collect(Collectors.toList());

        // Returns a new event using the data obtained
        return new Feedback(null, userID, anonymous, responses);
    }

    public void setUserID(String userID) {
        this.userID = userID;
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