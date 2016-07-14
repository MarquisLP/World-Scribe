package com.averi.worldscribe.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.ResidentsAdapter;
import com.averi.worldscribe.views.BottomBar;

public class PlaceActivity extends ArticleActivity {

    private RecyclerView residentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        residentsList = (RecyclerView) findViewById(R.id.recyclerResidents);

        populateResidences();
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_place;
    }

    @Override
    protected ImageView getImageView() { return (ImageView) findViewById(R.id.imagePlace); }

    @Override
    protected BottomBar getBottomBar() {
        return (BottomBar) findViewById(R.id.bottomBar);
    }

    @Override
    protected RecyclerView getConnectionsRecycler() {
        return (RecyclerView) findViewById(R.id.recyclerConnections);
    }

    @Override
    protected RecyclerView getSnippetsRecycler() {
        return (RecyclerView) findViewById(R.id.recyclerSnippets);
    }

    /**
     * Populate the Residents RecyclerView with cards for this Place's Residents.
     */
    private void populateResidences() {
        residentsList.setLayoutManager(new LinearLayoutManager(this));
        residentsList.setAdapter(new ResidentsAdapter(this, getWorldName(), getArticleName()));
    }

}
