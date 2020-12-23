package com.averi.worldscribe.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.averi.worldscribe.ArticleTextField;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.Residence;
import com.averi.worldscribe.adapters.ResidentsAdapter;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.ExternalDeleter;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.TaskRunner;
import com.averi.worldscribe.utilities.tasks.SaveResidenceTask;
import com.averi.worldscribe.viewmodels.PlaceViewModel;
import com.averi.worldscribe.views.ArticleSectionCollapser;
import com.averi.worldscribe.views.BottomBar;

import java.util.ArrayList;

public class PlaceActivity extends ArticleActivity {

    /**
     * The request code for a new Resident within this Place.
     */
    public static final int RESULT_NEW_RESIDENT = 300;

    private PlaceViewModel viewModel;
    private RecyclerView residentsList;
    private Button addResidentButton;
    private ProgressBar residentsProgressCircle;

    private final TaskRunner taskRunner = new TaskRunner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PlaceViewModel.class);

        residentsList = (RecyclerView) findViewById(R.id.recyclerResidents);
        addResidentButton = (Button) findViewById(R.id.buttonAddResident);
        residentsProgressCircle = (ProgressBar)  findViewById(R.id.residentsLoadingCircle);

        setupResidentsList();
        setupErrorDialog();

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
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.coordinatorLayout);
    }

    @Override
    protected NestedScrollView getNestedScrollView() {
        return (NestedScrollView) findViewById(R.id.scrollView);
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

        textFields.add(new ArticleTextField("Description",
                (EditText) findViewById(R.id.editDescription),
                this, getWorldName(), Category.Place, getArticleName()));
        textFields.add(new ArticleTextField("History",
                (EditText) findViewById(R.id.editHistory),
                this, getWorldName(), Category.Place, getArticleName()));

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
    protected ProgressBar getConnectionsProgressCircle() {
        return (ProgressBar) findViewById(R.id.connectionsProgressCircle);
    }

    @Override
    protected ProgressBar getSnippetsProgressCircle() {
        return (ProgressBar) findViewById(R.id.snippetsProgressCircle);
    }

    @Override
    protected ProgressBar getImageProgressCircle() {
        return (ProgressBar) findViewById(R.id.articleImageProgressCircle);
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
                    taskRunner.executeAsync(new SaveResidenceTask(newResidence),
                            (result) -> { this.populateResidences(); },
                            this::displayErrorDialog);
                }
        }
    }

    private void displayErrorDialog(Exception exception) {
        ActivityUtilities.buildExceptionDialog(this,
                Log.getStackTraceString(exception), (dialogInterface) -> {}).show();
    }

    @Override
    protected void addSectionCollapsers() {
        TextView residentsHeader = (TextView) findViewById(R.id.textResidents);

        residentsHeader.setOnClickListener(new ArticleSectionCollapser(this, residentsHeader,
                (LinearLayout) findViewById(R.id.linearResidents)));

        super.addSectionCollapsers();
    }

    private void setupResidentsList() {
        ResidentsAdapter adapter = new ResidentsAdapter(this, getWorldName(), getArticleName());
        viewModel.getResidents().observe(this, new Observer<ArrayList<Residence>>() {
            @Override
            public void onChanged(ArrayList<Residence> residences) {
                if (residences == null) {
                    residentsProgressCircle.setVisibility(View.VISIBLE);
                    residentsList.setVisibility(View.GONE);
                }
                else {
                    adapter.updateList(residences);
                    adapter.notifyDataSetChanged();
                    residentsList.setVisibility(View.VISIBLE);
                    residentsProgressCircle.setVisibility(View.GONE);
                }
            }
        });
        residentsList.setAdapter(adapter);
        residentsList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupErrorDialog() {
        final Context context = this;
        viewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newErrorMessage) {
                if (!(newErrorMessage.isEmpty())) {
                    ActivityUtilities.buildExceptionDialog(context, newErrorMessage,
                            dialogInterface -> viewModel.clearErrorMessage()
                    ).show();
                }
            }
        });
    }

    /**
     * Populate the Residents RecyclerView with cards for this Place's Residents.
     */
    private void populateResidences() {
        viewModel.loadResidents(getWorldName(), getArticleName());
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
}
