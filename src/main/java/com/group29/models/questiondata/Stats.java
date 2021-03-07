package com.group29.models.questiondata;

public class Stats {
    protected double currentValue;
    protected double overallAverage;
    protected double minValue;
    protected double maxValue;

    /**
     * Constructs a stats object
     * 
     * @param currentValue The current value of the Numeric question
     * @param overallAverage The average from all responses
     * @param minValue The smallest value from a response
     * @param maxValue The largest value from a response
     */
    public Stats(double currentValue, double overallAverage, double minValue, double maxValue) {
        this.currentValue = currentValue;
        this.overallAverage = overallAverage;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Gets the current value of the question
     * 
     * @return the current value
     */
    public double getCurrentValue() {
        return this.currentValue;
    }

    /**
     * Gets the minimum value of the question
     * 
     * @return the minimum value
     */
    public double getMinValue() {
        return this.minValue;
    }

    /**
     * Gets the max value of the question
     * 
     * @return the max value
     */
    public double getMaxValue() {
        return this.maxValue;
    }
}
