package com.averi.worldscribe.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.averi.worldscribe.ArticleTextField;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;
import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.ConnectionsAdapter;
import com.averi.worldscribe.adapters.SnippetsAdapter;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.AttributeGetter;
import com.averi.worldscribe.utilities.ExternalDeleter;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.TaskRunner;
import com.averi.worldscribe.utilities.tasks.DeleteArticleTask;
import com.averi.worldscribe.utilities.tasks.GetFilenamesInFolderTask;
import com.averi.worldscribe.utilities.tasks.RenameArticleTask;
import com.averi.worldscribe.viewmodels.ArticleViewModel;
import com.averi.worldscribe.views.ArticleSectionCollapser;
import com.averi.worldscribe.views.BottomBar;
import com.averi.worldscribe.views.BottomBarActivity;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.FileNotFoundException;
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
public abstract class ArticleActivity extends ReaderModeActivity implements BottomBarActivity {

    /**
     * The request code for selecting a new Article image.
     */
    public static final int RESULT_SELECT_IMAGE = 100;
    /**
     * The request code for creating a new Connection to this Article.
     */
    public static final int RESULT_NEW_CONNECTION = 200;

    /**
     * The amount of pixels that must be scrolled up/down for the BottomBar to slide in/out.
     */
    public static final int BOTTOM_BAR_SCROLL_THRESHOLD = 10;

    /**
     * The root layout of this Activity.
     */
    private ViewGroup rootLayout;
    /**
     * The display for the Article's image.
     */
    private ImageView imageView;
    /**
     * Loading circle shown while loading the Article's image.
     */
    private ProgressBar imageProgressCircle;
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
     * Loading circle shown while loading Connections.
     */
    private ProgressBar connectionsProgressCircle;
    /**
     * Clicking this begins Connection creation.
     */
    private Button addConnectionButton;
    /**
     * Loading circle shown while loading Snippets.
     */
    private ProgressBar snippetsProgressCircle;
    /**
     * Contains cards for all of the Article's Snippets.
     */
    private RecyclerView snippetsList;
    /**
     * Clicking this begins Snippet creation.
     */
    private Button addSnippetButton;

    private AlertDialog currentDialog;

    private ArticleViewModel viewModel;

    private final TaskRunner taskRunner = new TaskRunner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootLayout = getRootLayout();
        imageView = getImageView();
        imageProgressCircle = getImageProgressCircle();
        bottomBar = getBottomBar();
        Intent intent = getIntent();
        worldName = intent.getStringExtra(IntentFields.WORLD_NAME);
        category = (Category) intent.getSerializableExtra(IntentFields.CATEGORY);
        articleName = intent.getStringExtra(IntentFields.ARTICLE_NAME);
        connectionsList = getConnectionsRecycler();
        connectionsProgressCircle = getConnectionsProgressCircle();
        addConnectionButton = getAddConnectionButton();
        snippetsList = getSnippetsRecycler();
        snippetsProgressCircle = getSnippetsProgressCircle();
        addSnippetButton = getAddSnippetButton();
        textFields = getTextFields();

        viewModel = new ViewModelProvider(this).get(ArticleViewModel.class);

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

        setupConnectionsList();
        setupSnippetsList();
        setupErrorDialog();

        addSectionCollapsers();

