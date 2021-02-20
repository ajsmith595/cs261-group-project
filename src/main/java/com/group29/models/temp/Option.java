package com.group29.models.temp;

public class Option {
    protected String name;
    protected int number;

    public Option(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }
}
