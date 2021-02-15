package com.group29.models.questions;

import java.util.List;

public class Rating extends Question
{
    @Expose
    int minimumValue;
    @Expose
    int maximumValue;

    public Rating()
    {
        super();
    }
}