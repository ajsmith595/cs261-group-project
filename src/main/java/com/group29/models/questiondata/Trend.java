package com.group29.models.questiondata;

public class Trend {
    protected String phrase;
    protected int proportion;

    /**
     * Constructs a Trend object for a trending word
     * 
     * @param phrase The trending word/phrase
     * @param proportion The proportion for how much the word is trending
     */
    public Trend(String phrase, int proportion) {
        this.phrase = phrase;
        this.proportion = proportion;
    }

    /**
     * Gets the phrase of the trend
     * 
     * @return The phrase
     */
    public String getPhrase() {
        return this.phrase;
    }

    /**
     * Gets the proportion of the word trending
     * 
     * @return the proportion
     */
    public int getProportion() {
        return this.proportion;
    }
}
