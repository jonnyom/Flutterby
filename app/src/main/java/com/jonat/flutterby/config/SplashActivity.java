package com.jonat.flutterby.config;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jonat.flutterby.MapsActivity;
import com.jonat.flutterby.auth.LoginActivity;
import com.jonat.flutterby.auth.ProfileActivity;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // user auth state is changed - user is null
            // launch login activity
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }else{
            Intent intent = new Intent(SplashActivity.this, MapsActivity.class);
            startActivity(intent);
            finish();
        }


    }
}
