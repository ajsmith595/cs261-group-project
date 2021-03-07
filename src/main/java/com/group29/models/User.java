package com.group29.models;

import org.bson.Document;
import org.bson.BsonWriter;
import org.bson.BsonReader;
import org.bson.codecs.Codec;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import com.google.gson.annotations.Expose;

public class User {
    private String id;
    @Expose
    private String email;
    @Expose
    private String username;
    private String currentEventID;

    /**
     * Constructs a user object
     * 
     * @param id The ID of the user
     * @param email The email of the user
     * @param username The username of the user
     * @param currentEventID the event they are currently viewing
     */
    public User(String id, String email, String username, String currentEventID) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.currentEventID = currentEventID;
    }

    /**
     * Gets the ID of the user
     * 
     * @return the ID
     */
    public String getID() {
        return id;
    }

    /**
     * Gets the email of the user
     * 
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the username of the user
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the ID of the user
     * 
     * @param id The ID of the user
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Gets the user as a MongoDB Document
     * 
     * @return Document
     */
    public Document getUserAsDocument() {
        // Creates a blank document
        Document doc = new Document();

        // Fills the document with data
        // Includes the id if one exists
        if (id != null)
            doc.append("_id", new ObjectId(id));
        doc.append("email", email);
        doc.append("username", username);
        // Includes current event if one exists
        if (currentEventID != null)
            doc.append("currentEventID", currentEventID);

        // Returns the filled document
        return doc;
    }

    // Codec class to allow MongoDB to automatically create User classes
    public static class UserCodec implements Codec<User> {
        // Document codec to read raw BSON
        private Codec<Document> documentCodec;

        /**
         * Constructor for the codec
         */
        public UserCodec() {
            this.documentCodec = new DocumentCodec();
        }

        /**
         * Encodes an User into the writer
         * 
         * @param writer         Writer to write into
         * @param value          User to write
         * @param encoderContext Context for the encoding
         */
        @Override
        public void encode(final BsonWriter writer, final User value, final EncoderContext encoderContext) {
            documentCodec.encode(writer, value.getUserAsDocument(), encoderContext);
        }

        /**
         * Decodes an User from a BSON reader
         * 
         * @param reader         The reader to decode
         * @param decoderContext Context for the decoding
         * @return
         */
        @Override
        public User decode(final BsonReader reader, final DecoderContext decoderContext) {
            // Generates a document from the reader
            Document doc = documentCodec.decode(reader, decoderContext);

            // Grabs the information from the document
            String id = doc.getObjectId("_id").toHexString();
            String email = doc.getString("email");
            String username = doc.getString("username");
            String currentEventID = doc.getString("currentEventID");

            // Returns a new user using the data obtained
            return new User(id, email, username, currentEventID);
        }

        /**
         * Gets the class type for this encoder
         * 
         * @return The User class type
         */
        @Override
        public Class<User> getEncoderClass() {
            return User.class;
        }
    }

}
