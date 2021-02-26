package com.group29.models;

import com.group29.models.temp.*;

import java.util.*;

// TODO eventually rewrite to update (not entirely recalculate) data
public class FeedbackAggregator {
    // Instance of the feedback aggregator
    private static FeedbackAggregator feedbackAggregator = new FeedbackAggregator();
    /**
     * Gets the singleton instance of the feedback aggregator
     * @return The main instance of the feedback aggregator
     */
    public static FeedbackAggregator getFeedbackAggregator() {
        return feedbackAggregator;
    }

    /**
     * Collects the feedback submitted for a specified event
     * @param eventID The event specified
     * @return A list of question objects summarising the event's feedback
     */
    public Question[] collateFeedback(Event event) {
        // Get Data
        DatabaseManager dbManager = DatabaseManager.getDatabaseManager();
        Template template = dbManager.getTemplate(event.getTemplateID());
        Question[] questions = template.getQuestions();

        //List<Feedback> feedback = dbManager.getFeedback(event.getID()); //Commented out right now as this function just gets the event and returns the list right now
        List<Feedback> feedback = event.getFeedback();
        // Record responses in these lists
        List<List<UserResponse>> questionResponses = new ArrayList<>();
        // Create lists per question
        for (int i = 0; i < questions.length; i++) questionResponses.add(new LinkedList<>());

        // Add responses to questions lists
        int feedbackNumber = 0;
        for (Feedback fb : feedback) {
            int responseNumber = 0;
            for (Response r : fb.getResponses()) {
                String key = "feedback_response_" + Integer.toString(feedbackNumber) + "_"
                            + Integer.toString(responseNumber);
                questionResponses.get(Integer.parseInt(r.getQuestionID())).add(
                        new UserResponse(fb.getUserID(), r, fb.getAnonymous(), key, fb.getTimestamp() ));
                responseNumber++;
            }
            feedbackNumber++;
        }
        // Calculate aggregates
        Question[] results = new Question[questions.length];
        for (int i = 0; i < questionResponses.size(); i++) {
            Question q = questions[i];
            List<UserResponse> responses = questionResponses.get(i);
            if (q instanceof OpenQuestion) results[i] = aggregateOpenQuestion((OpenQuestion) q, responses);
            else if (q instanceof NumericQuestion) results[i] = aggregateNumericQuestion((NumericQuestion) q, responses);
            else if (q instanceof ChoiceQuestion) results[i] = aggregateChoiceQuestion((ChoiceQuestion) q, responses);
        }
        return results;
    }

    // From https://xpo6.com/list-of-english-stop-words/
    public static final HashSet<String> stopWords = new HashSet<>(Arrays.asList("a", "about", "above", "above", "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also","although","always","am","among", "amongst", "amoungst", "amount",  "an", "and", "another", "any","anyhow","anyone","anything","anyway", "anywhere", "are", "around", "as",  "at", "back","be","became", "because","become","becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill", "both", "bottom","but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry", "de", "describe", "detail", "do", "done", "down", "due", "during", "each", "eg", "eight", "either", "eleven","else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", "fify", "fill", "find", "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has", "hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "how", "however", "hundred", "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itself", "keep", "last", "latter", "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most", "mostly", "move", "much", "must", "my", "myself", "name", "namely", "neither", "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own","part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", "seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thickv", "thin", "third", "this", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward", "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", "yourselves", "the"));

    /**
     * Analyses an open question
     * @param question Question to analyse
     * @param responses List of responses to analyse
     * @return An open question object containing the question info, common words, and mood of the feedback
     */
    public OpenQuestion aggregateOpenQuestion(OpenQuestion question, List<UserResponse> responses) {
        Counter<String> wordCounts = new Counter<>();


        for (UserResponse ur : responses) {
            Response r = ur.response;
            Object response = r.getResponse();
            // Remove case and punctuation
            if (response instanceof String) {
                String sanitised = ((String) response).toLowerCase().replaceAll("\\p{P}", "");
                for (String word : sanitised.split("\\s+")) {
                    if (!stopWords.contains(word)) {    // If not common word, increment counter
                        wordCounts.inc(word);
                    }
                }   
            }
        }
        // Sort words (could be done in Counter, using TreeMap or similar, but bad practice as TreeMap should sort as a total order between keys only)
        List<Map.Entry<String, Integer>> counts = new ArrayList<>(wordCounts.getEntries());
        counts.sort(Map.Entry.comparingByValue());
        System.out.println(counts);

        // Calculate common words, and copy into array
        int trend_length = 3;
        int loop = (counts.size() > trend_length) ? trend_length : counts.size();
        Trend[] trends = new Trend[loop];
        for (int i = 0; i < loop; i++) {
            Map.Entry<String, Integer> entry = counts.get(i);
            trends[i] = new Trend(entry.getKey(), entry.getValue());
        }

        // Copy responses into QR array
        QuestionResponse[] qrs = new QuestionResponse[responses.size()];
        int j = 0;
        for (int i = responses.size()-1; i >= 0; i--) {
            UserResponse r = responses.get(i);
            Object response = r.response.getResponse();
            if (response instanceof String) {
                qrs[j] = new QuestionResponse((String) response, ((r.anonymous) ? null : r.userID), r.username);
                j++;
            }
        }

        return new OpenQuestion(question.getTitle(), qrs, trends);
    }