        getNestedScrollView().setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                moveBottomBarOnScroll(scrollY - oldScrollY);
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
        if (currentDialog != null) {
            currentDialog.dismiss();
            currentDialog = null;
        }
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
            case R.id.settingsItem:
                ActivityUtilities.handleCommonAppBarItems(this, worldName, item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * @return The NestedScrollView containing this Article's content.
     */
    protected abstract NestedScrollView getNestedScrollView();

    /**
     * @return The Article ImageView for this Activity.
     */
    protected abstract ImageView getImageView();

    /**
     * @return The ProgressBar displayed while loading the Article's image.
     */
    protected abstract ProgressBar getImageProgressCircle();

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
     * @return The header for the 'General Info' section of this Article.
     */
    protected abstract TextView getGeneralInfoHeader();

    /**
     * @return The Layout containing the content of the General Info section.
     */
    protected abstract ViewGroup getGeneralInfoLayout();

    /**
     * @return The header for the 'Connections' section of this Article.
     */
    protected abstract TextView getConnectionsHeader();

    /**
     * @return The Layout containing the content of the Connections section.
     */
    protected abstract ViewGroup getConnectionsLayout();

    /**
     * @return The ProgressBar that displays while loading Connections.
     */
    protected abstract ProgressBar getConnectionsProgressCircle();

    /**
     * @return The header for the 'Snippets' section of this Article.
     */
    protected abstract TextView getSnippetsHeader();

    /**
     * @return The Layout containing the content of the General Info section.
     */
    protected abstract ViewGroup getSnippetsLayout();

    /**
     * @return The ProgressBar that displays while loading Snippets.
     */
    protected abstract ProgressBar getSnippetsProgressCircle();

    /**
     * Adds ArticleSectionCollapsers to all sections for this Article.
     * Subclasses should override this method and add collapsers for their own unique collections,
     * and then call super.
     */
    protected void addSectionCollapsers() {
        TextView generalInfoHeader = getGeneralInfoHeader();
        TextView connectionsHeader = getConnectionsHeader();
        TextView snippetsHeader = getSnippetsHeader();

        generalInfoHeader.setOnClickListener(new ArticleSectionCollapser(this, generalInfoHeader,
                getGeneralInfoLayout()));
        connectionsHeader.setOnClickListener(new ArticleSectionCollapser(this, connectionsHeader,
                getConnectionsLayout()));
        snippetsHeader.setOnClickListener(new ArticleSectionCollapser(this, snippetsHeader,
                getSnippetsLayout()));
    }

    /**
     * Slides the Bottom Bar in or out of the screen, based on whether the user scrolled up or down
     * a significant amount.
     * @param dy The amount scrolled, in pixels.
     */
    private void moveBottomBarOnScroll(int dy) {
        if ((dy > BOTTOM_BAR_SCROLL_THRESHOLD) && (bottomBar.getVisibility() == View.VISIBLE)) {
            bottomBar.slideOut();
        } else if ((dy < -BOTTOM_BAR_SCROLL_THRESHOLD) &&
                (bottomBar.getVisibility() == View.GONE)) {
            bottomBar.slideIn();
        }
    }

    /**
     * Load data pertaining to the selected Article, and use it in set-up processes that are common
     * to Article Activities of all Categories.
     */
    private void setUpArticleCore() {
        setAppBar();
        setArticleImage();
        bottomBar.focusCategoryButton(this, category);
        loadTextFieldsData();
        populateConnections();
        populateSnippets();
    }

    @Override
    protected void setAppBar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        assert myToolbar != null;
        setSupportActionBar(myToolbar);
        super.setAppBar();
        getSupportActionBar().setTitle(articleName);
    }

