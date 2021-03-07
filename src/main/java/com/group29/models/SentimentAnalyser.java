package com.group29.models;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.ejml.simple.SimpleMatrix;

import java.util.Properties;

public class SentimentAnalyser {
    private static final SentimentAnalyser sentimentAnalyser = new SentimentAnalyser();
    private final StanfordCoreNLP nlpPipeline;

    /**
     * Constructs the SentimentAnalyser object
     */
    public SentimentAnalyser() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        nlpPipeline = new StanfordCoreNLP(props);
    }

    /**
     * Analyses a message for the sentiment
     * 
     * @param message The message being analysed
     * @return The sentiment as a float 
     */
    public float analyse(String message) {
        Annotation annotation = nlpPipeline.process(message);
        float[] results = new float[5];
        int length = 0;

        // Need to analyse each sentence separately. Not sure what the best way to combine these are, possibly straight average or select longest or most polarised sentence, currently going for average based on length however
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);   // Generate sentence tree with sentiments
            SimpleMatrix mood_proportions = RNNCoreAnnotations.getPredictions(tree);        // Extract total ratings
            String partText = sentence.toString();

            // Add to totals weighted by sentence length
            length += partText.length();
            for (int i = 0; i < 5; i++)
                results[i] += mood_proportions.get(i, 0) * partText.length();
        }

        if (length == 0) return 0f;

        // Total up resulting value
        float total = 0f;
        for (int i = 0; i < 5; i++) {
            results[i] /= length;
            total += results[i] * i;
        }

        // Remap to 0 to 4 to -1 to 1
        return (total / 2)-1;
    }

    /**
     * Gets the SentimentAnalyser
     */
    public static SentimentAnalyser getSentimentAnalyser() {
        return sentimentAnalyser;
    }
}
