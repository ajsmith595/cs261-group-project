package com.group29.models;

import com.group29.models.temp.*;

import java.util.*;

// TODO eventually rewrite to update (not entirely recalculate) data
public class FeedbackAggregator {
    // Instance of the feedback aggregator
    private static final FeedbackAggregator feedbackAggregator = new FeedbackAggregator();

    /**
     * Gets the singleton instance of the feedback aggregator
     * 
     * @return The main instance of the feedback aggregator
     */
    public static FeedbackAggregator getFeedbackAggregator() {
        return feedbackAggregator;
    }

    /**
     * Collects the feedback submitted for a specified event
     * 
     * @param event The event specified
     * @return A list of question objects summarising the event's feedback
     */
    public WebSocketData collateFeedback(Event event) {
        // Get Data
        DatabaseManager dbManager = DatabaseManager.getDatabaseManager();
        Template template = dbManager.getTemplate(event);
        Question[] questions = template.getQuestions();

        // List<Feedback> feedback = dbManager.getFeedback(event.getID()); //Commented
        // out right now as this function just gets the event and returns the list right
        // now
        List<Feedback> feedback = event.getFeedback();
        // Record responses in these lists
        List<List<UserResponse>> questionResponses = new ArrayList<>();
        // Create lists per question
        for (int i = 0; i < questions.length; i++)
            questionResponses.add(new LinkedList<>());

        int totalNumberOfResponses = 0;
        HashSet<String> uniqueUsers = new HashSet<String>();
        // Add responses to questions lists
        int feedbackNumber = 0;
        for (Feedback fb : feedback) {
            totalNumberOfResponses += 1;
            uniqueUsers.add(fb.getUserID());
            int responseNumber = 0;
            for (Response r : fb.getResponses()) {
                String key = "feedback_response_" + feedbackNumber + "_" + responseNumber;
                questionResponses.get(Integer.parseInt(r.getQuestionID()))
                        .add(new UserResponse(fb.getUserID(), r, fb.getAnonymous(), key, fb.getTimestamp()));
                responseNumber++;
            }
            feedbackNumber++;
        }
        // Calculate aggregates
        Question[] results = new Question[questions.length];
        for (int i = 0; i < questionResponses.size(); i++) {
            Question q = questions[i];
            List<UserResponse> responses = questionResponses.get(i);
            if (q instanceof OpenQuestion)
                results[i] = aggregateOpenQuestion((OpenQuestion) q, responses);
            else if (q instanceof NumericQuestion)
                results[i] = aggregateNumericQuestion((NumericQuestion) q, responses, event.getStartTime().getTime());
            else if (q instanceof ChoiceQuestion)
                results[i] = aggregateChoiceQuestion((ChoiceQuestion) q, responses);
        }

        long endTimeInMins = event.getStartTime().getTime() / 1000 / 60 + event.getDuration();
        int minsLeft = (int) (endTimeInMins - (new Date()).getTime() / 1000 / 60);
        return new WebSocketData(results, totalNumberOfResponses, uniqueUsers.size(), minsLeft);
    }

    // From https://xpo6.com/list-of-english-stop-words/
    public static final HashSet<String> stopWords = new HashSet<>(Arrays.asList("a", "about", "above", "above",
            "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also",
            "although", "always", "am", "among", "amongst", "amoungst", "amount", "an", "and", "another", "any",
            "anyhow", "anyone", "anything", "anyway", "anywhere", "are", "around", "as", "at", "back", "be", "became",
            "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below",
            "beside", "besides", "between", "beyond", "bill", "both", "bottom", "but", "by", "call", "can", "cannot",
            "cant", "co", "con", "could", "couldnt", "cry", "de", "describe", "detail", "do", "done", "down", "due",
            "during", "each", "eg", "eight", "either", "eleven", "else", "elsewhere", "empty", "enough", "etc", "even",
            "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", "fify", "fill", "find",
            "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full",
            "further", "get", "give", "go", "had", "has", "hasnt", "have", "he", "hence", "her", "here", "hereafter",
            "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "how", "however", "hundred",
            "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itself", "keep", "last",
            "latter", "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill",
            "mine", "more", "moreover", "most", "mostly", "move", "much", "must", "my", "myself", "name", "namely",
            "neither", "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not",
            "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or", "other",
            "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own", "part", "per", "perhaps", "please",
            "put", "rather", "re", "same", "see", "seem", "seemed", "seeming", "seems", "serious", "several", "she",
            "should", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone",
            "something", "sometime", "sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that",
            "the", "their", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore",
            "therein", "thereupon", "these", "they", "thickv", "thin", "third", "this", "those", "though", "three",
            "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward", "towards", "twelve",
            "twenty", "two", "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were",
            "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein",
            "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose",
            "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself",
            "yourselves", ""));

