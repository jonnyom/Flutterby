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

    public void setWordCount(){
        int count = 0;
        String[] storyArray = story.split(" ");
        for(int i = 0; i< storyArray.length; i++){
            count++;
        }
        wordCount = count;
    }

    protected int countSyllables(String word)
    {
        int count = 0;
        word = word.toLowerCase();

        if (word.charAt(word.length()-1) == 'e') {
            if (silentE(word)){
                String newWord = word.substring(0, word.length()-1);
                count = count + count(newWord);
            } else {
                count++;
            }
        } else {
            count = count + count(word);
        }
        return count;
    }

    private int count(String word) {
        int count = 0;
        Pattern splitter = Pattern.compile("[^aeiouy]*[aeiouy]+");
        Matcher matcher = splitter.matcher(word);

        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private boolean silentE(String word) {
        word = word.substring(0, word.length()-1);

        Pattern pattern = Pattern.compile("[aeiouy]");
        Matcher matcher = pattern.matcher(word);

        if (matcher.find()) {
            return true;
        } else
            return false;
    }

    public void setFogIndex(){
        fogIndex = (float) (0.4*(averageSentenceLength + percentHardWords));
    }

    public float getFogIndex(){
        return fogIndex;
    }

    public FogIndex(String story){
        this.story = story;
        setWordCount();
        setSentenceCount();
        setAverageSentenceLength();
        setPercentHardWords();
        setFogIndex();
    }
}
