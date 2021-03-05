package com.group29.models.questiondata;

public class Trend {
    protected String phrase;
    protected int proportion;

    public Trend(String phrase, int proportion) {
        this.phrase = phrase;
        this.proportion = proportion;
    }

    public String getPhrase() {
        return this.phrase;
    }

    public int getProportion() {
        return this.proportion;
    }
}
