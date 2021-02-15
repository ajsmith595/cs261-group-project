package com.group29.models.questions;

import com.google.gson.annotations.Expose;

public abstract class Question
{
    protected String id;
    @Expose
    protected String question;
    @Expose
    protected String type;
    @Expose
    protected boolean isOneTime;

    // Protected constructor to prevent instantiation
    protected Question()
    {

    }
}