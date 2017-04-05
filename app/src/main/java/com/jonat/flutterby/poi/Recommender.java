package com.jonat.flutterby.poi;

import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.util.Log;

import com.jonat.flutterby.config.Config;

import java.util.HashMap;
import java.util.Map;

public class Recommender {

    private static String TAG = "**** Recommender";
    private long startTime;
    private long endTime;
    private User user;
    private Config config;

    public Recommender(User user){
        this.user = user;
    }

    public void setStartTime(long startTime){
        this.startTime = startTime;
    }

    public void setEndTime(long endTime){
        this.endTime = endTime;
    }

    private long getStartTime(){
        return startTime;
    }

    private long getEndTime(){
        return endTime;
    }

    // normalise instead of if combine length + fog index
    public boolean shouldRecommendTimer(String story){
        config = new Config();
        FogIndex fogIndex = new FogIndex(story);
        float storyFogIndex = fogIndex.getFogIndex();
        long duration = endTime - startTime;
        long timeout;
        if(storyFogIndex > 10){
            timeout = 1000000;
        }else if(storyFogIndex < 9 && storyFogIndex > 5){
            timeout = 600000;
        }else{
            timeout = 300000;
        }
        if(duration <= timeout && duration > config.TOO_SHORT){
            return true;
        }
        return false;
    }

    // Method used to normalise the score assigned to a particular story for a user
    public float normaliseScore(String story){
        float returnVal;
        FogIndex fogIndex = new FogIndex(story);
        float storyFogIndex = fogIndex.getFogIndex();
        Log.d(TAG, "Story Fog Index: " + String.valueOf(storyFogIndex));
        long length = story.length();
        long endTime = (getEndTime()/1000);
        long startTime = (getStartTime()/1000);
        long duration = endTime-startTime;

        returnVal = (duration/(length*storyFogIndex));

        return returnVal;
    }

    // Method that takes a map and iterates through them, finding the most suitable point of interest to show to the user
    public boolean recommendPoi(PointOfInterest poi){
        boolean shouldRecommend = false;

        if(user.getInterests().containsKey(null) || user.getInterests().isEmpty()){
            Log.d(TAG, "Recommend POI: User has no interests, should recommend");
            return true;
        }
        String highestInterest = user.getHighestInterest();
        HashMap<String, Story> stories = poi.getPOIStories();
        for (String searchStory : stories.keySet()) {
            Log.d(TAG, "Recommend POI: Iterating through point's stories at: " + searchStory);
                if(highestInterest.equals(searchStory)){
                    Log.d(TAG, "Recommend POI: User's highest interest is the same as the current story, recommending point of interest...");
                    shouldRecommend = true;
                    break;
                }else {
                    Log.d(TAG, "Recommend POI: User's highest interest " + highestInterest+ " is different to the current story " + searchStory);
                    HashMap<String, Float> similarities = stories.get(searchStory).getSimilarities();
                    float similarity = similarities.get(highestInterest);
                    Log.d(TAG, "Recommend POI: Checking similarity to " + searchStory + " with similarity: " + similarity);
                    if(similarity > config.SIMILARITY_THRESHOLD){
                        Log.d(TAG, "Recommend POI: Similarity of " + similarity + " is greater than similarity threshold "
                                + config.SIMILARITY_THRESHOLD +" should recommend");
                        shouldRecommend = true;
                    }
                }
            }
        return shouldRecommend;
    }

    public Story recommendStory(HashMap<String, Story> stories){
        Story recommendedStory = null;
        if(stories == null){
            Log.d(TAG, "Recommend Story: Stories map is Null");
        }
        if(stories.size() == 1){
            Log.d(TAG, "Recommend Story: Point of Interest only has one story");
            recommendedStory = stories.get(getRandomStory(stories));
            if(recommendedStory != null){
                Log.d(TAG, "Recommend Story: Story is not null: " + recommendedStory.getStoryTitle());
            }else{
                Log.d(TAG, "Recommend Story: Recommended story is null");
            }
        }else if(stories.size() > 1){
            Log.d(TAG, "Recommend Story: Point of Interest has multiple stories");
            Log.d(TAG, "Recommend Story: Calling Multiple Story Recommender");
            recommendedStory = multipleStoryRecommender(stories);
//            recommendedStory = stories.get(getRandomStory(stories));
            if(recommendedStory != null){
                Log.d(TAG, "Recommend Story: Successfully called multiple story recommender: "
                        + recommendedStory.getStoryTitle());
            }else{
                Log.d(TAG, "Recommend Story: Recommended story is null");
            }
        }else{
            Log.d(TAG, "Recommend Story: Stories map passed is empty");
        }

        return recommendedStory;
    }

