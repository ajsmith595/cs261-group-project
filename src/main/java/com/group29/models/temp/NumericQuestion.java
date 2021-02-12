package com.group29.models.temp;

public class NumericQuestion extends Question {
    protected Stats stats;
    protected int min_value;
    protected int max_value;
    protected long min_time;
    protected long max_time;
    protected Point[] points;
    protected long current_time;

    public NumericQuestion(String title, Stats stats, int min_value, int max_value, long min_time, long max_time,
            long current_time, Point[] points) {
        this.type = "numeric";
        this.title = title;
        this.stats = stats;
        this.min_value = min_value;
        this.max_value = max_value;
        this.min_time = min_time;
        this.max_time = max_time;
        this.points = points;
        this.current_time = current_time;
    }
}
