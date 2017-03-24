package com.jonat.flutterby.display_stories;

import com.jonat.flutterby.poi.PointOfInterest;
import com.jonat.flutterby.poi.Story;

/**
 * Created by jonat on 24/03/2017.
 */

public class PoiTag {
    private PointOfInterest poi;
    private Story recommendedStory;

    public PoiTag(PointOfInterest poi, Story recommendedStory){
        this.poi = poi;
        this.recommendedStory = recommendedStory;
    }

    public Story getRecommendedStory(){
        return recommendedStory;
    }

    public PointOfInterest getPoi(){
        return poi;
    }

}
