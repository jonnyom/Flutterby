package com.jonat.flutterby.poi;

import java.util.HashMap;

/**
 * Created by jonat on 09/03/2017.
 */

public class Story {
    private String storyTitle;
    private String storyText;
    private HashMap<String,Float> similarities;

    public Story(String storyTitle, String storyText, HashMap<String,Float> similarities){
        this.storyTitle = storyTitle;
        this.storyText = storyText;
        this.similarities = similarities;
    }

    public String getStory(){
        return storyText;
    }

    public HashMap<String,Float> getSimilarities(){
        return similarities;
    }

    public String getStoryTitle(){return storyTitle;}
}