    private void setupSnippetsList() {
        SnippetsAdapter adapter = new SnippetsAdapter(this, worldName, category, articleName, nightModeIsEnabled());
        viewModel.getSnippetNames().observe(this, new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> newSnippetNames) {
                if (newSnippetNames == null) {
                    snippetsProgressCircle.setVisibility(View.VISIBLE);
                    snippetsList.setVisibility(View.GONE);
                }
                else {
                    adapter.updateList(newSnippetNames);
                    adapter.notifyDataSetChanged();
                    snippetsList.setVisibility(View.VISIBLE);
                    snippetsProgressCircle.setVisibility(View.GONE);
                }
            }
        });
        snippetsList.setAdapter(adapter);
        snippetsList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupErrorDialog() {
        final Context context = this;
        viewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newErrorMessage) {
                if (!(newErrorMessage.isEmpty())) {
                    ActivityUtilities.buildExceptionDialog(context, newErrorMessage,
                            dialogInterface -> viewModel.clearErrorMessage()
                    ).show();
                }
            }
        });
    }

    private void setupConnectionsList() {
        ConnectionsAdapter adapter = new ConnectionsAdapter(this, worldName, category, articleName);
        viewModel.getConnections().observe(this, new Observer<ArrayList<Connection>>() {
            @Override
            public void onChanged(ArrayList<Connection> newConnections) {
                if (newConnections == null) {
                    connectionsProgressCircle.setVisibility(View.VISIBLE);
                    connectionsList.setVisibility(View.GONE);
                }
                else {
                    adapter.updateList(newConnections);
                    adapter.notifyDataSetChanged();
                    connectionsList.setVisibility(View.VISIBLE);
                    connectionsProgressCircle.setVisibility(View.GONE);
                }
            }
        });
        connectionsList.setAdapter(adapter);
        connectionsList.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Populate the Connections RecyclerView with cards for this Article's
     * {@link com.averi.worldscribe.Connection Connection}s.
     */
    private void populateConnections() {
        viewModel.loadConnections(worldName, category, articleName);
    }

    /**
     * Populate the Snippets RecyclerView with cards for this Article's Snippets.
     */
    private void populateSnippets() {
        viewModel.loadSnippetNames(worldName, category, articleName);
    }

    /**
     * Load and scale this Article's image, then display it.
     * If the image is unset or could not be loaded, a default "unset" image is displayed.
     */
    private void setArticleImage() {
        viewModel.getImage().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap articleImage) {
                if (articleImage == null) {
                    imageProgressCircle.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                }
                else {
                    imageView.setImageBitmap(articleImage);
                    imageView.setVisibility(View.VISIBLE);
                    imageProgressCircle.setVisibility(View.GONE);
                }
            }
        });

        final Context context = this;
        viewModel.getImageColorFilterIsOn().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean imageColorFilterIsOn) {
                if (imageColorFilterIsOn) {
                    imageView.setColorFilter(AttributeGetter.getColorAttribute(context, R.attr.colorPrimary),
                            PorterDuff.Mode.SRC_ATOP);
                }
                else {
                    imageView.setColorFilter(null);
                }
            }
        });

        Resources resources = getResources();
        viewModel.loadImage(worldName, category, articleName,
                (int) resources.getDimension(R.dimen.articleImageWidth),
                (int) resources.getDimension(R.dimen.articleImageHeight));
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

        return super.onCreateOptionsMenu(menu);
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
     */
    protected void deleteArticle() {
        AlertDialog deletingProgressDialog = new AlertDialog.Builder(this, R.style.NormalDialog)
                .setCancelable(false)
                .setTitle(R.string.deletingArticleDialogTitle)
                .setView(new ProgressBar(this))
                .show();

        taskRunner.executeAsync(new DeleteArticleTask(worldName, category, articleName),
                (result) -> { finish(); },
                (exception) -> {
                    deletingProgressDialog.dismiss();
                    displayErrorDialog(exception);
                });
    }

    /**
     * Displays a dialog where the user can rename this Article.
     */
    private void showRenameArticleDialog() {
        AlertDialog.Builder builder = ActivityUtilities.getThemedDialogBuilder(this,
                nightModeIsEnabled());
        LayoutInflater inflater = this.getLayoutInflater();
        final View content = inflater.inflate(R.layout.rename_article_dialog, null);

        final EditText nameField = (EditText) content.findViewById(R.id.nameField);
        nameField.setText(articleName);

        final AlertDialog dialog = builder.setView(content)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) { }
                })
                .setNegativeButton(android.R.string.cancel, null).create();
        dialog.show();

        final ArticleActivity activity = this;

        // Handle onClick here to prevent the dialog from closing if the user enters
        // an invalid name.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String newName = nameField.getText().toString();

                        if (newName.equals(articleName)) {
                            dialog.dismiss();
                            return;
                        }

                        if (newName.isEmpty()) {
                            Toast.makeText(activity, R.string.emptyArticleNameError, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            ProgressBar renamingLoadingCircle = content.findViewById(R.id.renamingArticleProgressCircle);

                            renamingLoadingCircle.setVisibility(View.VISIBLE);
                            nameField.setVisibility(View.GONE);
                            dialog.setCancelable(false);
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                            String categoryFolderPath = worldName + "/" + category.pluralName(activity);
                                taskRunner.executeAsync(new GetFilenamesInFolderTask(categoryFolderPath, false),
                                        (existingArticleNames) -> {
                                            if (existingArticleNames.contains(newName)) {
                                                nameField.setVisibility(View.VISIBLE);
                                                renamingLoadingCircle.setVisibility(View.GONE);
                                                dialog.setCancelable(true);
                                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                                Toast.makeText(activity,
                                                        activity.getString(R.string.renameArticleToExistingError, category.name(), newName),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                            else if (ActivityUtilities.nameHasInvalidCharacters(newName)) {
                                                nameField.setVisibility(View.VISIBLE);
                                                renamingLoadingCircle.setVisibility(View.GONE);
                                                dialog.setCancelable(true);
                                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                                Toast.makeText(activity,
                                                        activity.getString(R.string.renameWithInvalidCharactersError, ActivityUtilities.getInvalidNameCharactersString()),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                            else {
                                                taskRunner.executeAsync(new RenameArticleTask(worldName, category, articleName, newName),
                                                        (result) -> {
                                                            dialog.dismiss();
                                                            activity.restartActivityWithNewArticleName(newName);
                                                        },
                                                        (Exception exception) -> {
                                                            dialog.dismiss();
                                                            activity.displayErrorDialog(exception);
                                                        });
                                            }
                                        },
                                        (Exception exception) -> {
                                            dialog.dismiss();
                                            activity.displayErrorDialog(exception);
                                        });
                        }
                    }
                });
    }

    private void restartActivityWithNewArticleName(String newArticleName) {
        Intent restartActivityIntent;
        switch (category) {
            case Person:
                restartActivityIntent = new Intent(this, PersonActivity.class);
                break;
            case Group:
                restartActivityIntent = new Intent(this, GroupActivity.class);
                break;
            case Place:
                restartActivityIntent = new Intent(this, PlaceActivity.class);
                break;
            case Item:
                restartActivityIntent = new Intent(this, ItemActivity.class);
                break;
            case Concept:
            default:
                restartActivityIntent = new Intent(this, ConceptActivity.class);
                break;
        }
        restartActivityIntent.putExtra(IntentFields.WORLD_NAME, worldName);
        restartActivityIntent.putExtra(IntentFields.CATEGORY, category);
        restartActivityIntent.putExtra(IntentFields.ARTICLE_NAME, newArticleName);

        finish();
        startActivity(restartActivityIntent);
    }

    private void displayErrorDialog(Exception exception) {
        Log.d("WorldScribe", Log.getStackTraceString(exception));
        ActivityUtilities.buildExceptionDialog(this, Log.getStackTraceString(exception),
                (dialogInterface -> {})).show();
    }

    public void respondToBottomBarButton(Category category) {
        goToArticleList(category);
    }

    /**
     * Opens the list of the Articles for a certain Category.
     * @param listCategory The Category whose Articles will be displayed.
     */
    private void goToArticleList(Category listCategory) {
        Intent intent = new Intent(this, ArticleListActivity.class);
        intent.putExtra(IntentFields.WORLD_NAME, worldName);
        intent.putExtra("category", listCategory);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.no_anim);
    }

    /**
     * Removes focus from all Views in this Activity.
     */
    public void removeFocus() {
        View current = getCurrentFocus();
        if (current != null) {
            current.clearFocus();
        }
    }

    /**
     * Displays an AlertDialog, and automatically dismisses it when the Activity is paused.
     * @param dialog The AlertDialog to show.
     */
    public void showUnpausableAlertDialog(AlertDialog dialog) {
        currentDialog = dialog;
        currentDialog.show();
    }

}
