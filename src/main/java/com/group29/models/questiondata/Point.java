package com.group29.models.questiondata;

public class Point {
    protected long time;
    protected double value;

    public Point(long time, double value) {
        this.time = time;
        this.value = value;
    }

    public long getTime() {
        return this.time;
    }

    public double getValue() {
        return this.value;
    }
}
