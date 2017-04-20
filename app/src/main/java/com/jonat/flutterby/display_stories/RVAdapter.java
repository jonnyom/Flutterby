package com.jonat.flutterby.display_stories;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jonat.flutterby.R;

import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PoiViewHolder>{

    private ArrayList<String> storyList;

    public static class PoiViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView storyTitle;
        TextView storyStory;

        PoiViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            storyTitle = (TextView)itemView.findViewById(R.id.storyTitleView);
            storyStory = (TextView)itemView.findViewById(R.id.storyStoryView);
        }
    }

    public RVAdapter(ArrayList<String> storyList){
        this.storyList = storyList;
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    @Override
    public PoiViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_similar_stories, viewGroup, false);
        PoiViewHolder pvh = new PoiViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PoiViewHolder poiViewHolder, int i) {
        int count = i+1;
        poiViewHolder.storyTitle.setText("Story " + count);
        poiViewHolder.storyStory.setText(storyList.get(i));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}
