package com.jonat.flutterby.poi;

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

    public PointOfInterest findClosest(HashMap<PointOfInterest, Float> poiDistances){
        PointOfInterest poi = null;
        float shortestDistance = Float.MAX_VALUE;
        Log.d(TAG, "Looking for closest Point Of Interest");
        if(poiDistances == null || poiDistances.isEmpty()){
            Log.d(TAG, "Find Closest: poiDistances is NULL");
        }
        for(PointOfInterest pointOfInterest: poiDistances.keySet()){
            float distance = poiDistances.get(pointOfInterest);
            if(distance < shortestDistance){
                shortestDistance = distance;
                poi = pointOfInterest;
            }
        }

        if(poi == null){
            Log.d(TAG, "Find Closest: POI is null");
        }

        return poi;
    }

    // Method that takes a map and iterates through them, finding the most suitable point of interest to show to the user
    public PointOfInterest recommendPoi(HashMap<PointOfInterest, Float> poiDistances){
        PointOfInterest poi = null;
        float scorer = 0;
        if(poiDistances == null || poiDistances.size() == 0){
            Log.d(TAG, "Recommend POI: poiDistances is NULL");
        }else{
            Log.d(TAG, "Recommend POI: poiDistance not null");
        }
        Map<String, Float> userInterests = user.getInterests();
        System.out.println(TAG + " Recommend POI: " + userInterests);
        if(userInterests.containsKey(null) || userInterests.isEmpty()) {
            Log.d(TAG, "Recommend POI: User interest contains a null or is empty");
            Log.d(TAG, "Recommend POI: Calling findClosest");
            poi = findClosest(poiDistances);
            if(poi!= null){
                Log.d(TAG, "Successfully called findClosest");
            }else{
                Log.d(TAG, "poi still null");
            }
        }else {
            for (PointOfInterest pointOfInterest : poiDistances.keySet()) {
                Log.d(TAG, "Recommend POI: Finding Most relevant Point Of Interest: " + pointOfInterest.getPOITitle());
                float distance = poiDistances.get(pointOfInterest);
                if(Float.isNaN(distance)) {
                    Log.d(TAG, "Recommend POI: distance found isn't a number");
                }else{
                    Log.d(TAG, "Recommend POI: Distance found is a number: " + distance);
                }
                for (String searchStory : userInterests.keySet()) {
                    Log.d(TAG, "Recommend POI: Iterating through user's interest at: " + searchStory);
                    float score = userInterests.get(searchStory);
                    if ((score / distance) >= scorer) {
                        Log.d(TAG, "Recommend POI: Score: " + distance/score);
                        scorer = score;
                        poi = pointOfInterest;
                    }
                }
            }
        }

        if(poi == null){
            Log.d(TAG, "Recommend POI: Recommended POI is null, finding closest Point Of Interest");
            poi = findClosest(poiDistances);
        }else{
            Log.d(TAG, "Recommend POI: poi is Not Null. Title: " + poi.getPOITitle());
        }

        return poi;
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
        Object[] poiStories = stories.keySet().toArray();
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
            Log.d(TAG, "Multiple Story Recommender: User interests are not empty");
            HashMap<String, Float> similarities;
            String mostSimTitle;
            float maxScore = 0;
            for (String searchStory : stories.keySet()) {
                Log.d(TAG, "Multiple Story Recommender: Searching Story: " + searchStory);
                Story story = stories.get(searchStory);
                similarities = story.getSimilarities();
                if(similarities == null || similarities.isEmpty()){
                    Log.d(TAG, "Multiple Story Recommender: Similarities map is empty");
                }else{
                    Log.d(TAG, "Multiple Story Recommender: Similarities map is not empty" + similarities);
                }
                if (userInterests.containsKey(searchStory)
                        && userInterests.get(searchStory) > maxScore) {
                    maxScore = userInterests.get(searchStory);

                    Log.d(TAG, "Multiple Story Recommender: User has previously viewed this story: "
                            + searchStory + " With a score of: " + maxScore);
                    Log.d(TAG, "Multiple Story Recommender: Finding most similar story");
                    mostSimTitle = getMostSimilar(searchStory, poiStories, similarities);
                    if(mostSimTitle != null){
                        Log.d(TAG, "Multiple Story Recommender: Most Similar Title is not null: " + mostSimTitle);
                    }else{
                        Log.d(TAG, "Multiple Story Recommender: Most similar title is null");
                    }
                }else{
                    Log.d(TAG, "Multiple Story Recommender: User has never viewed this story " + searchStory);
                    String highestInterest = user.getHighestInterest();
                    mostSimTitle = getMostSimToInterest(highestInterest, poiStories, similarities);
                }
                if(mostSimTitle != null){
                    Log.d(TAG, "Multiple Story Recommender: Most Similar Title is not null: " + mostSimTitle);
                }else{
                    Log.d(TAG, "Multiple Story Recommender: Most similar title is null");
                }
                Log.d(TAG, "Multiple Story Recommender: Returning the recommended story");
                recommendedStory = stories.get(mostSimTitle);
                if(recommendedStory != null){
                    Log.d(TAG, "Multiple Story Recommender: Recommended Story is not null: " + recommendedStory.getStoryTitle());
                }else{
                    Log.d(TAG, "Multiple Story Recommender: Recommended Story is null");
                }
            }
        }
        return recommendedStory;
    }

    @NonNull
    private String getMostSimilar(String comparison, Object[] titles, HashMap<String, Float> similarities){
        if(titles.length==0 || titles == null){
            Log.d(TAG, "Get Most Similar: Titles array is empty");
        }else{
            Log.d(TAG, "Get Most Similar: Finding Most Similar story to titles: " + titles);
        }
        if(similarities.isEmpty() || similarities == null){
            Log.d(TAG, "Get Most Similar: Similarities is empty or null");
        }else{
            Log.d(TAG, "Get Most Similar: Similarities isn't null, continuing");
        }
        String returnTitle = null;
        float max = 0;
        for(Object simTitle: titles){
            Log.d(TAG, "Get Most Similar: Finding Most Similar story to title: " + simTitle);
            float similarity = similarities.get(simTitle);
            if(!comparison.equals(simTitle) && similarity > max){
                Log.d(TAG, "Get Most Similar: Titles doesn't contain " + simTitle
                        + " Similarity is bigger than max: " + similarity);
                max = similarity;
                returnTitle = (String) simTitle;
            }
        }
        if(returnTitle == null){
            Log.d(TAG, "Get Most Similar: Return title is null");
        }else{
            Log.d(TAG, "Get Most Similar: Return title is not null: " + returnTitle);
        }
        return returnTitle;
    }

    @NonNull
    private String getMostSimToInterest(String highestInterest, Object[] titles, HashMap<String, Float> similarities){
        if(titles.length==0 || titles == null){
            Log.d(TAG, "Get Most Similar: Titles array is empty");
        }else{
            Log.d(TAG, "Get Most Similar: Finding Most Similar story to titles: " + titles);
        }
        if(similarities.isEmpty() || similarities == null){
            Log.d(TAG, "Get Most Similar: Similarities is empty or null");
        }else{
            Log.d(TAG, "Get Most Similar: Similarities isn't null, continuing");
        }
        String returnTitle = null;
        float max = 0;
        for(Object simTitle: titles){
            Log.d(TAG, "Get Most Similar: Finding Most Similar story to title: " + highestInterest);
            float similarity = similarities.get(highestInterest);
            if(!highestInterest.equals(simTitle) && similarity > max){
                Log.d(TAG, "Get Most Similar: Titles doesn't contain " + simTitle
                        + " Similarity is bigger than max: " + similarity);
                max = similarity;
                returnTitle = (String) simTitle;
            }
        }
        if(returnTitle == null){
            Log.d(TAG, "Get Most Similar: Return title is null");
        }else{
            Log.d(TAG, "Get Most Similar: Return title is not null: " + returnTitle);
        }
        return returnTitle;
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

