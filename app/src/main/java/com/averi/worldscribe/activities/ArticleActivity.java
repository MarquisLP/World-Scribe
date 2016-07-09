package com.averi.worldscribe.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ImageView;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.ConnectionsAdapter;
import com.averi.worldscribe.adapters.SnippetsAdapter;
import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.views.BottomBar;

import java.io.File;

/**
 * Created by mark on 05/07/16.
 *
 * ArticleActivity is the superclass for each Activity that displays a certain Category of Articles.
 *
 * All subclasses must implement {@link #getLayoutResourceID} using the Layout Resource file
 * corresponding to their respective Categories, as well as the other abstract methods for
 * obtaining specific Views.
 */
public abstract class ArticleActivity extends AppCompatActivity {

    /**
     * The display for the Article's image.
     */
    private ImageView imageView;
    /**
     * The BottomBar navigation View.
     */
    private BottomBar bottomBar;
    /**
     * The name of the World in which this Article exists.
     */
    private String worldName;
    /**
     * The {@link Category} this Article belongs to.
     */
    private Category category;
    /**
     * The name of the Article displayed by this Activity.
     */
    private String articleName;

    public String getWorldName() { return worldName; }

    public Category getCategory() { return category; }

    public String getArticleName() { return articleName; }

    /**
     * Contains cards for all of the Article's {@link com.averi.worldscribe.Connection}s.
     */
    private RecyclerView connectionsList;
    /**
     * Contains cards for all of the Article's Snippets.
     */
    private RecyclerView snippetsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceID());

        imageView = getImageView();
        bottomBar = getBottomBar();
        Intent intent = getIntent();
        worldName = intent.getStringExtra(AppPreferences.WORLD_NAME);
        category = (Category) intent.getSerializableExtra(AppPreferences.CATEGORY);
        articleName = intent.getStringExtra(AppPreferences.ARTICLE_NAME);
        connectionsList = getConnectionsRecycler();
        snippetsList = getSnippetsRecycler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpArticleCore();
    }

    /**
     * @return The Android Resource ID of this Activity's layout resource file.
     */
    protected abstract int getLayoutResourceID();

    /**
     * @return The Article ImageView for this Activity.
     */
    protected abstract ImageView getImageView();

    /**
     * @return The bottom navigation bar for this Activity.
     */
    protected abstract BottomBar getBottomBar();

    /**
     * @return The RecyclerView for this Article's
     * {@link com.averi.worldscribe.Connection Connection}s.
     */
    protected abstract RecyclerView getConnectionsRecycler();

    /**
     * @return The RecyclerView for this Article's Snippets.
     */
    protected abstract RecyclerView getSnippetsRecycler();

    /**
     * Load data pertaining to the selected Article, and use it in set-up processes that are common
     * to Article Activities of all Categories.
     */
    private void setUpArticleCore() {
        setAppBar();
        setArticleImage();
        bottomBar.highlightCategoryButton(this, Category.Person);
        populateConnections();
        populateSnippets();
    }

    /**
     * Set this Activity's toolbar and set the title to be the Article's name.
     */
    private void setAppBar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(articleName);
    }

    /**
     * Populate the Connections RecyclerView with cards for this Article's
     * {@link com.averi.worldscribe.Connection Connection}s.
     */
    private void populateConnections() {
        connectionsList.setLayoutManager(new LinearLayoutManager(this));
        connectionsList.setAdapter(new ConnectionsAdapter(this, worldName, category, articleName));
    }

    /**
     * Populate the Snippets RecyclerView with cards for this Article's Snippets.
     */
    private void populateSnippets() {
        snippetsList.setLayoutManager(new LinearLayoutManager(this));
        snippetsList.setAdapter(new SnippetsAdapter(this, worldName, category, articleName));
    }

    /**
     * Load and scale this Article's image, then display it.
     */
    private void setArticleImage() {
        Resources resources = getResources();
        Bitmap articleImage = ExternalReader.getArticleImage(this, worldName, category, articleName,
                (int) resources.getDimension(R.dimen.articleImageWidth),
                (int) resources.getDimension(R.dimen.articleImageHeight));
        imageView.setImageBitmap(articleImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.article_menu, menu);
        return true;
    }

}
