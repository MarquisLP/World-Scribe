package com.averi.worldscribe.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.ThemedSnackbar;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.IntentFields;

/**
 * The Activity where the user names the new Snippet during Snippet creation.
 */
public class CreateSnippetActivity extends BackButtonActivity {

    private String worldName;
    private Category articleCategory;
    private String articleName;

    private CoordinatorLayout coordinatorLayout;
    private EditText nameField;
    private Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent startupIntent = getIntent();
        worldName = startupIntent.getStringExtra(IntentFields.WORLD_NAME);
        articleCategory = (Category) startupIntent.getSerializableExtra(IntentFields.CATEGORY);
        articleName = startupIntent.getStringExtra(IntentFields.ARTICLE_NAME);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        nameField = (EditText) findViewById(R.id.nameField);
        createButton = (Button) findViewById(R.id.create);

        setAppBar();
        setNameFieldWatcher();
        ActivityUtilities.enableWordWrapOnSingleLineEditText(nameField);

    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_create_snippet;
    }

    @Override
    protected void setAppBar() {
        Toolbar appBar = (Toolbar) findViewById(R.id.my_toolbar);
        if (appBar != null) {
            appBar.setTitle(R.string.createSnippetTitle);
            setSupportActionBar(appBar);
        }

        super.setAppBar();
    }

    @Override
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.coordinatorLayout);
    }

    /**
     * Sets a TextWatcher to monitor the contents of the name text field, and activate or
     * deactivate the Create button as appropriate.
     */
    private void setNameFieldWatcher() {
        nameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                activateCreateButtonWhenNameIsNonEmpty();
            }
        });
    }

    /**
     * Allows the "Create" button to respond to clicks if and only if the name field is non-empty.
     * This is to ensure that every Snippet has a name.
     */
    private void activateCreateButtonWhenNameIsNonEmpty() {
        if (nameIsEmpty()) {
            createButton.setEnabled(false);
        } else {
            createButton.setEnabled(true);
        }
    }

    /**
     * @return True if there is no text entered into the name text field.
     */
    private boolean nameIsEmpty() {
        return (nameField.getText().toString().isEmpty());
    }

    /**
     * Respond to the user confirming creation.
     * @param view The View that was clicked for confirmation.
     */
    public void clickCreate(View view) {
        if (snippetNameIsAvailable()) {
            ExternalWriter.writeSnippetContents(this, worldName, articleCategory, articleName,
                    nameField.getText().toString(), "");
            editNewSnippet();
        }
    }

    /**
     * Checks if the name entered into the text field is available i.e. none of the Article's
     * existing Snippets use the same name.
     * If the name is unavailable, an error message is displayed.
     * @return True if the Snippet name is available; false otherwise.
     */
    private boolean snippetNameIsAvailable() {
        String snippetName = nameField.getText().toString();
        Boolean nameIsAvailable;

        if (ExternalReader.snippetExists(this, worldName, articleCategory, articleName,
                snippetName)) {
            nameIsAvailable = false;
            ThemedSnackbar.showSnackbarMessage(this, coordinatorLayout,
                    getString(R.string.snippetExistsError, snippetName, articleName));
        } else {
            nameIsAvailable = true;
        }

        return nameIsAvailable;
    }

    /**
     * Open the newly-created Snippet in SnippetActivity for editing.
     */
    private void editNewSnippet() {
        Intent editSnippetIntent = new Intent(this, SnippetActivity.class);
        editSnippetIntent.putExtra(IntentFields.WORLD_NAME, worldName);
        editSnippetIntent.putExtra(IntentFields.CATEGORY, articleCategory);
        editSnippetIntent.putExtra(IntentFields.ARTICLE_NAME, articleName);
        editSnippetIntent.putExtra(IntentFields.SNIPPET_NAME, nameField.getText().toString());
        startActivity(editSnippetIntent);
        finish();
    }

}