    /**
     * Analyses an open question
     * 
     * @param question  Question to analyse
     * @param responses List of responses to analyse
     * @return An open question object containing the question info, common words,
     *         and mood of the feedback
     */
    public OpenQuestion aggregateOpenQuestion(OpenQuestion question, List<UserResponse> responses) {
        Counter<String> wordCounts = new Counter<>();

        HashMap<String, UserResponse> userResponsesMap = new HashMap<>();
        for (UserResponse ur : responses) {
            userResponsesMap.put(ur.userID, ur); // only use most recent response for each user
        }

        ArrayList<UserResponse> userResponses = new ArrayList<>(userResponsesMap.values());

        for (UserResponse ur : userResponses) {
            Response r = ur.response;
            Object response = r.getResponse();
            // Remove case and punctuation
            if (response instanceof String) {
                String sanitised = ((String) response).toLowerCase().replaceAll("\\p{P}", "");
                ArrayList<String> wordsUsed = new ArrayList<>();
                for (String word : sanitised.split("\\s+")) {
                    if (!stopWords.contains(word) && !wordsUsed.contains(word)) {
                        // If not common word, and word has not already been said in current response,
                        // increment counter
                        wordCounts.inc(word);
                        wordsUsed.add(word);
                    }
                }
            }
        }
        // Sort words (could be done in Counter, using TreeMap or similar, but bad
        // practice as TreeMap should sort as a total order between keys only)
        List<Map.Entry<String, Integer>> counts = new ArrayList<>(wordCounts.getEntries());
        counts.sort(Map.Entry.<String, Integer>comparingByKey().reversed());
        // Calculate common words, and copy into array
        int trend_length = 3;
        int loop = Math.min(counts.size(), trend_length);
        Trend[] trends = new Trend[loop];
        int total = 0;
        for (int i = 0; i < loop; i++) {
            total += counts.get(i).getValue();
        }
        for (int i = 0; i < loop; i++) {
            Map.Entry<String, Integer> entry = counts.get(i);
            double proportion = (double) entry.getValue() / (double) total;
            int percent = (int) (Math.pow(proportion, 0.8) * 100);
            trends[i] = new Trend(entry.getKey(), percent);
        }

        // Copy responses into QR array
        int numResponses = Math.min(8, responses.size());
        QuestionResponse[] qrs = new QuestionResponse[numResponses];
        int j = 0;
        for (int i = responses.size() - 1; i >= responses.size() - numResponses; i--) {
            UserResponse r = responses.get(i);
            Object response = r.response.getResponse();
            if (response instanceof String) {
                String username = null;
                if (!r.anonymous) {
                    User u = DatabaseManager.getDatabaseManager().getUserFromID(r.userID);
                    if (u != null) {
                        username = u.getUsername();
                    }
                }
                qrs[j++] = new QuestionResponse((String) response, username, r.key);
            }
        }

        // Sentiment analysis
        // Sum up sentiment of each text response to get mean
        SentimentAnalyser sa = SentimentAnalyser.getSentimentAnalyser();
        double moodSum = 0.0;
        int count = 0;
        for (UserResponse r : userResponses) {
            Object response = r.response.getResponse();
            if (response instanceof String) {
                String message = (String) response;
                moodSum += sa.analyse(message);
                count++;
            }
        }
        float avg = (count == 0) ? 0f : (float) (moodSum / count);

        return new OpenQuestion(question.getTitle(), qrs, trends, avg);
    }

    private double averageValues(HashMap<String, Double> ratings) {
        int size = ratings.size();
        long sum = 0;
        for (Double i : ratings.values()) {
            sum += i;
        }
        double average = (size == 0) ? 0 : ((double) sum / size);
        return average;
    }

