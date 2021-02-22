package com.group29.models.temp;

public class Point {
    protected long time;
    protected float value;

    public Point(long time, float value) {
        this.time = time;
        this.value = value;
    }

    public long getTime() {
        return this.time;
    }

    public float getValue() {
        return this.value;
    }
}
