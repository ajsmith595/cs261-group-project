package com.group29.models.questiondata;

public class Point {
    protected long time;
    protected double value;

    /**
     * Constructs a point object
     * 
     * @param time The time of the point
     * @param value the value of the point
     */
    public Point(long time, double value) {
        this.time = time;
        this.value = value;
    }

    /**
     * Gets the time of the point
     * 
     * @return the time
     */
    public long getTime() {
        return this.time;
    }

    /**
     * Gets the value of the point
     * 
     * @return the value
     */
    public double getValue() {
        return this.value;
    }
}
