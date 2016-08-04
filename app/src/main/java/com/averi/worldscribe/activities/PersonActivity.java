package com.averi.worldscribe.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.averi.worldscribe.ArticleTextField;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.Membership;
import com.averi.worldscribe.adapters.MembershipsAdapter;
import com.averi.worldscribe.adapters.ResidencesAdapter;
import com.averi.worldscribe.adapters.SnippetsAdapter;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.views.BottomBar;
import com.averi.worldscribe.R;

import java.util.ArrayList;

public class PersonActivity extends ArticleActivity {

    /**
     * The request code for creating a new Membership for this Person.
     */
    public static final int RESULT_NEW_MEMBERSHIP = 300;

    private RadioGroup genderGroup;
    private RecyclerView membershipsList;
    private RecyclerView residencesList;
    private Button addMembershipButton;
    private Boolean genderWasEditedSinceLastSave = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        genderGroup = (RadioGroup) findViewById(R.id.radioGroupGender);
        membershipsList = (RecyclerView) findViewById(R.id.recyclerMemberships);
        residencesList = (RecyclerView) findViewById(R.id.recyclerResidences);
        addMembershipButton = (Button) findViewById(R.id.buttonAddMembership);

        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                genderWasEditedSinceLastSave = true;
            }
        });

        addMembershipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMembership();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Resources resources = getResources();

        loadGender(resources);
        populateMemberships();
        populateResidences();
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveGenderIfEdited();
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
    protected Button getAddConnectionButton() {
        return (Button) findViewById(R.id.buttonAddConnection);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_NEW_MEMBERSHIP:
                if (resultCode == RESULT_OK) {
                    Membership newMembership = new Membership();
                    newMembership.worldName = getWorldName();
                    newMembership.groupName = data.getStringExtra(IntentFields.ARTICLE_NAME);
                    newMembership.memberName = getArticleName();

                    Intent editMembershipIntent = new Intent(this, EditMembershipActivity.class);
                    editMembershipIntent.putExtra(IntentFields.MEMBERSHIP, newMembership);
                    startActivity(editMembershipIntent);
                }
        }
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

    /**
     * Saves the Person's Gender if it was changed since the last save.
     */
    private void saveGenderIfEdited() {
        if (genderWasEditedSinceLastSave) {
            Resources resources = getResources();
            String genderFilename = resources.getString(R.string.genderText);

            if (genderGroup.getCheckedRadioButtonId() == R.id.radioButtonMale) {
                ExternalWriter.writeStringToArticleFile(this, getWorldName(), Category.Person,
                        getArticleName(), genderFilename, resources.getString(R.string.maleText));
            } else if (genderGroup.getCheckedRadioButtonId() == R.id.radioButtonFemale) {
                ExternalWriter.writeStringToArticleFile(this, getWorldName(), Category.Person,
                        getArticleName(), genderFilename, resources.getString(R.string.femaleText));
            } else {
                ExternalWriter.writeStringToArticleFile(this, getWorldName(), Category.Person,
                        getArticleName(), genderFilename,
                        resources.getString(R.string.otherGenderText));
            }

            genderWasEditedSinceLastSave = false;
        }
    }

    /**
     * Opens SelectArticleActivity so the user can select the Group to be part of in a new
     * {@link com.averi.worldscribe.Membership Membership} for this Person.
     */
    private void createMembership() {
        Intent selectGroupIntent = new Intent(this, SelectArticleActivity.class);
        MembershipsAdapter membershipsAdapter = (MembershipsAdapter) membershipsList.getAdapter();

        selectGroupIntent.putExtra(IntentFields.WORLD_NAME, getWorldName());
        selectGroupIntent.putExtra(IntentFields.CATEGORY, Category.Group);
        selectGroupIntent.putExtra(IntentFields.MAIN_ARTICLE_CATEGORY, Category.Person);
        selectGroupIntent.putExtra(IntentFields.MAIN_ARTICLE_NAME, getArticleName());
        selectGroupIntent.putExtra(IntentFields.EXISTING_LINKS,
                membershipsAdapter.getLinkedArticleList());
        startActivityForResult(selectGroupIntent, RESULT_NEW_MEMBERSHIP);
    }

}