    /**
     * Analyses a numeric question, returns the min max and average ratings
     * @param question Question to analyse
     * @param responses List of responses to analyse
     * @return A numeric question object containing this info
     */
    public NumericQuestion aggregateNumericQuestion(NumericQuestion question, List<UserResponse> responses) {
        double sum = 0;
        double count = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        ArrayList<Point> points = new ArrayList<Point>();

        for (UserResponse ur : responses) {
            Response r = ur.response;
            // For each response, carry out aggregate functions
            Object response = r.getResponse();
            if (response instanceof Double){
                double responseAsFloat = (double) response;
                double rank = (double) response;
                sum += rank;
                count++;
                if (rank < min) min = rank;
                if (rank > max) max = rank;
                points.add(new Point(ur.timestamp / 1000, responseAsFloat));
                
            }
            //points.add(new Point(f.getTimestamp() / 1000, responseAsFloat));
        }
        double current_value = (question.getMinValue() + question.getMaxValue()) / 2;
        if (count > 0) {
            current_value = sum / count;
        }
        Stats stats = new Stats(current_value, min, max);

        return new NumericQuestion(question.getTitle(), stats,
                question.getMinValue(), question.getMaxValue(), question.getMinTime(), question.getMaxTime(),
                Calendar.getInstance().getTimeInMillis()/1000, points.toArray(new Point[0]));
    }

    /**
     * Analyses a multiple-choice question, returns the number of times each option was selected
     * @param question Question to analyse
     * @param responses List of responses to analyse
     * @return A multiple-choice question object containing this info
     */
    public ChoiceQuestion aggregateChoiceQuestion(ChoiceQuestion question, List<UserResponse> responses) {
        Counter<String> optionCounts = new Counter<>();

        // Increase counts for each option per response
        for (UserResponse ur : responses) {
            Response r = ur.response;
            Object response = r.getResponse();
            if (response instanceof ArrayList) {
                ArrayList arr = (ArrayList) response;
                for (Object a : arr) {
                    if (a instanceof String) {
                        optionCounts.inc((String) a);
                    }
                }
            }
        }

        // Copy options across, updating counts as we go
        Option[] origList = question.getOptions();
        Option[] finalList = new Option[origList.length];
        for (int i = 0; i < finalList.length; i++) {
            String name = origList[i].getName();
            finalList[i] = new Option(name, optionCounts.get(name));
        }

        return new ChoiceQuestion(question.getTitle(), finalList,false);
    }
}

/**
 * Temporary class to store userID with a response, just just with the Feedback object
 */
class UserResponse {
    public String userID;
    public Response response;
    public String username;
    public boolean anonymous;
    public long timestamp;

    public UserResponse(String userID, Response response, boolean anonymous, String username, long timestamp  ) {
        this.userID = userID;
        this.response = response;
        this.anonymous = anonymous;
        this.username = username;
        this.timestamp = timestamp;
    }
}

/**
 * A counter object is used to count the number of occurrences of each option of type T
 * @param <T> Type of object to count
 */
class Counter<T> {
    private HashMap<T, Integer> counts = new HashMap<>();
    private int total;


    public int get(T key) {
        return counts.getOrDefault(key, 0);
    }

    public void inc(T key) {
        counts.put(key, get(key)+1);
        total++;
    }

    public int getTotal() {
        return total;
    }

    public Set<Map.Entry<T,Integer>> getEntries() {
        return counts.entrySet();
    }
}
