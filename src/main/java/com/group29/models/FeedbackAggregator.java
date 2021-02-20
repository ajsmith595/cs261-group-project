package com.group29.models;

import java.util.List;

public class FeedbackAggregator {
    // Instance of the feedback aggregator
    private static FeedbackAggregator feedbackAggregator = new FeedbackAggregator();
//    private

    /**
     * Gets the singleton instance of the feedback aggregator
     * @return The main instance of the feedback aggregator
     */
    public static FeedbackAggregator getFeedbackAggregator() {
        return feedbackAggregator;
    }

    public void collateFeedback(String eventID) {
        Event event = DatabaseManager.getDatabaseManager().getEventFromCode(eventID);

        List<Feedback> fb = DatabaseManager.getDatabaseManager().getFeedback(event.getID());


    }
}
