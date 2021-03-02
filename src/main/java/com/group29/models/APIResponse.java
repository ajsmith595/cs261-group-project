package com.group29.models;

import com.google.gson.annotations.SerializedName;

// Class for API responses
public class APIResponse {
    // Response status for the response
    // SerializedName when converting to json
    enum ResponseStatus {
        @SerializedName("success")
        SUCCESS, @SerializedName("error")
        ERROR;
    };

    // The status of the response
    private ResponseStatus status;

    // The message/data of the response
    private String message;
    private Object data;

    /**
     * Private constructor, success or error should be used instead
     * 
     * @param status The status of the response
     */
    private APIResponse(ResponseStatus status) {
        this.status = status;
    }

    /**
     * Creates a success response with the following data
     * 
     * @param data The data to respond with
     */
    public static APIResponse success(Object data) {
        APIResponse response = new APIResponse(ResponseStatus.SUCCESS);
        response.data = data;
        return response;
    }

    /**
     * Creates an error response with the following message
     * 
     * @param message The error message to respond with
     * @return APIResponse
     */
    public static APIResponse error(String message) {
        APIResponse response = new APIResponse(ResponseStatus.ERROR);
        response.message = message;
        return response;
    }
}