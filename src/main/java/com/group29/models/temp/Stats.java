package com.group29.models.temp;

public class Stats {
    protected double current_value;
    protected double min_value;
    protected double max_value;

    public Stats(double current_value, double min_value, double max_value) {
        this.current_value = current_value;
        this.min_value = min_value;
        this.max_value = max_value;
    }

    public double getCurrentValue() {
        return this.current_value;
    }

    public double getMinValue() {
        return this.min_value;
    }

    public double getMaxValue() {
        return this.max_value;
    }
}
