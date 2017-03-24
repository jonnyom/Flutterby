package com.jonat.flutterby.display_stories;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jonat.flutterby.R;

import java.util.ArrayList;

public class SimilarStoriesActivity extends AppCompatActivity {

    private ArrayList<String> stories;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview_layout);

        rv = (RecyclerView) findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        Intent intent = getIntent();

        String title = intent.getStringExtra("title");
        stories = intent.getStringArrayListExtra(title);

        initializeAdapter();
    }

    private void initializeAdapter(){
        RVAdapter adapter = new RVAdapter(stories);
        rv.setAdapter(adapter);
    }

}
