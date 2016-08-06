package com.averi.worldscribe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.averi.worldscribe.utilities.IntentFields;

/**
 * The Activity where the user names the new Snippet during Snippet creation.
 */
public class CreateSnippetActivity extends AppCompatActivity {

    private String worldName;
    private Category articleCategory;
    private String articleName;

    private EditText nameField;
    private Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_snippet);

        Intent startupIntent = getIntent();
        worldName = startupIntent.getStringExtra(IntentFields.WORLD_NAME);
        articleCategory = (Category) startupIntent.getSerializableExtra(IntentFields.CATEGORY);
        articleName = startupIntent.getStringExtra(IntentFields.ARTICLE_NAME);

        nameField = (EditText) findViewById(R.id.nameField);
        createButton = (Button) findViewById(R.id.create);

        setAppBar();
        setNameFieldWatcher();
    }

    /**
     * Sets up this Activity's app bar.
     */
    private void setAppBar() {
        Toolbar appBar = (Toolbar) findViewById(R.id.my_toolbar);
        if (appBar != null) {
            appBar.setTitle(R.string.createSnippetTitle);
            setSupportActionBar(appBar);
        }
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

    public void clickCreate(View view) {

    }

}
