package com.averi.worldscribe.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.averi.worldscribe.ArticleTextField;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.adapters.MembershipsAdapter;
import com.averi.worldscribe.adapters.ResidencesAdapter;
import com.averi.worldscribe.adapters.SnippetsAdapter;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.views.BottomBar;
import com.averi.worldscribe.R;

import java.util.ArrayList;

public class PersonActivity extends ArticleActivity {

    private EditText aliasesField;
    private EditText ageField;
    private EditText biographyField;
    private RadioGroup genderGroup;
    private RecyclerView membershipsList;
    private RecyclerView residencesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        aliasesField = (EditText) findViewById(R.id.editAliases);
        ageField = (EditText) findViewById(R.id.editAge);
        biographyField = (EditText) findViewById(R.id.editBio);
        genderGroup = (RadioGroup) findViewById(R.id.radioGroupGender);
        membershipsList = (RecyclerView) findViewById(R.id.recyclerMemberships);
        residencesList = (RecyclerView) findViewById(R.id.recyclerResidences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Resources resources = getResources();

        loadTextFieldsData(resources);
        loadGender(resources);
        populateMemberships();
        populateResidences();
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

    @Override
    protected RecyclerView getSnippetsRecycler() {
        return (RecyclerView) findViewById(R.id.recyclerSnippets);
    }

    @Override
    protected ArrayList<ArticleTextField> getTextFields() {
        Resources resources = getResources();
        ArrayList<ArticleTextField> textFields = new ArrayList<>();

        textFields.add(new ArticleTextField(resources.getString(R.string.aliasesField),
                (EditText) findViewById(R.id.editAliases),
                this, getWorldName(), Category.Person, getArticleName()));
        textFields.add(new ArticleTextField(resources.getString(R.string.ageField),
                (EditText) findViewById(R.id.editAge),
                this, getWorldName(), Category.Person, getArticleName()));
        textFields.add(new ArticleTextField(resources.getString(R.string.biographyField),
                (EditText) findViewById(R.id.editBio),
                this, getWorldName(), Category.Person, getArticleName()));

        return textFields;
    }

    /**
     * Retrieve this Person's aliases, age, biography data and display them in the corresponding
     * text fields.
     * @param resources A Resources instance containing this app's resource files and values.
     */
    private void loadTextFieldsData(Resources resources) {
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

    /**
     * Load this Person's gender and display it.
     * @param resources A Resources instance containing this app's resource files and values.
     */
    private void loadGender(Resources resources) {
        String genderString = ExternalReader.getArticleTextFieldData(this, super.getWorldName(),
                super.getCategory(), super.getArticleName(),
                resources.getString(R.string.genderText));

        if (genderString.equals(resources.getString(R.string.maleText))) {
            genderGroup.check(R.id.radioButtonMale);
        } else if (genderString.equals(resources.getString(R.string.femaleText))) {
            genderGroup.check(R.id.radioButtonFemale);
        } else {
            genderGroup.check(R.id.radioButtonOtherGender);
        }
    }

    /**
     * Populate the Memberships RecyclerView with cards for this Person's Memberships.
     */
    private void populateMemberships() {
        membershipsList.setLayoutManager(new LinearLayoutManager(this));
        membershipsList.setAdapter(new MembershipsAdapter(this, getWorldName(), getArticleName()));
    }

    /**
     * Populate the Residences RecyclerView with cards for this Person's Residences.
     */
    private void populateResidences() {
        residencesList.setLayoutManager(new LinearLayoutManager(this));
        residencesList.setAdapter(new ResidencesAdapter(this, getWorldName(), getArticleName()));
    }

}
