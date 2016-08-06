package com.averi.worldscribe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.averi.worldscribe.utilities.IntentFields;

/**
 * The Activity where the user names the new Snippet during Snippet creation.
 */
public class CreateSnippetActivity extends AppCompatActivity {

    private String worldName;
    private Category articleCategory;
    private String articleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_snippet);

        Intent startupIntent = getIntent();
        worldName = startupIntent.getStringExtra(IntentFields.WORLD_NAME);
        articleCategory = (Category) startupIntent.getSerializableExtra(IntentFields.CATEGORY);
        articleName = startupIntent.getStringExtra(IntentFields.ARTICLE_NAME);
    }

    public void clickCreate(View view) {

    }
}
