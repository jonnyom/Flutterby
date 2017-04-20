package com.jonat.flutterby.poi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jonat on 05/03/2017.
 */

public class FogIndex {

    private String story;
    private int wordCount;
    private int sentenceCount;
    private float averageSentenceLength;
    private float percentHardWords;
    private float fogIndex;

    public int getWordCount(){
        return wordCount;
    }

    public int getSentenceCount(){
        return sentenceCount;
    }

    public void setSentenceCount(){
        sentenceCount = story.split("[!?.:]+").length;
    }

    public float getAverageSentenceLength(){
        return averageSentenceLength;
    }

    public void setAverageSentenceLength() {
        averageSentenceLength = wordCount / sentenceCount;
    }

    public float getPercentHardWords(){
        return percentHardWords;
    }

    // Method to find the number of hard words in the document
    // - Syllables longer than 3 that aren't nouns (uppercase words) or hyphenated
    public void setPercentHardWords(){
        int count = 0;
        for(String word: story.split(" ")){
            if(countSyllables(word) >= 3){
                if(!word.contains("-") && !Character.isUpperCase(word.charAt(0))){
                    count++;
                }
            }
        }
        percentHardWords = count/wordCount;
    }

    // Method to count the number of words in the document
    public void setWordCount(){
        int count = 0;
        String[] storyArray = story.split(" ");
        for(int i = 0; i< storyArray.length; i++){
            count++;
        }
        wordCount = count;
    }

    // Method to count the syllables in a word
    protected int countSyllables(String word)
    {
        int count = 0;
        word = word.toLowerCase();

        try {
            if (word.charAt(word.length() - 1) == 'e') {
                if (silentE(word)) {
                    String newWord = word.substring(0, word.length() - 1);
                    count = count + count(newWord);
                } else {
                    count++;
                }
            } else {
                count = count + count(word);
            }
        }catch (StringIndexOutOfBoundsException e){
            System.err.println("String index out of bounds: " + e);
        }
        return count;
    }

    // Method to count any substring from a syllable
    private int count(String word) {
        int count = 0;
        Pattern splitter = Pattern.compile("[^aeiouy]*[aeiouy]+");
        Matcher matcher = splitter.matcher(word);

        while (matcher.find()) {
            count++;
        }
        return count;
    }

    // Method to calculate if the word contains a slient E
    private boolean silentE(String word) {
        word = word.substring(0, word.length()-1);

        Pattern pattern = Pattern.compile("[aeiouy]");
        Matcher matcher = pattern.matcher(word);

        if (matcher.find()) {
            return true;
        } else
            return false;
    }

    // Method to create the Fog Index
    private void setFogIndex(){
        fogIndex = (float) (0.4*(averageSentenceLength + percentHardWords));
    }

    public float getFogIndex(){
        return fogIndex;
    }

    // Constructor to create Fog Index by initialising each of the methods
    public FogIndex(String story){
        this.story = story;
        setWordCount();
        setSentenceCount();
        setAverageSentenceLength();
        setPercentHardWords();
        setFogIndex();
    }
}
