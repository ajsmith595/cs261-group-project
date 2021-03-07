package com.group29.models.questiondata;

public class QuestionResponse {
    protected String message;
    protected String username;
    protected String id;

    /**
     * Creates a question response
     * 
     * @param message The message of the question response
     * @param username The username of the attendee who sent the response
     * @param id The id of the user who sent the response
     */
    public QuestionResponse(String message, String username, String id) {
        this.message = message;
        this.username = username;
        this.id = id;
    }

    /**
     * Gets the message of the response
     * 
     * @return The message of the response
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Gets the username of the attendee who sent the response
     * 
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Gets the ID of the attendee who sent the response
     * 
     * @return the ID
     */
    public String getID() {
        return this.id;
    }
}
