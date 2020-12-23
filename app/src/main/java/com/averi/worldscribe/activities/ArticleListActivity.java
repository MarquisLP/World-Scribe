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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.averi.worldscribe.BuildConfig;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.StringListAdapter;
import com.averi.worldscribe.adapters.StringListContext;
import clouds.CloudActivity;
import clouds.dropbox.UploadToDropboxTask;
import clouds.nextcloud.UploadToNextcloudTask;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.ErrorLoggingActivity;
import com.averi.worldscribe.utilities.FileRetriever;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.LogErrorTask;
import com.averi.worldscribe.utilities.TaskRunner;
import com.averi.worldscribe.utilities.tasks.GetFilenamesInFolderTask;
import com.averi.worldscribe.utilities.tasks.RenameWorldTask;
import com.averi.worldscribe.viewmodels.ArticleListViewModel;
import com.averi.worldscribe.views.BottomBar;
import com.averi.worldscribe.views.BottomBarActivity;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.oauth.DbxOAuthException;
import com.dropbox.core.oauth.DbxRefreshResult;
import com.dropbox.core.v2.DbxClientV2;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArticleListActivity extends ThemedActivity
        implements StringListContext, BottomBarActivity, CloudActivity, ErrorLoggingActivity {

    public static final String DROPBOX_APP_KEY = "5pzb74tti855m61";
    public static final String DROPBOX_CLIENT_IDENTIFIER = "WorldScribe/1.7.0";
    private static final String DROPBOX_ERROR_LOG_MESSAGE = "An error occurred while trying to " +
            "upload a " +
            "file/folder with path '%s'.";
    private static final String FEEDBACK_SURVEY_URL = "https://goo.gl/forms/3VAhRuAajgBKmXyY2";
    private static final String DEVELOPER_WEBSITE_URL = "https://averistudios.com";
    private static final int LOGIN_REQUEST = 1;

    private ArticleListViewModel viewModel;

    private ProgressBar progressCircle;
    private RecyclerView recyclerView;
    private String worldName;
    private Category category;
    private BottomBar bottomBar;
    private TextView textEmpty;
    private ArrayList<String> articleNames = new ArrayList<>();
    private UploadToDropboxTask uploadToDropboxTask;
    private boolean syncWorldToDropboxOnResume = false;
    private ProgressDialog dropboxProgressDialog;

    private final TaskRunner taskRunner = new TaskRunner();

    //Used to change the title of the messageboxes.
    private CloudType cloudType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        category = loadCategory(intent);
        worldName = loadWorldName(intent);
        progressCircle = (ProgressBar) findViewById(R.id.progressCircle);
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        textEmpty = (TextView) findViewById(R.id.empty);

        viewModel = new ViewModelProvider(this).get(ArticleListViewModel.class);

        setupLoadingAnimation();
        setupErrorDialog();
        setupRecyclerView();
        setAppBar(worldName);
        bottomBar.focusCategoryButton(this, category);
        showAnnouncementsAndChangelogIfOpeningNewVersion();

    }

    private void setupLoadingAnimation() {
        viewModel.isLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading) {
                    textEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    progressCircle.setVisibility(View.VISIBLE);
                }
                else {
                    progressCircle.setVisibility(View.GONE);
                }
            }
        });
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

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(true);

        StringListAdapter adapter = new StringListAdapter(this, articleNames);
        viewModel.getArticleNames().observe(this, new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> newArticleNames) {
                adapter.updateList(newArticleNames);
                adapter.notifyDataSetChanged();

                if (newArticleNames.isEmpty()) {
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
        });
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

        viewModel.loadArticleNamesFromStorage(worldName, category);

        if (syncWorldToDropboxOnResume) {
            storeDropboxCredentials();
            syncWorldToDropbox();
            syncWorldToDropboxOnResume = false;
        }
    }

    /**
     * Stores a Dropbox short-lived access token, refresh token, and expiration date after
     * a successful Dropbox authentication flow.
     */
    private void storeDropboxCredentials() {
        DbxCredential dbxCredential = Auth.getDbxCredential();
        String accessToken = dbxCredential.getAccessToken();
        String refreshToken = dbxCredential.getRefreshToken();
        Long expiresAt = dbxCredential.getExpiresAt();

        SharedPreferences preferences = getSharedPreferences(
                AppPreferences.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        if (accessToken != null) {
            preferences.edit().putString(
                    AppPreferences.DROPBOX_ACCESS_TOKEN, accessToken).apply();
        }
        if (refreshToken != null) {
            preferences.edit().putString(
                    AppPreferences.DROPBOX_REFRESH_TOKEN, refreshToken).apply();
        }
        if (expiresAt != null) {
            preferences.edit().putLong(
                    AppPreferences.DROPBOX_EXPIRES_AT, expiresAt).apply();
        }
    }

    /**
     * Displays a loading dialog that will stay on-screen while uploading occurs.
     */
    private void showDropboxProgressDialog() {
        final String title = this.getString(R.string.cloudUploadProgressTitle, cloudType.name());
        final String message = this.getString(R.string.cloudUploadProgressMessage);
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
        viewModel.loadArticleNamesFromStorage(worldName, category);
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
            case R.id.syncToNextcloudItem:
                syncWorldToNextcloud();
                return true;
            case R.id.viewAnnouncementsItem:
                showAnnouncementsDialog(false);
                return true;
            case R.id.viewChangelogItem:
                showChangelogDialog();
                return true;
            case R.id.feedbackItem:
                openFeedbackSurveyInBrowser();
                return true;
            case R.id.viewDeveloperWebsiteItem:
                openDeveloperWebsiteInBrowser();
                return true;
            case R.id.createWorldItem:
            case R.id.loadWorldItem:
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

        final ArticleListActivity activity = this;

        // Handle onClick here to prevent the dialog from closing if the user enters
        // an invalid name.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String newName = nameField.getText().toString();

                        if (newName.equals(worldName)) {
                            dialog.dismiss();
                            return;
                        }

                        if (newName.isEmpty()) {
                            Toast.makeText(activity, R.string.emptyWorldNameError, Toast.LENGTH_SHORT).show();
                        }
                        else {

                            ProgressBar renamingWorldProgressCircle = dialog.findViewById(R.id.renamingWorldProgressCircle);

                            dialog.setCancelable(false);
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                            renamingWorldProgressCircle.setVisibility(View.VISIBLE);
                            nameField.setVisibility(View.GONE);

                            taskRunner.executeAsync(new GetFilenamesInFolderTask("/", false),
                                    (existingWorldNames) -> {
                                        if (existingWorldNames.contains(newName)) {
                                            dialog.setCancelable(true);
                                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                                            renamingWorldProgressCircle.setVisibility(View.GONE);
                                            nameField.setVisibility(View.VISIBLE);
                                            Toast.makeText(activity,
                                                    activity.getString(R.string.renameWorldToExistingError, newName),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            taskRunner.executeAsync(new RenameWorldTask(worldName, newName),
                                                    (result) -> {
                                                        worldName = newName;
                                                        AppPreferences.saveLastOpenedWorld(activity, newName);
                                                        setAppBar(newName);
                                                        dialog.dismiss();
                                                    },
                                                    (exception) -> {
                                                        dialog.setCancelable(true);
                                                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                                                        renamingWorldProgressCircle.setVisibility(View.GONE);
                                                        nameField.setVisibility(View.VISIBLE);
                                                        activity.displayErrorDialog(exception);
                                                    });
                                        }
                                    },
                                    (exception) -> {
                                        dialog.setCancelable(true);
                                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                                        renamingWorldProgressCircle.setVisibility(View.GONE);
                                        nameField.setVisibility(View.VISIBLE);
                                        activity.displayErrorDialog(exception);
                                    }
                            );
                        }
                    }
                });
    }

    private void displayErrorDialog(Exception exception) {
        ActivityUtilities.buildExceptionDialog(this,
                Log.getStackTraceString(exception), (dialogInterface) -> {}).show();
    }

    /**
     * Displays all existing announcements in a dialog.
     *
     * <p>
     *     Credit to Gabriele Mariotti for the
     *     <a href="https://github.com/gabrielemariotti/changeloglib">ChangeLog Library</a>.
     * </p>
     */
    private void showAnnouncementsDialog(boolean showChangelogAfterwards) {
        AlertDialog.Builder builder = ActivityUtilities.getThemedDialogBuilder(this,
                nightModeIsEnabled());

        LayoutInflater inflater = this.getLayoutInflater();
        View content = inflater.inflate(R.layout.announcements_dialog, null);

        if (showChangelogAfterwards) {
            final AlertDialog dialog = builder.setView(content)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            showChangelogDialog();
                        }
                    }).create();
            dialog.show();
        }
        else {
            final AlertDialog dialog = builder.setView(content)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) { }
                    }).create();
            dialog.show();
        }
    }

    /**
     * Displays the Changelog Dialog if this is the first time the user has opened
     * the app since updating to a new version.
     */
    private void showAnnouncementsAndChangelogIfOpeningNewVersion() {
        SharedPreferences preferences = getSharedPreferences(AppPreferences.PREFERENCES_FILE_NAME,
                MODE_PRIVATE);
        int lastOpenedVersionCode = preferences.getInt(AppPreferences.LAST_OPENED_VERSION_CODE,
                0);
        final int currentVersionCode = BuildConfig.VERSION_CODE;

        if (lastOpenedVersionCode != currentVersionCode) {
            preferences.edit().putInt(AppPreferences.LAST_OPENED_VERSION_CODE,
                    currentVersionCode).apply();
            showAnnouncementsDialog(true);
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
        if (!(AppPreferences.dropboxAccessTokenAndRefreshTokenExist(this))) {
            List<String> dropboxScopes = Arrays.asList("account_info.read", "files.content.read", "files.content.write");
            DbxRequestConfig dropboxRequestConfig = new DbxRequestConfig(DROPBOX_CLIENT_IDENTIFIER);
            Auth.startOAuth2PKCE(getApplicationContext(), DROPBOX_APP_KEY, dropboxRequestConfig, dropboxScopes);
            // Since the Authentication Activity interrupts the flow of this method,
            // the actual syncing should occur when the user returns to this Activity after
            // authentication.
            syncWorldToDropboxOnResume = true;
        } else {
            cloudType = CloudType.Dropbox;

            final Context context = this;
            new AlertDialog.Builder(this)
                    .setTitle(this.getString(R.string.confirmBackupToCloudTitle, worldName))
                    .setMessage(this.getString(R.string.confirmBackupToCloud, worldName, cloudType.name()))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            DbxClientV2 client = getDropboxClient();
                            DocumentFile worldDirectory = FileRetriever.getWorldDirectory(context, worldName, false);
                            new UploadToDropboxTask(client, worldDirectory, ArticleListActivity.this, context).execute();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    private void syncWorldToNextcloud() {
        Intent intent = new Intent(this, NextcloudLoginActivity.class);
        intent.putExtra(NextcloudLoginActivity.SERVER, AppPreferences.getLastNextcloudServer(this));
        intent.putExtra(NextcloudLoginActivity.USERNAME, AppPreferences.getLastNextcloudUser(this));
        startActivityForResult(intent, LOGIN_REQUEST);

        //new UploadToNextcloudTask(Uri.parse("http://10.0.2.2/nextcloud"), this, FileRetriever.getWorldDirectory(worldName), "admin", "123").execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_REQUEST) {
            if(resultCode == RESULT_OK && data != null) {
                cloudType = CloudType.Nextcloud;

                final Context context = this;
                new AlertDialog.Builder(this)
                        .setTitle(this.getString(R.string.confirmBackupToCloudTitle, worldName))
                        .setMessage(this.getString(R.string.confirmBackupToCloud, worldName, cloudType.name()))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                                String Server = data.getStringExtra(NextcloudLoginActivity.SERVER);

                                if (!Server.contains("http://") && !Server.contains("https://"))
                                    Server = "http://" + Server;

                                //Saves the server and the username.
                                //Why is there no password? Because it's to unsave to save the password.
                                //But what is about the android AccountManager? Basically the AM stores the password also in cleartext, which can be
                                //read by the root and I don't have the knowlege about a good encryption for passwords.
                                //Why you don't use OAuth2 of nextcloud= Because the library from nextcloud, android-library, doesn't support it.
                                AppPreferences.saveLastNextcloudServer(ArticleListActivity.this, Server);
                                AppPreferences.saveLastNextcloudUser(ArticleListActivity.this, data.getStringExtra(NextcloudLoginActivity.USERNAME));

                                OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(Uri.parse(Server), ArticleListActivity.this, true);
                                client.setCredentials(
                                        OwnCloudCredentialsFactory.newBasicCredentials(
                                                data.getStringExtra(NextcloudLoginActivity.USERNAME),
                                                data.getStringExtra(NextcloudLoginActivity.PASSWORD)
                                        ));

                                DocumentFile worldDirectory = FileRetriever.getWorldDirectory(context, worldName, false);
                                new UploadToNextcloudTask(client, ArticleListActivity.this, worldDirectory, context).execute();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        }
    }

    /**
     * @return The Dropbox account access token currently stored in SharedPreferences.
     */
    private DbxCredential getDropboxCredential() {
        String accessToken = getSharedPreferences(AppPreferences.PREFERENCES_FILE_NAME,
                MODE_PRIVATE).getString(AppPreferences.DROPBOX_ACCESS_TOKEN, "");
        String refreshToken = getSharedPreferences(AppPreferences.PREFERENCES_FILE_NAME,
                MODE_PRIVATE).getString(AppPreferences.DROPBOX_REFRESH_TOKEN, "");
        Long expiresAt = getSharedPreferences(AppPreferences.PREFERENCES_FILE_NAME,
                MODE_PRIVATE).getLong(AppPreferences.DROPBOX_EXPIRES_AT, 0);
        return new DbxCredential(accessToken, expiresAt, refreshToken, DROPBOX_APP_KEY);
    }

    /**
     * Builds and returns a Dropbox Client object for a Dropbox account given that account's
     * access token.
     * @return A Dropbox Client object containing all of the given account's info
     */
    private DbxClientV2 getDropboxClient() {
        String clientIdentifier = getString(R.string.app_name) + "/" + getVersionName();
        DbxRequestConfig config = DbxRequestConfig.newBuilder(clientIdentifier).build();
        return new DbxClientV2(config, getDropboxCredential());
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

    public void onCloudUploadStart() {
        showDropboxProgressDialog();
    }

    public void onDropboxNeedsAuthentication() {
        dropboxProgressDialog.dismiss();

        syncWorldToDropboxOnResume = true;
        List<String> dropboxScopes = Arrays.asList("account_info.read", "files.content.read", "files.content.write");
        DbxRequestConfig dropboxRequestConfig = new DbxRequestConfig(DROPBOX_CLIENT_IDENTIFIER);
        Auth.startOAuth2PKCE(getApplicationContext(), DROPBOX_APP_KEY, dropboxRequestConfig, dropboxScopes);
    }

    public void onCloudUploadSuccess() {
        dropboxProgressDialog.dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message;

        message = this.getString(R.string.cloudUploadSuccess);
        builder.setPositiveButton(this.getString(R.string.dismissCloudUploadOutcome), null)
                .setMessage(message)
                .show();
    }

    public void onCloudUploadFailure(Exception exception, String lastFileBeingUploaded) {
        dropboxProgressDialog.dismiss();

        try {
            new LogErrorTask(this, String.format(DROPBOX_ERROR_LOG_MESSAGE,
                    lastFileBeingUploaded), this, exception).execute();
        } catch (Exception ex) {
            Log.e("WorldScribe",  "Exception when creating log file:\n" + exception.getMessage());
        }

        displayErrorDialog(exception);
    }

    public void onErrorLoggingCompletion(String errorMessage, final DocumentFile errorLogFile) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String message = this.getString(R.string.cloudUploadFailure, cloudType.name());
        final String dismissButtonText = this.getString(R.string.dismissCloudUploadOutcome);

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
    private void sendEmail(DocumentFile file) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"averistudios@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "WorldScribe " + file.getName());
        intent.putExtra(Intent.EXTRA_TEXT, "");
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = file.getUri();
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        this.startActivity(Intent.createChooser(intent, "Send email..."));
    }

    private void openFeedbackSurveyInBrowser() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(FEEDBACK_SURVEY_URL)));
    }

    private void openDeveloperWebsiteInBrowser() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DEVELOPER_WEBSITE_URL)));
    }
}
