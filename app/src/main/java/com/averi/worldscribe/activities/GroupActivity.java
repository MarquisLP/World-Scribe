package com.averi.worldscribe.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageView;

import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.MembersAdapter;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.views.BottomBar;

public class GroupActivity extends ArticleActivity {

    private RecyclerView membersList;
    private EditText mandateField;
    private EditText historyField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        membersList = (RecyclerView) findViewById(R.id.recyclerMembers);
        mandateField = (EditText) findViewById(R.id.editMandate);
        historyField = (EditText) findViewById(R.id.editHistory);

        populateMembers();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadTextFields();
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_group;
    }

    @Override
    protected ImageView getImageView() { return (ImageView) findViewById(R.id.imageGroup); }

    @Override
    protected BottomBar getBottomBar() {
        return (BottomBar) findViewById(R.id.bottomBar);
    }

    @Override
    protected RecyclerView getConnectionsRecycler() {
        return (RecyclerView) findViewById(R.id.recyclerConnections);
    }

    @Override
    protected RecyclerView getSnippetsRecycler() {
        return (RecyclerView) findViewById(R.id.recyclerSnippets);
    }

    private void populateMembers() {
        membersList.setLayoutManager(new LinearLayoutManager(this));
        membersList.setAdapter(new MembersAdapter(this, getWorldName(), getArticleName()));
    }

    /**
     * Load data for the Group's text fields and display them.
     */
    private void loadTextFields() {
        Resources resources = getResources();
        mandateField.setText(ExternalReader.getArticleTextFieldData(this, getWorldName(),
                getCategory(), getArticleName(), resources.getString(R.string.mandateText)));
        historyField.setText(ExternalReader.getArticleTextFieldData(this, getWorldName(),
                getCategory(), getArticleName(), resources.getString(R.string.historyHint)));
    }
}
