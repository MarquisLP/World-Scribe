package com.averi.worldscribe.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.AppPreferences;

public class SnippetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snippet);

        Intent intent = getIntent();
        String snippetName = intent.getStringExtra(AppPreferences.SNIPPET_NAME);
        setAppBarTitle(snippetName);
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
}
