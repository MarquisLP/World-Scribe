package com.averi.worldscribe.activities;

import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.ErrorMessager;
import com.averi.worldscribe.utilities.ExternalReader;

public class CreateArticleActivity extends AppCompatActivity {

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
        worldName = getIntent().getStringExtra(AppPreferences.WORLD_NAME);

        populateCategorySpinner();
        selectInitialCategory();
        addTextListener();
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
                AppPreferences.CATEGORY);
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
            // Create the new Article.
        }
    }

}
