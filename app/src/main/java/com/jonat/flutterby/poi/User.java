package com.jonat.flutterby.poi;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class User {

    private String TAG = "******* USER " + getUsername();

    private String username;
    private String uid;
    private FirebaseUser fUser;
    private DatabaseReference mDatabase;
    private Map<String, Float> interestMap;
    private boolean noStoredInterest = false;

    public User(){

    }

    public User(String username){
        this.username = username;
        interestMap = new HashMap<>();
    }

    public String getUsername(){
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fUser!=null){
            username = fUser.getEmail();
        }
        return username;
    }

    public String getUid(){
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fUser!=null){
            uid = fUser.getUid();
        }
        return uid;
    }

    public Map<String, Float> getInterests(){
        return interestMap;
    }

    public String getHighestInterest(){
        if(!interestsNotFilled()) {
            Object highestInterest = interestMap.keySet().toArray()[0];
            for (String interest : interestMap.keySet()) {
                float score = interestMap.get(interest);
                if (score > interestMap.get(highestInterest)) {
                    highestInterest = interest;
                }
            }
            return (String) highestInterest;
        }else{
            return null;
        }
    }

    public boolean noStoredInterests(){
        if(interestsNotFilled()){
            noStoredInterest = true;
        }else if(interestMap.containsKey(null)){
            noStoredInterest = true;
        }
        return noStoredInterest;
    }

    public void setInterests(Map<String, Float> interestMap){
        if(interestsNotFilled()){
            noStoredInterests();
        }
        this.interestMap = interestMap;
        System.out.println(interestMap);
    }

    public void pushData(String storyTitle, float measure) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if(interestMap.containsKey(null)){
            interestMap.remove(null);
        }
        if(interestMap.containsKey(storyTitle)){
            if(measure>interestMap.get(storyTitle)) {
                mDatabase.child("users").child(getUid()).child("interests").child(storyTitle).setValue(measure);
//                Log.d(TAG, "Interest " +storyTitle+ " added with a score of " + measure);
                interestMap.put(storyTitle, measure);
            }
        }else{
            mDatabase.child("users").child(getUid()).child("interests").child(storyTitle).setValue(measure);
//            Log.d(TAG, "Interest " +storyTitle+ " added with a score of " + measure);
            interestMap.put(storyTitle, measure);
        }
    }

    public boolean interestsNotFilled(){
        return interestMap.isEmpty();
    }

}
