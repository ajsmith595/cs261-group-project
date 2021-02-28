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

    public SentimentAnalyser() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        nlpPipeline = new StanfordCoreNLP(props);
    }

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
        // Total up resulting value
        float total = 0;
        for (int i = 0; i < 5; i++) {
            results[i] /= length;
            total += results[i] * i;
        }
        return total;
    }

    public static SentimentAnalyser getSentimentAnalyser() {
        return sentimentAnalyser;
    }
}