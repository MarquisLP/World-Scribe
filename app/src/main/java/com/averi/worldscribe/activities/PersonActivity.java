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
import com.averi.worldscribe.utilities.TaskRunner;
import com.averi.worldscribe.utilities.tasks.SaveResidenceTask;
import com.averi.worldscribe.viewmodels.PersonViewModel;
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

    private PersonViewModel viewModel;

    private RadioGroup genderGroup;
    private RecyclerView membershipsList;
    private ProgressBar membershipsProgressCircle;
    private RecyclerView residencesList;
    private ProgressBar residencesProgressCircle;
    private Button addMembershipButton;
    private Button addResidenceButton;
    private Boolean genderWasEditedSinceLastSave = false;
    private RadioGroup.OnCheckedChangeListener genderListener;

    private final TaskRunner taskRunner = new TaskRunner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        genderGroup = (RadioGroup) findViewById(R.id.radioGroupGender);
        membershipsList = (RecyclerView) findViewById(R.id.recyclerMemberships);
        membershipsProgressCircle = (ProgressBar)  findViewById(R.id.membershipsProgressCircle);
        residencesList = (RecyclerView) findViewById(R.id.recyclerResidences);
        residencesProgressCircle = (ProgressBar)  findViewById(R.id.residencesProgressCircle);
        addMembershipButton = (Button) findViewById(R.id.buttonAddMembership);
        addResidenceButton = (Button) findViewById(R.id.buttonAddResidence);

        viewModel = new ViewModelProvider(this).get(PersonViewModel.class);

        setupMembershipsList();
        setupResidencesList();
        setupErrorDialog();

        genderListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                genderWasEditedSinceLastSave = true;
            }
        };
        genderGroup.setOnCheckedChangeListener(genderListener);

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

        textFields.add(new ArticleTextField("Aliases",
                (EditText) findViewById(R.id.editAliases),
                this, getWorldName(), Category.Person, getArticleName()));
        textFields.add(new ArticleTextField("Age",
                (EditText) findViewById(R.id.editAge),
                this, getWorldName(), Category.Person, getArticleName()));
        textFields.add(new ArticleTextField("Biography",
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

    /**
     * Load this Person's gender and display it.
     * @param resources A Resources instance containing this app's resource files and values.
     */
    private void loadGender(Resources resources) {
        // Prevent the 'save Gender to file' function from triggering.
        genderGroup.setOnCheckedChangeListener(null);

        String genderString = ExternalReader.getArticleTextFieldData(this, super.getWorldName(),
                super.getCategory(), super.getArticleName(),
                "Gender");

        // NextCloud might append some weird characters at the end of the Gender.txt file,
        // so we use startsWith (instead of equals) to account for that.
        if (genderString.startsWith("Male")) {
            genderGroup.check(R.id.radioButtonMale);
        } else if (genderString.startsWith("Female")) {
            genderGroup.check(R.id.radioButtonFemale);
        } else {
            genderGroup.check(R.id.radioButtonOtherGender);
        }

        genderGroup.setOnCheckedChangeListener(genderListener);
    }

    private void setupMembershipsList() {
        MembershipsAdapter adapter = new MembershipsAdapter(this, getWorldName(), getArticleName());
        viewModel.getMemberships().observe(this, new Observer<ArrayList<Membership>>() {
            @Override
            public void onChanged(ArrayList<Membership> memberships) {
                if (memberships == null) {
                    membershipsProgressCircle.setVisibility(View.VISIBLE);
                    membershipsList.setVisibility(View.GONE);
                }
                else {
                    adapter.updateList(memberships);
                    adapter.notifyDataSetChanged();
                    membershipsList.setVisibility(View.VISIBLE);
                    membershipsProgressCircle.setVisibility(View.GONE);
                }
            }
        });
        membershipsList.setAdapter(adapter);
        membershipsList.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Populate the Memberships RecyclerView with cards for this Person's Memberships.
     */
    private void populateMemberships() {
        viewModel.loadMemberships(getWorldName(), getArticleName());
    }

    private void setupResidencesList() {
        ResidencesAdapter adapter = new ResidencesAdapter(this, getWorldName(), getArticleName());
        viewModel.getResidences().observe(this, new Observer<ArrayList<Residence>>() {
            @Override
            public void onChanged(ArrayList<Residence> residences) {
                if (residences == null) {
                    residencesProgressCircle.setVisibility(View.VISIBLE);
                    residencesList.setVisibility(View.GONE);
                }
                else {
                    adapter.updateList(residences);
                    adapter.notifyDataSetChanged();
                    residencesList.setVisibility(View.VISIBLE);
                    residencesProgressCircle.setVisibility(View.GONE);
                }
            }
        });
        residencesList.setAdapter(adapter);
        residencesList.setLayoutManager(new LinearLayoutManager(this));
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
     * Populate the Residences RecyclerView with cards for this Person's Residences.
     */
    private void populateResidences() {
        viewModel.loadResidences(getWorldName(), getArticleName());
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
            String genderFilename = "Gender";

            if (genderGroup.getCheckedRadioButtonId() == R.id.radioButtonMale) {
                ExternalWriter.writeStringToArticleFile(this, getWorldName(), Category.Person,
                        getArticleName(), genderFilename, "Male");
            } else if (genderGroup.getCheckedRadioButtonId() == R.id.radioButtonFemale) {
                ExternalWriter.writeStringToArticleFile(this, getWorldName(), Category.Person,
                        getArticleName(), genderFilename, "Female");
            } else {
                ExternalWriter.writeStringToArticleFile(this, getWorldName(), Category.Person,
                        getArticleName(), genderFilename,
                        "Other");
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
}