    /**
     * Analyses a numeric question, returns the min max and average ratings
     * 
     * @param question  Question to analyse
     * @param responses List of responses to analyse
     * @return A numeric question object containing this info
     */
    public NumericQuestion aggregateNumericQuestion(NumericQuestion question, List<UserResponse> responses,
            long startTime) {
        double sum = 0;
        double count = 0, min = 10, max = 0;
        ArrayList<Point> points = new ArrayList<>();
        HashMap<String, Double> ratings = new HashMap<>();
        int interval = 300000;
        int i = 0;
        double pointAverage = 0;
        Calendar c = Calendar.getInstance();
        long currentTime = c.getTime().getTime();
        // System.out.println("current Time: " +currentTime);
        // System.out.println("start Time: " +startTime);
        // As go through ur
        // Store each user's points in a hashmap
        // When ur is over interval
        // Average each response stored in HM
        // Create a point with this average at the interval point (function to do this
        // would be good)
        // When finished with all ur
        // Add points at intervals until at current time
        // Add point at current time
        for (UserResponse ur : responses) {
            Response r = ur.response;
            // System.out.println("ur Time: " +ur.timestamp);
            // For each response, carry out aggregate functions
            Object response = r.getResponse();
            if (response instanceof Double) {
                double responseAsDouble = (double) response;
                double rank = (double) response;
                sum += rank;
                count++;
                if (rank < min)
                    min = rank;
                if (rank > max)
                    max = rank;
                // points.add(new Point(ur.timestamp / 1000, responseAsDouble));
                if (ur.timestamp > startTime + i * interval) {
                    pointAverage = this.averageValues(ratings);
                    while (startTime + i * interval < ur.timestamp) {
                        // System.out.println("new Point: " + (startTime + i*interval));
                        points.add(new Point((startTime + i * interval) / 1000, pointAverage));
                        i++;
                    }
                }
                ratings.put(ur.userID, responseAsDouble);
            }
        }
        pointAverage = this.averageValues(ratings);
        while (startTime + i * interval < currentTime) {
            points.add(new Point((startTime + i * interval) / 1000, pointAverage));
            i++;
        }
        // Add point for current time
        points.add(new Point(currentTime / 1000, pointAverage));
        double average = (count == 0) ? 0 : (sum / count); // Prevent divide by 0 error
        Stats stats = new Stats(pointAverage, average, min, max);
        return new NumericQuestion(question.getTitle(), stats, question.getMinValue(), question.getMaxValue(),
                question.getMinTime(), question.getMaxTime(), Calendar.getInstance().getTimeInMillis() / 1000,
                points.toArray(new Point[0]));
    }

    /**
     * Analyses a multiple-choice question, returns the number of times each option
     * was selected
     * 
     * @param question  Question to analyse
     * @param responses List of responses to analyse
     * @return A multiple-choice question object containing this info
     */
    public ChoiceQuestion aggregateChoiceQuestion(ChoiceQuestion question, List<UserResponse> responses) {
        Counter<Integer> optionCounts = new Counter<>();

        HashMap<String, UserResponse> userResponses = new HashMap<>();
        for (UserResponse ur : responses) {
            userResponses.put(ur.userID, ur);
        }

        // Increase counts for each option per response
        for (UserResponse ur : userResponses.values()) {
            Response r = ur.response;
            Object response = r.getResponse();
            if (response instanceof ArrayList) {
                if (!question.getMultiple()) {
                    continue;
                }
                // Raw arraylist as response could be int, double or string
                ArrayList arr = (ArrayList) response;
                for (Object a : arr) {
                    int value = 0;
                    if (a instanceof Double) {
                        value = ((Double) a).intValue();
                    } else if (a instanceof Integer) {
                        value = (Integer) a;
                    }
                    optionCounts.inc(value);
                }
            } else {
                int value = 0;
                if (response instanceof Double) {
                    value = ((Double) response).intValue();
                } else if (response instanceof Integer) {
                    value = (Integer) response;
                }
                optionCounts.inc(value);
            }
        }

        // Copy options across, updating counts as we go
        Option[] origList = question.getOptions();
        Option[] finalList = new Option[origList.length];
        for (int i = 0; i < finalList.length; i++) {
            String name = origList[i].getName();
            finalList[i] = new Option(name, optionCounts.get(i));
        }

        return new ChoiceQuestion(question.getTitle(), finalList, question.getMultiple());
    }
}

/**
 * Temporary class to store userID with a response, just just with the Feedback
 * object
 */
class UserResponse {
    public String userID;
    public Response response;
    public String key;
    public boolean anonymous;
    public long timestamp;

    public UserResponse(String userID, Response response, boolean anonymous, String key, long timestamp) {
        this.userID = userID;
        this.response = response;
        this.anonymous = anonymous;
        this.key = key;
        this.timestamp = timestamp;
    }
}

/**
 * A counter object is used to count the number of occurrences of each option of
 * type T
 * 
 * @param <T> Type of object to count
 */
class Counter<T> {
    private final HashMap<T, Integer> counts = new HashMap<>();
    private int total;

    public int get(T key) {
        return counts.getOrDefault(key, 0);
    }

    public void inc(T key) {
        counts.put(key, get(key) + 1);
        total++;
    }

    public int getTotal() {
        return total;
    }

    public Set<Map.Entry<T, Integer>> getEntries() {
        return counts.entrySet();
    }
}
