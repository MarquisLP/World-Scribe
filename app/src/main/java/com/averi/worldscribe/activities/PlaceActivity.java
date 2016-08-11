package com.averi.worldscribe.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.averi.worldscribe.ArticleTextField;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.Residence;
import com.averi.worldscribe.adapters.ResidencesAdapter;
import com.averi.worldscribe.adapters.ResidentsAdapter;
import com.averi.worldscribe.utilities.ExternalDeleter;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.views.BottomBar;

import java.util.ArrayList;

public class PlaceActivity extends ArticleActivity {

    /**
     * The request code for a new Resident within this Place.
     */
    public static final int RESULT_NEW_RESIDENT = 300;

    private RecyclerView residentsList;
    private Button addResidentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        residentsList = (RecyclerView) findViewById(R.id.recyclerResidents);
        addResidentButton = (Button) findViewById(R.id.buttonAddResident);

        addResidentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addResident();
            }
        });
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

        textFields.add(new ArticleTextField(resources.getString(R.string.descriptionHint),
                (EditText) findViewById(R.id.editDescription),
                this, getWorldName(), Category.Place, getArticleName()));
        textFields.add(new ArticleTextField(resources.getString(R.string.historyHint),
                (EditText) findViewById(R.id.editHistory),
                this, getWorldName(), Category.Place, getArticleName()));

        return textFields;
    }

    @Override
    protected void onResume() {
        super.onResume();

        populateResidences();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_NEW_RESIDENT:
                if (resultCode == RESULT_OK) {
                    Residence newResidence = new Residence();
                    newResidence.worldName = getWorldName();
                    newResidence.placeName = getArticleName();
                    newResidence.residentName = data.getStringExtra(IntentFields.ARTICLE_NAME);
                    ExternalWriter.saveResidence(this, newResidence);
                }
        }
    }

    /**
     * Populate the Residents RecyclerView with cards for this Place's Residents.
     */
    private void populateResidences() {
        residentsList.setLayoutManager(new LinearLayoutManager(this));
        residentsList.setAdapter(new ResidentsAdapter(this, getWorldName(), getArticleName()));
    }

    /**
     * Opens SelectArticleActivity so the user can select the Person for a new
     * {@link com.averi.worldscribe.Residence Residence} within this Place.
     */
    private void addResident() {
        Intent selectGroupIntent = new Intent(this, SelectArticleActivity.class);
        ResidentsAdapter residentsAdapter = (ResidentsAdapter) residentsList.getAdapter();

        selectGroupIntent.putExtra(IntentFields.WORLD_NAME, getWorldName());
        selectGroupIntent.putExtra(IntentFields.CATEGORY, Category.Person);
        selectGroupIntent.putExtra(IntentFields.MAIN_ARTICLE_CATEGORY, Category.Place);
        selectGroupIntent.putExtra(IntentFields.MAIN_ARTICLE_NAME, getArticleName());
        selectGroupIntent.putExtra(IntentFields.EXISTING_LINKS,
                residentsAdapter.getLinkedArticleList());
        startActivityForResult(selectGroupIntent, RESULT_NEW_RESIDENT);
    }

    @Override
    protected void deleteArticle() {
        if (removeAllResidents()) {
            super.deleteArticle();
        } else {
            Toast.makeText(this, getString(R.string.deleteArticleError), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes all Residences at this Place.
     * @return True if all Residences were deleted successfully; false otherwise.
     */
    private boolean removeAllResidents() {
        boolean residencesWereDeleted = true;

        ArrayList<Residence> allResidences = (
                (ResidentsAdapter) residentsList.getAdapter()).getResidences();

        Residence residence;
        int index = 0;
        while ((index < allResidences.size()) && (residencesWereDeleted)) {
            residence = allResidences.get(index);
            residencesWereDeleted = ExternalDeleter.deleteResidence(this, residence);
            index++;
        }

        return residencesWereDeleted;
    }

}
