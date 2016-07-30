package com.averi.worldscribe.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.IntentFields;

public class EditConnectionActivity extends AppCompatActivity {

    private TextView mainArticleNameText;
    private TextView otherArticleNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_connection);

        mainArticleNameText = (TextView) findViewById(R.id.textCurrentArticleName);
        otherArticleNameText = (TextView) findViewById(R.id.textOtherArticleName);

        Intent startupIntent = getIntent();
        mainArticleNameText.setText(startupIntent.getStringExtra(
                IntentFields.ARTICLE_NAME));
        otherArticleNameText.setText(startupIntent.getStringExtra(
                IntentFields.CONNECTED_ARTICLE_NAME));

        setAppBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Set up this Activity's app bar.
     */
    private void setAppBar() {
        Toolbar appBar = (Toolbar) findViewById(R.id.my_toolbar);
        if (appBar != null) {
            appBar.setTitle(R.string.editConnectionTitle);
            setSupportActionBar(appBar);
        }
    }

}
