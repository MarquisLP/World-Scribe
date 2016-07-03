package com.averi.worldscribe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

import tools.AppPreferences;

public class PersonActivity extends AppCompatActivity {

    private BottomBar bottomBar;
    private String worldName;
    private Category category;
    private String personName;
    private RecyclerView connectionsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        Intent intent = getIntent();
        worldName = intent.getStringExtra(AppPreferences.WORLD_NAME);
        category = (Category) intent.getSerializableExtra(AppPreferences.CATEGORY);
        personName = intent.getStringExtra(AppPreferences.ARTICLE_NAME);
        connectionsList = (RecyclerView) findViewById(R.id.recyclerConnections);

        setAppBar();
        bottomBar.highlightCategoryButton(this, Category.Person);
        populateConnections();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.article_menu, menu);
        return true;
    }

    private void setAppBar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(personName);
    }

    private void populateConnections() {
        connectionsList.setLayoutManager(new LinearLayoutManager(this));
        connectionsList.setAdapter(new ConnectionsAdapter(this, worldName, category, personName));
    }
}
