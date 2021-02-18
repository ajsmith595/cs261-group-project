package com.group29.models.temp;

public class ChoiceQuestion extends Question {
    protected Option[] options;

    public ChoiceQuestion(String title, Option[] options) {
        this.type = "choice";
        this.title = title;
        this.options = options;
    }

    public Option[] getOptions() {
        return this.options;
    }
}
