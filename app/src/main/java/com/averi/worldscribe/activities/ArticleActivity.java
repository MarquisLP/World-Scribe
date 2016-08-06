package com.averi.worldscribe.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.averi.worldscribe.ArticleTextField;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;
import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.ConnectionsAdapter;
import com.averi.worldscribe.adapters.SnippetsAdapter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.views.BottomBar;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;

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
     * The request code for selecting a new Article image.
     */
    public static final int RESULT_SELECT_IMAGE = 100;
    /**
     * The request code for creating a new Connection to this Article.
     */
    public static final int RESULT_NEW_CONNECTION = 200;

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
    /**
     * A list of all of the text fields for this Article.
     */
    private ArrayList<ArticleTextField> textFields;

    public String getWorldName() { return worldName; }

    public Category getCategory() { return category; }

    public String getArticleName() { return articleName; }

    /**
     * Contains cards for all of the Article's {@link com.averi.worldscribe.Connection}s.
     */
    private RecyclerView connectionsList;
    /**
     * Clicking this begins Connection creation.
     */
    private Button addConnectionButton;
    /**
     * Contains cards for all of the Article's Snippets.
     */
    private RecyclerView snippetsList;
    /**
     * Clicking this begins Snippet creation.
     */
    private Button addSnippetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceID());

        imageView = getImageView();
        bottomBar = getBottomBar();
        Intent intent = getIntent();
        worldName = intent.getStringExtra(IntentFields.WORLD_NAME);
        category = (Category) intent.getSerializableExtra(IntentFields.CATEGORY);
        articleName = intent.getStringExtra(IntentFields.ARTICLE_NAME);
        connectionsList = getConnectionsRecycler();
        addConnectionButton = getAddConnectionButton();
        snippetsList = getSnippetsRecycler();
        addSnippetButton = getAddSnippetButton();
        textFields = getTextFields();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNewArticleImage();
            }
        });
        addConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewConnection();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpArticleCore();
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveTextFieldsData();
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
     * @return The Button used for creating new Connections to this Article.
     */
    protected abstract Button getAddConnectionButton();

    /**
     * @return The RecyclerView for this Article's Snippets.
     */
    protected abstract RecyclerView getSnippetsRecycler();

    /**
     * @return An ArrayList containing all text fields for this Article.
     */
    protected abstract ArrayList<ArticleTextField> getTextFields();

    /**
     * @return The Button used for creating new Snippets owned by this Article.
     */
    protected abstract Button getAddSnippetButton();

    /**
     * Load data pertaining to the selected Article, and use it in set-up processes that are common
     * to Article Activities of all Categories.
     */
    private void setUpArticleCore() {
        setAppBar();
        setArticleImage();
        bottomBar.highlightCategoryButton(this, category);
        loadTextFieldsData();
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

    /**
     * Load data for all of the Article's text fields and display them.
     */
    private void loadTextFieldsData() {
        for (ArticleTextField textField : textFields) {
            textField.loadData();
        }
    }

    /**
     * Save all text field data for this Article.
     */
    private void saveTextFieldsData() {
        for (ArticleTextField textField : textFields) {
            textField.saveDataIfEdited();
        }
    }

    /**
     * Select a new image to use for this Article from the user's gallery.
     */
    private void selectNewArticleImage() {
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    CropImage.activity(imageUri)
                            .setAspectRatio(1, 1)
                            .setFixAspectRatio(true)
                            .setAllowRotation(true)
                            .start(this);
                }
                break;

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Uri resultUri = CropImage.getActivityResult(data).getUri();
                    Boolean imageWasSaved = ExternalWriter.saveArticleImage(this, worldName,
                            category, articleName, resultUri);
                    if (!(imageWasSaved)) {
                        Toast.makeText(this, getResources().getString(R.string.saveImageError),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getResources().getString(R.string.cropImageError),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case RESULT_NEW_CONNECTION:
                if (resultCode == RESULT_OK) {
                    Category otherArticleCategory = (Category) data.getSerializableExtra(
                            IntentFields.CATEGORY);
                    String otherArticleName = data.getStringExtra(IntentFields.ARTICLE_NAME);

                    Connection connection = new Connection();
                    connection.worldName = worldName;
                    connection.articleCategory = category;
                    connection.articleName = articleName;
                    connection.articleRelation = "";
                    connection.connectedArticleCategory = otherArticleCategory;
                    connection.connectedArticleName = otherArticleName;
                    connection.connectedArticleRelation = "";

                    Intent startConnectionEditIntent = new Intent(this,
                            EditConnectionActivity.class);
                    startConnectionEditIntent.putExtra(IntentFields.CONNECTION, connection);
                    startActivity(startConnectionEditIntent);
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.article_menu, menu);
        return true;
    }

    /**
     * Create a new {@link com.averi.worldscribe.Connection Connection} to this Article.
     */
    private void createNewConnection() {
        Intent selectConnectedArticleIntent = new Intent(this, SelectArticleActivity.class);
        selectConnectedArticleIntent.putExtra(IntentFields.WORLD_NAME, worldName);
        selectConnectedArticleIntent.putExtra(IntentFields.MAIN_ARTICLE_CATEGORY, category);
        selectConnectedArticleIntent.putExtra(IntentFields.MAIN_ARTICLE_NAME, articleName);
        ConnectionsAdapter connectionsAdapter = (ConnectionsAdapter) connectionsList.getAdapter();
        selectConnectedArticleIntent.putExtra(IntentFields.EXISTING_LINKS,
                connectionsAdapter.getLinkedArticleList());
        startActivityForResult(selectConnectedArticleIntent, RESULT_NEW_CONNECTION);
    }

}
