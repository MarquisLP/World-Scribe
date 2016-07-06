package com.averi.worldscribe.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageView;

import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.views.BottomBar;
import com.averi.worldscribe.R;

public class PersonActivity extends ArticleActivity {

    private EditText aliasesField;
    private EditText ageField;
    private EditText biographyField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        aliasesField = (EditText) findViewById(R.id.editAliases);
        ageField = (EditText) findViewById(R.id.editAge);
        biographyField = (EditText) findViewById(R.id.editBio);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTextFieldsData();
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_person;
    }

    @Override
    protected ImageView getImageView() { return (ImageView) findViewById(R.id.imagePerson); }

    @Override
    protected BottomBar getBottomBar() {
        return (BottomBar) findViewById(R.id.bottomBar);
    }

    @Override
    protected RecyclerView getConnectionsRecycler() {
        return (RecyclerView) findViewById(R.id.recyclerConnections);
    }

    /**
     * Retrieve this Person's aliases, age, biography data and display them in the corresponding
     * text fields.
     */
    private void loadTextFieldsData() {
        Resources resources = getResources();

        aliasesField.setText(ExternalReader.getArticleTextFieldData(this, super.getWorldName(),
                super.getCategory(), super.getArticleName(),
                resources.getString(R.string.aliasesField)));
        ageField.setText(ExternalReader.getArticleTextFieldData(this, super.getWorldName(),
                super.getCategory(), super.getArticleName(),
                resources.getString(R.string.ageField)));
        biographyField.setText(ExternalReader.getArticleTextFieldData(this, super.getWorldName(),
                super.getCategory(), super.getArticleName(),
                resources.getString(R.string.biographyField)));
    }

}
