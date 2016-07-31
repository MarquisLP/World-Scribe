package com.averi.worldscribe.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

import com.averi.worldscribe.Connection;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.IntentFields;

public class EditConnectionActivity extends AppCompatActivity {

    private Connection connection;
    private TextView mainArticleNameText;
    private EditText mainArticleRelationText;
    private TextView otherArticleNameText;
    private EditText otherArticleRelationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_connection);

        mainArticleNameText = (TextView) findViewById(R.id.textCurrentArticleName);
        mainArticleRelationText = (EditText) findViewById(R.id.editCurrentArticleRelation);
        otherArticleNameText = (TextView) findViewById(R.id.textOtherArticleName);
        otherArticleRelationText = (EditText) findViewById(R.id.editOtherArticleRelation);

        connection = (Connection) getIntent().getSerializableExtra(IntentFields.CONNECTION);
        mainArticleNameText.setText(connection.articleName);
        mainArticleRelationText.setText(connection.articleRelation);
        otherArticleNameText.setText(connection.connectedArticleName);
        otherArticleRelationText.setText(connection.connectedArticleRelation);

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
