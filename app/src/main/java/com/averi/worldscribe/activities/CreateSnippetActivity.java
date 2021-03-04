package com.averi.worldscribe.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.TaskRunner;
import com.averi.worldscribe.utilities.ThemedSnackbar;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.tasks.GetFilenamesInFolderTask;
import com.averi.worldscribe.utilities.tasks.WriteTextToFileTask;

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
    private ProgressBar savingSnippetProgressCircle;

    private final TaskRunner taskRunner = new TaskRunner();

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
        savingSnippetProgressCircle = findViewById(R.id.creatingSnippetProgressCircle);

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
        String newSnippetName = nameField.getText().toString();

        if (ActivityUtilities.nameHasInvalidCharacters(newSnippetName)) {
            ThemedSnackbar.showSnackbarMessage(this, coordinatorLayout,
                    getString(R.string.renameWithInvalidCharactersError, ActivityUtilities.getInvalidNameCharactersString()));
            return;
        }

        savingSnippetProgressCircle.setVisibility(View.VISIBLE);
        nameField.setVisibility(View.GONE);
        createButton.setVisibility(View.GONE);

        String snippetsFolderPath = worldName + "/" + articleCategory.pluralName(this) + "/"
                + articleName + "/Snippets";
        taskRunner.executeAsync(new GetFilenamesInFolderTask(snippetsFolderPath, true),
                (existingSnippetNames) -> {
                    if (existingSnippetNames.contains(newSnippetName)) {
                        nameField.setVisibility(View.VISIBLE);
                        createButton.setVisibility(View.VISIBLE);
                        savingSnippetProgressCircle.setVisibility(View.GONE);
                        ThemedSnackbar.showSnackbarMessage(this, coordinatorLayout,
                                getString(R.string.snippetExistsError, newSnippetName, articleName));
                    }
                    else {
                        String newSnippetPath = snippetsFolderPath + "/" + newSnippetName + ".txt";
                        taskRunner.executeAsync(new WriteTextToFileTask(newSnippetPath, ""),
                                (result) -> { this.editNewSnippet(newSnippetName); },
                                this::displayErrorDialog);
                    }
                },
                this::displayErrorDialog
                );
    }

    private void displayErrorDialog(Exception exception) {
        nameField.setVisibility(View.VISIBLE);
        createButton.setVisibility(View.VISIBLE);
        savingSnippetProgressCircle.setVisibility(View.GONE);
        ActivityUtilities.buildExceptionDialog(this,
                Log.getStackTraceString(exception), (dialogInterface) -> {}).show();
    }

    /**
     * Open the newly-created Snippet in SnippetActivity for editing.
     */
    private void editNewSnippet(String newSnippetName) {
        Intent editSnippetIntent = new Intent(this, SnippetActivity.class);
        editSnippetIntent.putExtra(IntentFields.WORLD_NAME, worldName);
        editSnippetIntent.putExtra(IntentFields.CATEGORY, articleCategory);
        editSnippetIntent.putExtra(IntentFields.ARTICLE_NAME, articleName);
        editSnippetIntent.putExtra(IntentFields.SNIPPET_NAME, newSnippetName);
        startActivity(editSnippetIntent);
        finish();
    }

}
