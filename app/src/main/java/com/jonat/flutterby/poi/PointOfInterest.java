package com.jonat.flutterby.poi;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by jonat on 09/11/2016.
 */

public class PointOfInterest {
    private String title;
    private HashMap<String, Story> stories;
    private Vector<POIGenre> genres;
    private LatLng latLng;

    public PointOfInterest(LatLng latLng, String title, HashMap<String, Story> stories, Vector<POIGenre> genres){
        this.latLng = latLng;
        this.title = title;
        this.stories = stories;
        this.genres = genres;
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

    public LatLng getLatLng(){return latLng; }

}
