package com.averi.worldscribe.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.views.BottomBar;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.StringListAdapter;
import com.averi.worldscribe.adapters.StringListContext;

import java.util.ArrayList;

import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.views.BottomBarActivity;

public class ArticleListActivity extends ThemedActivity
        implements StringListContext, BottomBarActivity {

    private RecyclerView recyclerView;
    private String worldName;
    private Category category;
    private BottomBar bottomBar;
    private TextView textEmpty;
    private ArrayList<String> articleNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        Intent intent = getIntent();
        category = loadCategory(intent);
        worldName = loadWorldName(intent);
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        textEmpty = (TextView) findViewById(R.id.empty);

        setupRecyclerView();
        setAppBar(worldName, category);
        bottomBar.highlightCategoryButton(this, category);
    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(true);

        StringListAdapter adapter = new StringListAdapter(this, articleNames);
        recyclerView.setAdapter(adapter);
    }

    private void setAppBar(String worldName, Category category) {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        String categoryGroupName = category.pluralName(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(categoryGroupName + " — " + worldName);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        populateList(worldName, category);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void populateList(String worldName, Category category) {
        articleNames = ExternalReader.getArticleNamesInCategory(this, worldName, category);
        StringListAdapter adapter = (StringListAdapter) recyclerView.getAdapter();
        adapter.updateList(articleNames);
        adapter.notifyDataSetChanged();

        if (articleNames.isEmpty()) {
            if (textEmpty != null) {
                textEmpty.setVisibility(View.VISIBLE);
            }
            recyclerView.setVisibility(View.GONE);
        } else {
            if (textEmpty != null) {
                textEmpty.setVisibility(View.GONE);
            }
            recyclerView.setVisibility(View.VISIBLE);
        }

    }

    private Category loadCategory(Intent intent) {
        return ((Category) intent.getSerializableExtra(IntentFields.CATEGORY));
    }

    private String loadWorldName(Intent intent) {
        return (intent.getStringExtra(IntentFields.WORLD_NAME));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.createArticleItem:
                Intent goToArticleCreationIntent = new Intent(this, CreateArticleActivity.class);
                goToArticleCreationIntent.putExtra(IntentFields.WORLD_NAME, worldName);
                goToArticleCreationIntent.putExtra(IntentFields.CATEGORY, category);
                startActivity(goToArticleCreationIntent);
                return true;
            case R.id.createWorldItem:
            case R.id.loadWorldItem:
            case R.id.deleteWorldItem:
            case R.id.settingsItem:
                ActivityUtilities.handleCommonAppBarItems(this, worldName, item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void respondToListItemSelection(String itemText) {
        Intent goToArticleIntent;

        switch (category) {
            case Person:
                goToArticleIntent = new Intent(this, PersonActivity.class);
                break;
            case Group:
                goToArticleIntent = new Intent(this, GroupActivity.class);
                break;
            case Place:
                goToArticleIntent = new Intent(this, PlaceActivity.class);
                break;
            case Item:
                goToArticleIntent = new Intent(this, ItemActivity.class);
                break;
            case Concept:
            default:
                goToArticleIntent = new Intent(this, ConceptActivity.class);
                break;
        }

        goToArticleIntent.putExtra(IntentFields.WORLD_NAME, worldName);
        goToArticleIntent.putExtra(IntentFields.CATEGORY, category);
        goToArticleIntent.putExtra(IntentFields.ARTICLE_NAME, itemText);

        startActivity(goToArticleIntent);
    }

    public void respondToBottomBarButton(Category category) {
        this.category = category;
        setAppBar(worldName, category);
        bottomBar.highlightCategoryButton(this, category);
        populateList(worldName, category);
    }

}
