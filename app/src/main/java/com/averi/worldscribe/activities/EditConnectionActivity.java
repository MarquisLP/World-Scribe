package com.averi.worldscribe.activities;

import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.averi.worldscribe.Connection;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.IntentFields;

public class EditConnectionActivity extends AppCompatActivity {

    private Connection connection;

    private CoordinatorLayout layoutRoot;
    private TextView mainArticleNameText;
    private EditText mainArticleRelationText;
    private TextView otherArticleNameText;
    private EditText otherArticleRelationText;

    private boolean mainArticleRelationWasEdited = false;
    private boolean otherArticleRelationWasEdited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_connection);

        layoutRoot = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mainArticleNameText = (TextView) findViewById(R.id.textCurrentArticleName);
        mainArticleRelationText = (EditText) findViewById(R.id.editCurrentArticleRelation);
        otherArticleNameText = (TextView) findViewById(R.id.textOtherArticleName);
        otherArticleRelationText = (EditText) findViewById(R.id.editOtherArticleRelation);

        connection = (Connection) getIntent().getSerializableExtra(IntentFields.CONNECTION);
        mainArticleNameText.setText(connection.articleName);
        mainArticleRelationText.setText(connection.articleRelation);
        otherArticleNameText.setText(connection.connectedArticleName);
        otherArticleRelationText.setText(connection.connectedArticleRelation);

        mainArticleRelationText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mainArticleRelationWasEdited = true;
            }
        });

        otherArticleRelationText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                otherArticleRelationWasEdited = true;
            }
        });

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveEditItem:
                saveRelationsIfEdited();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Saves each Article's relation to file if they were edited.
     * If either or both relations fail to save, an error message is displayed.
     */
    public void saveRelationsIfEdited() {
        boolean errorSavingMainArticleRelation = false;
        boolean errorSavingOtherArticleRelation = false;

        if (mainArticleRelationWasEdited) {
            errorSavingMainArticleRelation = (!(ExternalWriter.saveConnectionRelation(
                    this, connection.worldName, connection.articleCategory,
                    connection.articleName, connection.connectedArticleCategory,
                    connection.connectedArticleName,
                    mainArticleRelationText.getText().toString())));
        }

        if (otherArticleRelationWasEdited) {
            errorSavingOtherArticleRelation = (!(ExternalWriter.saveConnectionRelation(
                    this, connection.worldName, connection.connectedArticleCategory,
                    connection.connectedArticleName, connection.articleCategory,
                    connection.articleName, otherArticleRelationText.getText().toString())));
        }

        if ((errorSavingMainArticleRelation) || (errorSavingOtherArticleRelation)) {
            Toast.makeText(this, getString(R.string.saveConnectionError),
                    Toast.LENGTH_SHORT).show();
            // TODO: Delete both relation files to prevent possible crashes.
        }
    }

}
