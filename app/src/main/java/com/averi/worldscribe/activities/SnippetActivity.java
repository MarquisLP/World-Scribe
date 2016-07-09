package com.averi.worldscribe.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.ExternalReader;

public class SnippetActivity extends AppCompatActivity {

    /**
     * The text field for editing the Snippet's content.
     */
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet);

        editText = (EditText) findViewById(R.id.editText);

        Intent intent = getIntent();
        String snippetName = intent.getStringExtra(AppPreferences.SNIPPET_NAME);
        setAppBarTitle(snippetName);
        loadSnippetContent(intent.getStringExtra(AppPreferences.WORLD_NAME),
                (Category) intent.getSerializableExtra(AppPreferences.CATEGORY),
                intent.getStringExtra(AppPreferences.ARTICLE_NAME),
                snippetName);
    }

    /**
     * Setup the app bar and set its title to be the Snippet's name.
     * @param snippetName The name of the Snippet displayed in this Activity.
     */
    private void setAppBarTitle(String snippetName) {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(snippetName);
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
}