    private Story multipleStoryRecommender(HashMap<String, Story> stories){
        if(stories == null || stories.isEmpty()){
            Log.d(TAG, "Multiple Story Recommender: Stories map is null or empty");
        }
        Log.d(TAG, "Multiple Story Recommender: Multiple stories being searched to find most similar");
        Story recommendedStory = null;
        Map<String, Float> userInterests  = user.getInterests();
        if(userInterests.containsKey(null) || userInterests.isEmpty()){
            Log.d(TAG, "Multiple Story Recommender: User Interests is empty. Returning random story");
            Object story = getRandomStory(stories);
            if(story == null){
                Log.d(TAG, "Multiple Story Recommender: Random story key is null");
            }else {
                Log.d(TAG, "Multiple Story Recommender: Key is not null. Story: " + story);
                recommendedStory = stories.get(story);
                if(recommendedStory != null) {
                    Log.d(TAG, "Multiple Story Recommender: Recommended story is: "
                            + recommendedStory.getStoryTitle());
                }else{
                    Log.d(TAG, "Multiple Story Recommender: Recommended Story is null");
                }
            }
        }else {
            Log.d(TAG, "Multiple Story Recommnder: User's interests are filled");
            String highestInterest = user.getHighestInterest();
            if(highestInterest == null){
                Log.d(TAG, "Highest interest is null");
                recommendedStory = stories.get(getRandomStory(stories));
                return recommendedStory;
            }
            Log.d(TAG, "Multiple Story Recommender: User's highest interest is: " + highestInterest + " with a score of: " + userInterests.get(highestInterest));
            Log.d(TAG, "Multiple Story Recommender: Finding most similar story to " + highestInterest);
            float maxSimilarity = 0;
            for(String searchStory: stories.keySet()){
                if(highestInterest.equals(searchStory)){
                    Log.d(TAG, "Multiple Story Recommender: User's highest interest is the same as the current story, skipping story...");
                }else {
                    Log.d(TAG, "Multiple Story Recommender: User's highest interest " + highestInterest+ " is different to the current story " + searchStory);
                    HashMap<String, Float> similarities = stories.get(searchStory).getSimilarities();
                    float similarity = similarities.get(highestInterest);
                    Log.d(TAG, "Multiple Story Recommender: Checking similarity to " + searchStory + " with similarity: " + similarity);
                    if(similarity > maxSimilarity){
                        Log.d(TAG, "Multiple Story Recommender: Similarity of " + similarity + " is greater than previously seen similarity " + maxSimilarity);
                        maxSimilarity = similarity;
                        recommendedStory = stories.get(searchStory);
                    }else{
                        Log.d(TAG, "Multiple Story Recommender: Similarity of " + similarity + " is not greater than previously seen similarity" + maxSimilarity + ", " +
                                "skipping...");
                    }
                }
            }
        }
        if(recommendedStory!= null){
            Log.d(TAG, "Multiple Story Recommender: Recommended story is: " + recommendedStory.getStoryTitle());
        }else{
            Log.d(TAG, "Multiple Story Recommender: Recommended story is null :(");
        }
        return recommendedStory;
    }

    private Object getRandomStory(HashMap<String, Story> stories){
        Log.d(TAG, "Get Random Story: Getting Random story. Converting keyset to array");
        int randomInx = (int) (Math.random() * stories.size());
        Object[] keys = stories.keySet().toArray();
        Object story = keys[randomInx];
        if(story == null){
            Log.d(TAG, "Get Random Story: key is null");
        }else{
            Log.d(TAG, "Get Random Story: Key is not null: " + story);
        }
        return story;
    }
}

