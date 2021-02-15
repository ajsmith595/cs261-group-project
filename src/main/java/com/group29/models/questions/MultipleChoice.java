package com.group29.models.questions;

import java.util.List;

public class MultipleChoice extends Question
{
    @Expose
    List<String> choices;

    public MultipleChoice()
    {
        super();
    }
}