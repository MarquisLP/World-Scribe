package com.averi.worldscribe.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.ResidentsAdapter;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.views.BottomBar;

public class PlaceActivity extends ArticleActivity {

    private RecyclerView residentsList;
    private EditText descriptionField;
    private EditText historyField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        residentsList = (RecyclerView) findViewById(R.id.recyclerResidents);
        descriptionField = (EditText) findViewById(R.id.editDescription);
        historyField = (EditText) findViewById(R.id.editHistory);

        populateResidences();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadTextFields();
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

    /**
     * Load data for the Place's text fields and display them.
     */
    private void loadTextFields() {
        Resources resources = getResources();
        descriptionField.setText(ExternalReader.getArticleTextFieldData(this, getWorldName(),
                getCategory(), getArticleName(), resources.getString(R.string.descriptionHint)));
        historyField.setText(ExternalReader.getArticleTextFieldData(this, getWorldName(),
                getCategory(), getArticleName(), resources.getString(R.string.historyHint)));
    }

}
