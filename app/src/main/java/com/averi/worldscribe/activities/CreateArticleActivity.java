package com.averi.worldscribe.activities;

import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.ErrorMessager;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;

public class CreateArticleActivity extends BackButtonActivity {

    public static final int PERSON_ITEM_POSITION = 0;
    public static final int GROUP_ITEM_POSITION = 1;
    public static final int PLACE_ITEM_POSITION = 2;
    public static final int ITEM_ITEM_POSITION = 3;
    public static final int CONCEPT_ITEM_POSITION = 4;

    private CoordinatorLayout coordinatorLayout;
    private EditText nameField;
    private Spinner categorySpinner;
    private Button createButton;
    private String worldName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_article);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        nameField = (EditText) findViewById(R.id.articleName);
        categorySpinner = (Spinner) findViewById(R.id.categorySelection);
        createButton = (Button) findViewById(R.id.create);
        worldName = getIntent().getStringExtra(IntentFields.WORLD_NAME);

        setAppBar();
        populateCategorySpinner();
        selectInitialCategory();
        addTextListener();
    }

    @Override
    protected void setAppBar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.createArticleTitle);

        super.setAppBar();
    }

    private void populateCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories_array, R.layout.spinner_item);
        categorySpinner.setAdapter(adapter);
    }

    /**
     * Select the initial Category to display in the spinner, based on the Category of the
     * previous activity.
     */
    private void selectInitialCategory() {
        Category previousCategory = (Category) getIntent().getSerializableExtra(
                IntentFields.CATEGORY);
        switch (previousCategory) {
            case Person:
                categorySpinner.setSelection(PERSON_ITEM_POSITION);
                break;
            case Group:
                categorySpinner.setSelection(GROUP_ITEM_POSITION);
                break;
            case Place:
                categorySpinner.setSelection(PLACE_ITEM_POSITION);
                break;
            case Item:
                categorySpinner.setSelection(ITEM_ITEM_POSITION);
                break;
            case Concept:
            default:
                categorySpinner.setSelection(CONCEPT_ITEM_POSITION);
                break;
        }
    }

    /**
     * Add an EditText listener to disable the Create button when the EditText is empty.
     */
    private void addTextListener() {
        nameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                activateCreateButtonWhenNameIsNonempty();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Activate the Create button if and only if the Article's name is non-empty.
     */
    private void activateCreateButtonWhenNameIsNonempty() {
        if (nameIsEmpty()) {
            createButton.setEnabled(false);
        } else {
            createButton.setEnabled(true);
        }
    }

    /**
     * @return True if the Article's name is non-empty; false otherwise.
     */
    private boolean nameIsEmpty() { return (getArticleName().length() == 0); }

    /**
     * @return The name entered for the Article.
     */
    private String getArticleName() { return nameField.getText().toString(); }

    /**
     * Creates a new Article of the specified name and {@link Category}.
     * If an Article of the same name and Category already exists, an error is displayed
     * instead.
     * @param view The View this method is bound to.
     */
    public void createArticle(View view) {
        Category category = Category.getCategoryFromName(this,
                categorySpinner.getSelectedItem().toString());
        String articleName = getArticleName();

        if (ExternalReader.articleExists(this, worldName, category, articleName)) {
            ErrorMessager.showSnackbarMessage(this, coordinatorLayout,
                    getString(R.string.articleAlreadyExistsError));
        } else {
            boolean articleWasCreated = ExternalWriter.createArticleDirectory(
                    this, worldName, category, articleName);
            if (articleWasCreated) {
                goToNewArticle(worldName, category, articleName);
            } else {
                ErrorMessager.showSnackbarMessage(this, coordinatorLayout,
                        getString(R.string.articleCreationError));
            }
        }
    }

    /**
     * Display the newly-created Article in the appropriate Activity.
     * @param worldName The name of the world the Article belongs to.
     * @param category The {@link Category} the Article belongs to.
     * @param articleName The name of the new Article.
     */
    private void goToNewArticle(String worldName, Category category, String articleName) {
        Intent goToNewArticleIntent;

        switch (category) {
            case Person:
                goToNewArticleIntent = new Intent(this, PersonActivity.class);
                break;
            case Group:
                goToNewArticleIntent = new Intent(this, GroupActivity.class);
                break;
            case Place:
                goToNewArticleIntent = new Intent(this, PlaceActivity.class);
                break;
            case Item:
                goToNewArticleIntent = new Intent(this, ItemActivity.class);
                break;
            case Concept:
            default:
                goToNewArticleIntent = new Intent(this, ConceptActivity.class);
        }

        goToNewArticleIntent.putExtra(IntentFields.WORLD_NAME, worldName);
        goToNewArticleIntent.putExtra(IntentFields.CATEGORY, category);
        goToNewArticleIntent.putExtra(IntentFields.ARTICLE_NAME, articleName);

        startActivity(goToNewArticleIntent);
        finish();
    }

}
