package com.group29.models;

import com.google.gson.annotations.Expose;

import org.bson.Document;
import org.bson.BsonWriter;
import org.bson.BsonReader;
import org.bson.codecs.Codec;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

public class Response {
    private String id;
    @Expose
    private String questionID;
    @Expose
    private Object response;

    private Response(String id, String questionID, Object response) {
        this.id = id;
        this.questionID = questionID;
        this.response = response;
    }

    public String getQuestionID() {
        return this.questionID;
    }

    public Object getResponse() {
        return this.response;
    }

    /**
     * Gets the feedback as a MongoDB Document
     * 
     * @return Document
     */
    public Document getResponseAsDocument() {
        // Creates a blank document
        Document doc = new Document();

        // Fills the document with data
        // Includes the id if one exists
        if (id != null)
            doc.append("_id", new ObjectId(id));
        doc.append("questionID", questionID);
        doc.append("response", response);

        // Returns the filled document
        return doc;
    }

    public static Response generateResponseFromDocument(Document doc) {
        // Grabs the information from the document
        // String id = doc.getObjectId("_id").toHexString();
        String questionID = doc.getString("questionID");
        Object response = doc.get("response");

        // Returns a new event using the data obtained
        return new Response(null, questionID, response);
    }

    // Codec class to allow MongoDB to automatically create Response classes
    public static class ResponseCodec implements Codec<Response> {
        // Document codec to read raw BSON
        private Codec<Document> documentCodec;

        /**
         * Constructor for the codec
         */
        public ResponseCodec() {
            this.documentCodec = new DocumentCodec();
        }

        /**
         * Encodes an Response into the writer
         * 
         * @param writer         Writer to write into
         * @param value          Response to write
         * @param encoderContext Context for the encoding
         */
        @Override
        public void encode(final BsonWriter writer, final Response value, final EncoderContext encoderContext) {
            documentCodec.encode(writer, value.getResponseAsDocument(), encoderContext);
        }

        /**
         * Decodes an Response from a BSON reader
         * 
         * @param reader         The reader to decode
         * @param decoderContext Context for the decoding
         * @return
         */
        @Override
        public Response decode(final BsonReader reader, final DecoderContext decoderContext) {
            // Generates a document from the reader
            Document doc = documentCodec.decode(reader, decoderContext);

            return generateResponseFromDocument(doc);
        }

        /**
         * Gets the class type for this encoder
         * 
         * @return The Response class type
         */
        @Override
        public Class<Response> getEncoderClass() {
            return Response.class;
        }
    }
}
