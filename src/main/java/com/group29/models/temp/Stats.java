package com.group29.models.temp;

public class Stats {
    protected float current_value;
    protected int min_value;
    protected int max_value;

    public Stats(float current_value, int min_value, int max_value) {
        this.current_value = current_value;
        this.min_value = min_value;
        this.max_value = max_value;
    }
}
