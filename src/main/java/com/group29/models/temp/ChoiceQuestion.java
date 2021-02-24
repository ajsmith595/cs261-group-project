package com.group29.models.temp;

public class ChoiceQuestion extends Question {
    protected Option[] options;
    protected boolean multiple;

    public ChoiceQuestion(String title, Option[] options) {
        this.type = "choice";
        this.title = title;
        this.options = options;
        this.multiple = false;
    }

    public ChoiceQuestion(String title, Option[] options, boolean multiple) {
        this.type = "choice";
        this.title = title;
        this.options = options;
        this.multiple = multiple;
    }

    public boolean getMultiple() {
        return this.multiple;
    }

    public Option[] getOptions() {
        return this.options;
    }

    public Option[] getOptions() {
        return options;
    }
}
