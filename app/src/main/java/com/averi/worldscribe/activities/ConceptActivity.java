package com.averi.worldscribe.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.views.BottomBar;

public class ConceptActivity extends ArticleActivity {

    private EditText descriptionField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        descriptionField = (EditText) findViewById(R.id.editDescription);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadTextFields();
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_concept;
    }

    @Override
    protected ImageView getImageView() { return (ImageView) findViewById(R.id.imageConcept); }

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
     * Load data for the Concept's text fields and display them.
     */
    private void loadTextFields() {
        Resources resources = getResources();
        descriptionField.setText(ExternalReader.getArticleTextFieldData(this, getWorldName(),
                getCategory(), getArticleName(), resources.getString(R.string.descriptionHint)));
    }

}
