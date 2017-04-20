package com.jonat.flutterby.poi;

import android.location.Location;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by jonat on 09/11/2016.
 */

public class PointOfInterest {
    private String title;
    private HashMap<String, Story> stories;
    private Vector<POIGenre> genres;
    private Location location;

    public PointOfInterest(Location location, String title, HashMap<String, Story> stories, Vector<POIGenre> genres){
        this.location = location;
        this.title = title;
        this.stories = stories;
        this.genres = genres;
    }

    public void setLocation(Location location){
        this.location = location;
    }

    /**
     * Method to retrieve title for POI object
     * @return String
     */
    public String getPOITitle(){
        return title;
    }

    /**
     * Method to retrieve story for POI
     * @return String
     */
    public HashMap<String, Story> getPOIStories(){
        return stories;
    }

    public Vector<POIGenre> getGenres(){
        return genres;
    }

    public Location getLocation(){return location; }

}
