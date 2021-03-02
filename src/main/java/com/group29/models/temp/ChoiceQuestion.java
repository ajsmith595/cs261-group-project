package com.group29.models.temp;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;

public class ChoiceQuestion extends Question {
    protected Option[] options;
    protected boolean multiple;

    public ChoiceQuestion(Document doc)
    {
        super("choice", doc.getString("title"));
        multiple = doc.getBoolean("multiple");
        
        options = doc.getList("options", Document.class).stream().map(x -> new Option(x.getString("name"), x.getInteger("number"))).toArray(Option[]::new);

    }

    public ChoiceQuestion(String title, Option[] options, boolean multiple) {
        super("choice", title);
        this.options = options;
        this.multiple = multiple;
    }

    public boolean getMultiple() {
        return this.multiple;
    }

    public Option[] getOptions() {
        return this.options;
    }

<<<<<<< HEAD
=======
    @Override
    public Document getQuestionAsDocument()
    {
        // Creates a blank document
        Document doc = new Document();

        // Fills the document with data
        doc.append("_id", new ObjectId(id));
        doc.append("title", title);
        doc.append("type", type);

        doc.append("multiple", multiple);
        doc.append("options", Arrays.stream(options).map(x -> x.getAsDocument()).collect(Collectors.toList()));

        // Returns this filled document
        return doc;
    }
>>>>>>> 59b67fb9bf8152df489ac53cefb9b95941469b8c
}
