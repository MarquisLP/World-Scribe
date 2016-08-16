package com.averi.worldscribe.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.averi.worldscribe.ArticleTextField;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;
import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.ConnectionsAdapter;
import com.averi.worldscribe.adapters.SnippetsAdapter;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.ExternalDeleter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.views.BottomBar;
import com.theartofdev.edmodo.cropper.CropImage;

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
public abstract class ArticleActivity extends ThemedActivity {

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
        addSnippetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewSnippet();
            }
        });

        connectionsList.setLayoutManager(new LinearLayoutManager(this));
        snippetsList.setLayoutManager(new LinearLayoutManager(this));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.renameArticleItem:
                showRenameArticleDialog();
                return true;
            case R.id.deleteArticleItem:
                confirmArticleDeletion();
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
        assert myToolbar != null;
        myToolbar.setTitle(articleName);
        setSupportActionBar(myToolbar);
    }

    /**
     * Populate the Connections RecyclerView with cards for this Article's
     * {@link com.averi.worldscribe.Connection Connection}s.
     */
    private void populateConnections() {
        connectionsList.setAdapter(new ConnectionsAdapter(this, worldName, category, articleName));
    }

    /**
     * Populate the Snippets RecyclerView with cards for this Article's Snippets.
     */
    private void populateSnippets() {
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

    /**
     * Open CreateSnippetActivity to create a new Snippet belonging to this Article.
     */
    private void createNewSnippet() {
        Intent createSnippetIntent = new Intent(this, CreateSnippetActivity.class);
        createSnippetIntent.putExtra(IntentFields.WORLD_NAME, worldName);
        createSnippetIntent.putExtra(IntentFields.CATEGORY, category);
        createSnippetIntent.putExtra(IntentFields.ARTICLE_NAME, articleName);
        startActivity(createSnippetIntent);
    }

    /**
     * Deletes the Article upon user confirmation.
     */
    private void confirmArticleDeletion() {
        new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.confirmArticleDeletionTitle, articleName))
                .setMessage(this.getString(R.string.confirmArticleDeletion, articleName))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteArticle();
                        }
                    })
                .setNegativeButton(android.R.string.no, null).show();
    }

    /**
     * <p>
     * Deletes the Article and all links to it within other Articles.
     * </p>
     * <p>
     * If the Article couldn't be deleted, an error message is displayed.
     * </p>
     * <p>
     * Subclasses for Articles of Categories that have additional subdirectories must override
     * this method and empty out those subdirectories before calling super. Otherwise, the deletion
     * will fail.
     * </p>
     */
    protected void deleteArticle() {
        Toast errorToast =  Toast.makeText(this, getString(R.string.deleteArticleError),
                Toast.LENGTH_SHORT);

        if ((deleteAllConnections()) && (deleteAllSnippets())) {
             if (ExternalDeleter.deleteArticleDirectory(this, worldName, category, articleName)) {
                 finish();
             } else {
                 errorToast.show();
             }
        } else {
            errorToast.show();
        }
    }

    /**
     * Deletes all of the Connections linked to this Article.
     * @return True if all Connections were deleted successfully; false otherwise.
     */
    private boolean deleteAllConnections() {
        boolean connectionsWereDeleted = true;

        ArrayList<Connection> allConnections = (
                (ConnectionsAdapter) connectionsList.getAdapter()).getConnections();

        Connection connection;
        int index = 0;
        while ((index < allConnections.size()) && (connectionsWereDeleted)) {
            connection = allConnections.get(index);
            connectionsWereDeleted = ExternalDeleter.deleteConnection(this, connection);
            index++;
        }

        return connectionsWereDeleted;
    }

    /**
     * Deletes all of the Snippets possessed by this Article.
     * @return True if all Snippets were deleted successfully; false otherwise.
     */
    private boolean deleteAllSnippets() {
        boolean snippetsWereDeleted = true;

        ArrayList<String> allSnippets = (
                (SnippetsAdapter) snippetsList.getAdapter()).getSnippetNames();

        String snippetName;
        int index = 0;
        while ((index < allSnippets.size()) && (snippetsWereDeleted)) {
            snippetName = allSnippets.get(index);
            snippetsWereDeleted = ExternalDeleter.deleteSnippet(this, worldName, category,
                    articleName, snippetName);
            index++;
        }

        return snippetsWereDeleted;
    }

    /**
     * Displays a dialog where the user can rename this Article.
     */
    private void showRenameArticleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View content = inflater.inflate(R.layout.rename_article_dialog, null);

        final EditText nameField = (EditText) content.findViewById(R.id.nameField);
        nameField.setText(articleName);

        final AlertDialog dialog = builder.setView(content)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) { }
                })
                .setNegativeButton(android.R.string.cancel, null).create();
        dialog.show();

        // Handle onClick here to prevent the dialog from closing if the user enters
        // an invalid name.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String newName = nameField.getText().toString();

                        if (newArticleNameIsValid(newName)) {
                            if (!(newName.equals(articleName))) {
                                renameArticle(newName);
                            }
                            dialog.dismiss();
                        }
                    }
                });
    }

    /**
     * <p>
     *     Checks whether a new name for this Article is valid, i.e. non-empty and not in use by
     *     other Articles of the same Category.
     * </p>
     * <p>
     *     If the name is invalid, an error message is displayed.
     * </p>
     * @param newName The new name requested for this Article.
     * @return True if the new name is valid; false otherwise.
     */
    private boolean newArticleNameIsValid(String newName) {
        boolean newNameIsValid;

        if (newName.isEmpty()) {
            Toast.makeText(this, R.string.emptyArticleNameError, Toast.LENGTH_SHORT).show();
            newNameIsValid = false;
        } else if (newName.equals(articleName)) {   // Name was not changed.
            newNameIsValid = true;
        } else if (ExternalReader.articleExists(this, worldName, category, newName)) {
            Toast.makeText(this,
                    getString(R.string.renameArticleToExistingError, category.name(), newName),
                    Toast.LENGTH_SHORT).show();
            newNameIsValid = false;
        } else {
            newNameIsValid = true;
        }

        return newNameIsValid;
    }

    /**
     * <p>
     *     Renames the Article; all references to it from other Articles are also updated to reflect
     *     the new name.
     * </p>
     * <p>
     *     If the Article couldn't be renamed, an error message is displayed.
     * </p>
     * <p>
     *     Subclasses for Articles of Categories that have additional types of references (e.g.
     *     Residences) must override this method and update the Article's name within those
     *     references as well. Otherwise, those references on other Articles' pages will break.
     * </p>
     * @param newName The new name for the Article.
     * @return True if the Article was renamed successfully; false otherwise.
     */
    protected boolean renameArticle(String newName) {
        boolean renameWasSuccessful = false;
        Toast errorToast = Toast.makeText(this, R.string.renameArticleError, Toast.LENGTH_SHORT);

        if (renameArticleInConnections(newName)) {
            if (ExternalWriter.renameArticleDirectory(this, worldName, category,
                    articleName, newName)) {
                renameWasSuccessful = true;
                articleName = newName;
                setAppBar();
                populateSnippets();
            } else {
                errorToast.show();
            }
        } else {
            errorToast.show();
        }

        return renameWasSuccessful;
    }

    /**
     * <p>
     *     Updates all of this Article's Connections to reflect a new Article name.
     * </p>
     * <p>
     *     If one or more Connections failed to be updated, an error message is displayed.
     * </p>
     * @param newName The new name for this Article.
     * @return True if all Connections updated successfully; false otherwise.
     */
    private boolean renameArticleInConnections(String newName) {
        boolean connectionsWereUpdated = true;
        ConnectionsAdapter adapter = (ConnectionsAdapter) connectionsList.getAdapter();
        ArrayList<Connection> connections = adapter.getConnections();

        int index = 0;
        Connection currentConnection;
        while ((index < connections.size()) && (connectionsWereUpdated)) {
            currentConnection = connections.get(index);

            if (ExternalWriter.renameArticleInConnection(this, currentConnection, newName)) {
                currentConnection.articleName = newName;
            } else {
                connectionsWereUpdated = false;
            }

            index++;
        }

        return connectionsWereUpdated;
    }

}
