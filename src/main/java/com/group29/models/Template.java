package com.group29.models;

import com.group29.models.temp.Question;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

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

import com.google.gson.annotations.Expose;

public class Template
{
    private String id;
    @Expose
    private String userID;
    @Expose
    List<Question> questions;


    // Private constructor for template
    // Should only be created by Mongo DB
    private Template(String id, String userID, List<Question> questions)
    {
        this.id = id;
        this.userID = userID;
        this.questions = questions;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    
    /**
     * Gets the template as a MongoDB Document
     * 
     * @return Document
     */
    public Document getTemplateAsDocument() {
        // Creates a blank document
        Document doc = new Document();

        // Fills the document with data
        // Includes the id if one exists
        if (id != null)
            doc.append("_id", new ObjectId(id));
        doc.append("userID", new ObjectId(userID));
        // Creates the array if it is not already created
        if (questions == null)
            questions = new ArrayList<Question>();

        doc.append("questions", (List<Document>) (questions.stream().map(x -> x.getQuestionAsDocument())
            .collect(Collectors.toList())));

        // Returns the filled document
        return doc;
    }

    // Codec class to allow MongoDB to automatically create Template classes
    public static class TemplateCodec implements Codec<Template> {
        // Document codec to read raw BSON
        private Codec<Document> documentCodec;

        /**
         * Constructor for the codec
         */
        public TemplateCodec() {
            this.documentCodec = new DocumentCodec();
        }

        /**
         * Encodes an Template into the writer
         * 
         * @param writer         Writer to write into
         * @param value          Template to write
         * @param encoderContext Context for the encoding
         */
        @Override
        public void encode(final BsonWriter writer, final Template value, final EncoderContext encoderContext) {
            documentCodec.encode(writer, value.getTemplateAsDocument(), encoderContext);
        }

        /**
         * Decodes an Template from a BSON reader
         * 
         * @param reader         The reader to decode
         * @param decoderContext Context for the decoding
         * @return
         */
        @Override
        public Template decode(final BsonReader reader, final DecoderContext decoderContext) {
            // Generates a document from the reader
            Document doc = documentCodec.decode(reader, decoderContext);

            String id = doc.getObjectId("_id").toHexString();
            String userID = doc.getObjectId("userID").toHexString();
            List<Question> questions = doc.getList("questions", Document.class).stream().map(x -> Question.generateQuestionFromDocument(x)).collect(Collectors.toList());


            return new Template(id, userID, questions);
        }

        /**
         * Gets the class type for this encoder
         * 
         * @return The Template class type
         */
        @Override
        public Class<Template> getEncoderClass() {
            return Template.class;
        }
    }


}
