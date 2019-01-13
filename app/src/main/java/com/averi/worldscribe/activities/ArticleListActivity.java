package com.averi.worldscribe.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.averi.worldscribe.BuildConfig;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.GenericFileProvider;
import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.StringListAdapter;
import com.averi.worldscribe.adapters.StringListContext;
import com.averi.worldscribe.dropbox.DropboxActivity;
import com.averi.worldscribe.dropbox.UploadToDropboxTask;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.ErrorLoggingActivity;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.FileRetriever;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.LogErrorTask;
import com.averi.worldscribe.views.BottomBar;
import com.averi.worldscribe.views.BottomBarActivity;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ArticleListActivity extends ThemedActivity
        implements StringListContext, BottomBarActivity, DropboxActivity, ErrorLoggingActivity {

    public static final String DROPBOX_APP_KEY = "5pzb74tti855m61";
    private static final String DROPBOX_ERROR_LOG_MESSAGE = "An error occurred while trying to " +
            "upload a " +
            "file/folder with path '%s'.";
    private static final String FEEDBACK_SURVEY_URL = "https://goo.gl/forms/3VAhRuAajgBKmXyY2";

    private RecyclerView recyclerView;
    private String worldName;
    private Category category;
    private BottomBar bottomBar;
    private TextView textEmpty;
    private ArrayList<String> articleNames = new ArrayList<>();
    private UploadToDropboxTask uploadToDropboxTask;
    private boolean syncWorldToDropboxOnResume = false;
    private ProgressDialog dropboxProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        category = loadCategory(intent);
        worldName = loadWorldName(intent);
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        textEmpty = (TextView) findViewById(R.id.empty);

        setupRecyclerView();
        setAppBar(worldName);
        bottomBar.focusCategoryButton(this, category);
        showChangelogDialogIfOpeningNewVersion();
    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(true);

        StringListAdapter adapter = new StringListAdapter(this, articleNames);
        recyclerView.setAdapter(adapter);
    }

    private void setAppBar(String worldName) {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(worldName);
        }
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_article_list;
    }

    @Override
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.coordinatorLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        populateList(worldName, category);

        if (syncWorldToDropboxOnResume) {
            storeDropboxAccessToken();
            syncWorldToDropbox();
            syncWorldToDropboxOnResume = false;
        }
    }

    /**
     * Stores a user access token generated from Dropbox's servers into SharedPreferences,
     * if the user has authenticated their account.
     */
    private void storeDropboxAccessToken() {
        String accessToken = Auth.getOAuth2Token();

        if (accessToken != null) {
            SharedPreferences preferences = getSharedPreferences(
                    AppPreferences.PREFERENCES_FILE_NAME, MODE_PRIVATE);
            preferences.edit().putString(
                    AppPreferences.DROPBOX_ACCESS_TOKEN, accessToken).apply();
        }
    }

    /**
     * Displays a loading dialog that will stay on-screen while uploading occurs.
     */
    private void showDropboxProgressDialog() {
        final String title = this.getString(R.string.dropboxUploadProgressTitle);
        final String message = this.getString(R.string.dropboxUploadProgressMessage);
        final Context context = this;

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dropboxProgressDialog = ProgressDialog.show(context, title, message);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);

        ActivityUtilities.setUpSearchViewAppearance(this, menu, getString(R.string.searchHint));
        ActivityUtilities.setSearchViewFiltering(menu,
                (StringListAdapter) this.recyclerView.getAdapter());

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
            case R.id.renameWorldItem:
                showRenameWorldDialog();
                return true;
            case R.id.syncToDropboxItem:
                syncWorldToDropbox();
                return true;
            case R.id.viewChangelogItem:
                showChangelogDialog();
                return true;
            case R.id.feedbackItem:
                openFeedbackSurveyInBrowser();
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
     * Displays a dialog to allow the user to rename the currently-opened World.
     */
    private void showRenameWorldDialog() {
        AlertDialog.Builder builder = ActivityUtilities.getThemedDialogBuilder(this,
                nightModeIsEnabled());
        LayoutInflater inflater = this.getLayoutInflater();
        View content = inflater.inflate(R.layout.rename_world_dialog, null);

        final EditText nameField = (EditText) content.findViewById(R.id.nameField);
        nameField.setText(worldName);

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

                        if (newWorldNameIsValid(newName)) {
                            if (!(newName.equals(worldName))) {
                                renameWorld(newName);
                            }
                            dialog.dismiss();
                        }
                    }
                });
    }

    /**
     * Displays the Changelog Dialog if this is the first time the user has opened
     * the app since updating to a new version.
     */
    private void showChangelogDialogIfOpeningNewVersion() {
        SharedPreferences preferences = getSharedPreferences(AppPreferences.PREFERENCES_FILE_NAME,
                MODE_PRIVATE);
        int lastOpenedVersionCode = preferences.getInt(AppPreferences.LAST_OPENED_VERSION_CODE,
                0);
        final int currentVersionCode = BuildConfig.VERSION_CODE;

        if (lastOpenedVersionCode != currentVersionCode) {
            preferences.edit().putInt(AppPreferences.LAST_OPENED_VERSION_CODE,
                    currentVersionCode).apply();
            showChangelogDialog();
        }
    }

    /**
     * Displays the Changelog dialog with full details about the current app version.
     *
     * <p>
     *     Credit to Gabriele Mariotti for the
     *     <a href="https://github.com/gabrielemariotti/changeloglib">ChangeLog Library</a>.
     * </p>
     */
    private void showChangelogDialog() {
        AlertDialog.Builder builder = ActivityUtilities.getThemedDialogBuilder(this,
                nightModeIsEnabled());

        LayoutInflater inflater = this.getLayoutInflater();
        View content = inflater.inflate(R.layout.changelog_dialog, null);

        final AlertDialog dialog = builder.setView(content)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) { }
                }).create();
        dialog.show();
    }

    /**
     * <p>
     *     Checks whether a new name for this World is valid, i.e. non-empty and not in use by
     *     other World.
     * </p>
     * <p>
     *     If the name is invalid, an error message is displayed.
     * </p>
     * @param newName The new name requested for this World.
     * @return True if the new name is valid; false otherwise.
     */
    private boolean newWorldNameIsValid(String newName) {
        boolean newNameIsValid;

        if (newName.isEmpty()) {
            Toast.makeText(this, R.string.emptyWorldNameError, Toast.LENGTH_SHORT).show();
            newNameIsValid = false;
        } else if (newName.equals(worldName)) {   // Name was not changed.
            newNameIsValid = true;
        } else if (ExternalReader.worldAlreadyExists(newName)) {
            Toast.makeText(this,
                    getString(R.string.renameWorldToExistingError, newName),
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
    protected boolean renameWorld(String newName) {
        boolean renameWasSuccessful = false;

        if (ExternalWriter.renameWorldDirectory(worldName, newName)) {
            renameWasSuccessful = true;
            worldName = newName;
            AppPreferences.saveLastOpenedWorld(this, newName);
            setAppBar(newName);
        } else {
            Toast.makeText(this, R.string.renameWorldError, Toast.LENGTH_SHORT).show();
        }

        return renameWasSuccessful;
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
        bottomBar.focusCategoryButton(this, category);
        populateList(worldName, category);
    }

    /**
     * Syncs the current World's files to the user's Dropbox account.
     *
     * <p>
     *     If the user has not yet linked a Dropbox account, they will be asked to authenticate
     *     their account.
     * </p>
     *
     * <p>
     *     If an error occurs while syncing files, a message will be displayed.
     * </p>
     */
    private void syncWorldToDropbox() {
        if (!(AppPreferences.dropboxAccessTokenExists(this))) {
            Auth.startOAuth2Authentication(getApplicationContext(), DROPBOX_APP_KEY);
            // Since the Authentication Activity interrupts the flow of this method,
            // the actual syncing should occur when the user returns to this Activity after
            // authentication.
            syncWorldToDropboxOnResume = true;
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(this.getString(R.string.confirmBackupToDropboxTitle, worldName))
                    .setMessage(this.getString(R.string.confirmBackupToDropbox, worldName))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                        String accessToken = getDropboxAccessToken();
                        DbxClientV2 client = getDropboxClient(accessToken);
                        File worldDirectory = FileRetriever.getWorldDirectory(worldName);
                        new UploadToDropboxTask(client, worldDirectory, ArticleListActivity.this).execute();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    /**
     * @return The Dropbox account access token currently stored in SharedPreferences.
     */
    private String getDropboxAccessToken() {
        return getSharedPreferences(AppPreferences.PREFERENCES_FILE_NAME,
                MODE_PRIVATE).getString(AppPreferences.DROPBOX_ACCESS_TOKEN, "");
    }

    /**
     * Builds and returns a Dropbox Client object for a Dropbox account given that account's
     * access token.
     * @param ACCESS_TOKEN The token used in accessing the user's Dropbox account
     * @return A Dropbox Client object containing all of the given account's info
     */
    private DbxClientV2 getDropboxClient(final String ACCESS_TOKEN) {
        String clientIdentifier = getString(R.string.app_name) + "/" + getVersionName();
        DbxRequestConfig config = DbxRequestConfig.newBuilder(clientIdentifier).build();
        return new DbxClientV2(config, ACCESS_TOKEN);
    }

    /**
     * @return The name of the current app version.
     */
    private String getVersionName() {
        PackageInfo pInfo = null;

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
        }

        return pInfo.versionName;
    }

    public void onDropboxUploadStart() {
        showDropboxProgressDialog();
    }

    public void onDropboxNeedsAuthentication() {
        dropboxProgressDialog.dismiss();

        syncWorldToDropboxOnResume = true;
        Auth.startOAuth2Authentication(this, DROPBOX_APP_KEY);
    }

    public void onDropboxUploadSuccess() {
        dropboxProgressDialog.dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message;

        message = this.getString(R.string.dropboxUploadSuccess);
        builder.setPositiveButton(this.getString(R.string.dismissDropboxUploadOutcome), null)
                .setMessage(message)
                .show();
    }

    public void onDropboxUploadFailure(Exception exception, String lastFileBeingUploaded) {
        dropboxProgressDialog.dismiss();

        try {
            new LogErrorTask(this, String.format(DROPBOX_ERROR_LOG_MESSAGE,
                    lastFileBeingUploaded), exception).execute();
        } catch (Exception ex) {
            Log.e("WorldScribe",  "Exception when creating log file:\n" + exception.getMessage());
        }
    }

    public void onErrorLoggingCompletion(String errorMessage, final File errorLogFile) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String message = this.getString(R.string.dropboxUploadFailure);
        final String dismissButtonText = this.getString(R.string.dismissDropboxUploadOutcome);

        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout alertLayout = (LinearLayout) inflater.inflate(R.layout.layout_dropbox_error,
                null);
        final CheckBox chkSendLog = (CheckBox) alertLayout.findViewById(R.id.chkSendLog);
        builder.setView(alertLayout);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.setPositiveButton(dismissButtonText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (chkSendLog.isChecked()) {
                                sendEmail(errorLogFile);
                            }
                        }
                    })
                    .setMessage(message)
                    .show();
            }
        });
    }

    /**
     * This function was written by user6038288 on
     * <a href="https://stackoverflow.com/a/48007001">StackOverflow</a>.
     * @param file The file that will be attached to the email
     */
    private void sendEmail(File file) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"averistudios@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "WorldScribe " + file.getName());
        intent.putExtra(Intent.EXTRA_TEXT, "");
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = GenericFileProvider.getUriForFile(this, this.getApplicationContext()
                .getPackageName() + ".my.package.name.provider", file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        this.startActivity(Intent.createChooser(intent, "Send email..."));
    }

    private void openFeedbackSurveyInBrowser() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(FEEDBACK_SURVEY_URL)));
    }
}
