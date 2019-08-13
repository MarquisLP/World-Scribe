package com.averi.worldscribe.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.IntentFields;

public class SnippetActivity extends ReaderModeActivity {

    /**
     * The text field for editing the Snippet's content.
     */
    private EditText editText;

    /**
     * Set to true if the Snippet was edited since the last time it was saved.
     */
    private boolean editedSinceLastSave = false;

    /**
     * The name of the World that the Article possessing the Snippet belongs to.
     */
    private String worldName;

    /**
     * The Category of the Article possessing the Snippet.
     */
    private Category category;

    /**
     * The name of the Article possessing the Snippet.
     */
    private String articleName;

    /**
     * The name of the Snippet displayed in this Activity.
     */
    private String snippetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        editText = (EditText) findViewById(R.id.editText);

        Intent intent = getIntent();
        worldName = intent.getStringExtra(IntentFields.WORLD_NAME);
        category = (Category) intent.getSerializableExtra(IntentFields.CATEGORY);
        articleName = intent.getStringExtra(IntentFields.ARTICLE_NAME);
        snippetName = intent.getStringExtra(IntentFields.SNIPPET_NAME);

        setAppBar();
        loadSnippetContent(intent.getStringExtra(IntentFields.WORLD_NAME),
                (Category) intent.getSerializableExtra(IntentFields.CATEGORY),
                intent.getStringExtra(IntentFields.ARTICLE_NAME),
                snippetName);

        if (AppPreferences.nightModeIsEnabled(this)) {
            darkenTextGradient();
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) { editedSinceLastSave = true; }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.snippet_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_snippet;
    }

    @Override
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.root);
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveSnippetContentIfEdited();
    }

    @Override
    protected void setAppBar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(snippetName);

        super.setAppBar();
    }

    /**
     * Changes the gradient at the bottom of the text box to a darker variant.
     */
    private void darkenTextGradient() {
        View textGradient = findViewById(R.id.textGradient);
        assert textGradient != null;
        textGradient.setBackgroundResource( R.drawable.textbox_gradient_night_mode);
    }

    /**
     * Display the contents of the Snippet stored on file.
     */
    private void loadSnippetContent(String worldName, Category category, String articleName,
                                    String snippetName) {
        String snippetContent = ExternalReader.getSnippetText(this, worldName, category,
                articleName, snippetName);
        editText.setText(snippetContent);
    }

    /**
     * Saves any edits made to the Snippet's content to the corresponding text file.
     */
    private void saveSnippetContentIfEdited() {
        if (editedSinceLastSave) {
            String snippetContent = editText.getText().toString();
            ExternalWriter.writeSnippetContents(this, worldName, category, articleName,
                    snippetName, snippetContent);
            editedSinceLastSave = false;
        }

    }
}
