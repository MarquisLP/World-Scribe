package com.averi.worldscribe.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.averi.worldscribe.ArticleTextField;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.Membership;
import com.averi.worldscribe.R;
import com.averi.worldscribe.Residence;
import com.averi.worldscribe.adapters.MembershipsAdapter;
import com.averi.worldscribe.adapters.ResidencesAdapter;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.ExternalDeleter;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.views.ArticleSectionCollapser;
import com.averi.worldscribe.views.BottomBar;

import java.util.ArrayList;

public class PersonActivity extends ArticleActivity {

    /**
     * The request code for creating a new Membership for this Person.
     */
    public static final int RESULT_NEW_MEMBERSHIP = 300;
    /**
     * The request code for creating a new Residence for this Person.
     */
    public static final int RESULT_NEW_RESIDENCE = 400;

    private RadioGroup genderGroup;
    private RecyclerView membershipsList;
    private RecyclerView residencesList;
    private Button addMembershipButton;
    private Button addResidenceButton;
    private Boolean genderWasEditedSinceLastSave = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        genderGroup = (RadioGroup) findViewById(R.id.radioGroupGender);
        membershipsList = (RecyclerView) findViewById(R.id.recyclerMemberships);
        residencesList = (RecyclerView) findViewById(R.id.recyclerResidences);
        addMembershipButton = (Button) findViewById(R.id.buttonAddMembership);
        addResidenceButton = (Button) findViewById(R.id.buttonAddResidence);

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
        addResidenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createResidence();
            }
        });

        ActivityUtilities.enableWordWrapOnSingleLineEditText(
                (EditText) findViewById(R.id.editAliases));
        ActivityUtilities.enableWordWrapOnSingleLineEditText(
                (EditText) findViewById(R.id.editAge));
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
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.coordinatorLayout);
    }

    @Override
    protected NestedScrollView getNestedScrollView() {
        return (NestedScrollView) findViewById(R.id.scrollView);
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
    protected Button getAddSnippetButton() {
        return (Button) findViewById(R.id.buttonAddSnippet);
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
    protected TextView getGeneralInfoHeader() {
        return (TextView) findViewById(R.id.textGeneralInfo);
    }

    @Override
    protected ViewGroup getGeneralInfoLayout() {
        return (LinearLayout) findViewById(R.id.linearGeneralInfo);
    }

    @Override
    protected TextView getConnectionsHeader() {
        return (TextView) findViewById(R.id.textConnections);
    }

    @Override
    protected ViewGroup getConnectionsLayout() {
        return (LinearLayout) findViewById(R.id.linearConnections);
    }

    @Override
    protected TextView getSnippetsHeader() {
        return (TextView) findViewById(R.id.textSnippets);
    }

    @Override
    protected ViewGroup getSnippetsLayout() {
        return (LinearLayout) findViewById(R.id.linearSnippets);
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
                break;
            case RESULT_NEW_RESIDENCE:
                if (resultCode == RESULT_OK) {
                    Residence newResidence = new Residence();
                    newResidence.worldName = getWorldName();
                    newResidence.residentName = getArticleName();
                    newResidence.placeName = data.getStringExtra(IntentFields.ARTICLE_NAME);
                    ExternalWriter.saveResidence(this, newResidence);
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

    @Override
    protected void addSectionCollapsers() {
        TextView membershipsHeader = (TextView) findViewById(R.id.textMemberships);
        TextView residencesHeader = (TextView) findViewById(R.id.textResidences);

        membershipsHeader.setOnClickListener(new ArticleSectionCollapser(this, membershipsHeader,
                (LinearLayout) findViewById(R.id.linearMemberships)));
        residencesHeader.setOnClickListener(new ArticleSectionCollapser(this, residencesHeader,
                (LinearLayout) findViewById(R.id.linearResidences)));

        super.addSectionCollapsers();
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

    /**
     * Opens SelectArticleActivity so the user can select the Place for a new
     * {@link com.averi.worldscribe.Residence Residence} for this Person.
     */
    private void createResidence() {
        Intent selectGroupIntent = new Intent(this, SelectArticleActivity.class);
        ResidencesAdapter residencesAdapter = (ResidencesAdapter) residencesList.getAdapter();

        selectGroupIntent.putExtra(IntentFields.WORLD_NAME, getWorldName());
        selectGroupIntent.putExtra(IntentFields.CATEGORY, Category.Place);
        selectGroupIntent.putExtra(IntentFields.MAIN_ARTICLE_CATEGORY, Category.Person);
        selectGroupIntent.putExtra(IntentFields.MAIN_ARTICLE_NAME, getArticleName());
        selectGroupIntent.putExtra(IntentFields.EXISTING_LINKS,
                residencesAdapter.getLinkedArticleList());
        startActivityForResult(selectGroupIntent, RESULT_NEW_RESIDENCE);
    }

    @Override
    protected void deleteArticle() {
        if ((deleteAllMemberships()) && (deleteAllResidences())) {
            super.deleteArticle();
        } else {
            Toast.makeText(this, getString(R.string.deleteArticleError), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes all of this Person's Memberships to different Groups.
     * @return True if all Memberships were deleted successfully; false otherwise.
     */
    private boolean deleteAllMemberships() {
        boolean membershipsWereDeleted = true;

        ArrayList<Membership> allMemberships = (
                (MembershipsAdapter) membershipsList.getAdapter()).getMemberships();

        Membership membership;
        int index = 0;
        while ((index < allMemberships.size()) && (membershipsWereDeleted)) {
            membership = allMemberships.get(index);
            membershipsWereDeleted = ExternalDeleter.deleteMembership(this, membership);
            index++;
        }

        return membershipsWereDeleted;
    }

    /**
     * Deletes all of this Person's Residences at different Places.
     * @return True if all Residences were deleted successfully; false otherwise.
     */
    private boolean deleteAllResidences() {
        boolean residencesWereDeleted = true;

        ArrayList<Residence> allResidences = (
                (ResidencesAdapter) residencesList.getAdapter()).getResidences();

        Residence residence;
        int index = 0;
        while ((index < allResidences.size()) && (residencesWereDeleted)) {
            residence = allResidences.get(index);
            residencesWereDeleted = ExternalDeleter.deleteResidence(this, residence);
            index++;
        }

        return residencesWereDeleted;
    }

    @Override
    protected boolean renameArticle(String newName) {
        boolean renameWasSuccessful = false;

        if ((renamePersonInMemberships(newName)) && (renamePersonInResidences(newName))) {
            renameWasSuccessful = super.renameArticle(newName);
        } else {
            Toast.makeText(this, R.string.renameArticleError, Toast.LENGTH_SHORT).show();
        }

        return renameWasSuccessful;
    }

    /**
     * <p>
     *     Updates all of this Person's Memberships to reflect a new name for this Person.
     * </p>
     * <p>
     *     If one or more Memberships failed to be updated, an error message is displayed.
     * </p>
     * @param newName The new name for this Person.
     * @return True if all Memberships updated successfully; false otherwise.
     */
    private boolean renamePersonInMemberships(String newName) {
        boolean membershipsWereUpdated = true;
        MembershipsAdapter adapter = (MembershipsAdapter) membershipsList.getAdapter();
        ArrayList<Membership> memberships = adapter.getMemberships();

        int index = 0;
        Membership membership;
        while ((index < memberships.size()) && (membershipsWereUpdated)) {
            membership = memberships.get(index);

            if (ExternalWriter.renameMemberInMembership(this, membership, newName)) {
                membership.memberName = newName;
            } else {
                membershipsWereUpdated = false;
            }

            index++;
        }

        return membershipsWereUpdated;
    }

    /**
     * <p>
     *     Updates all of this Person's Residences to reflect a new name for this Person.
     * </p>
     * <p>
     *     If one or more Residences failed to be updated, an error message is displayed.
     * </p>
     * @param newName The new name for this Person.
     * @return True if all Residences updated successfully; false otherwise.
     */
    private boolean renamePersonInResidences(String newName) {
        boolean residencesWereUpdated = true;
        ResidencesAdapter adapter = (ResidencesAdapter) residencesList.getAdapter();
        ArrayList<Residence> residences = adapter.getResidences();

        int index = 0;
        Residence residence;
        while ((index < residences.size()) && (residencesWereUpdated)) {
            residence = residences.get(index);

            if (ExternalWriter.renameResidentInResidence(this, residence, newName)) {
                residence.residentName = newName;
            } else {
                residencesWereUpdated = false;
            }

            index++;
        }

        return residencesWereUpdated;
    }

}
