package com.jonat.flutterby.poi;

/**
 * Created by jonat on 04/01/2017.
 */

public class POIGenre {
    private String genre;
    private long genreID;


    public POIGenre(){

    }

    public POIGenre(long genreID, String genre){
        this.genre = genre;
        this.genreID = genreID;
    }


    public String getGenre(){
        return genre;
    }

    public long getGenreID(){
        return genreID;
    }
}
