package com.group29;

import spark.*;
import com.google.gson.Gson;

public class JSONTransformer implements ResponseTransformer {
    private Gson gson = new Gson();

    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }
}
