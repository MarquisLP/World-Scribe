package com.averi.worldscribe.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.FileRetriever;
import com.balda.flipper.Root;
import com.balda.flipper.StorageManagerCompat;

public class PermissionActivity extends ThemedActivity {

    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public static final int REQUEST_WRITE_ROOT_DIRECTORY = 2;

    private TextView textWelcome;
    private TextView textExplanation;

    private SharedPreferences preferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("com.averi.worldscribe", MODE_PRIVATE);

        textWelcome = findViewById(R.id.textWelcome);
        textExplanation = findViewById(R.id.textExplanation);

        if ((!(deviceUsesRuntimePermissions())) || (writePermissionWasGranted())) {
            StorageManagerCompat storageManagerCompat = new StorageManagerCompat(this);
            Root root = storageManagerCompat.getRoot(StorageManagerCompat.DEF_MAIN_ROOT);
            if ((root != null) && (root.isAccessGranted(this))) {
                preferences.edit().putString(AppPreferences.ROOT_DIRECTORY_URI,
                        root.toRootDirectory(this).getUri().toString())
                    .apply();
                generateMissingAppDirectoryAndFiles();
                goToNextActivity();
            }
            else {
                textWelcome.setText(R.string.selectRootDirectoryTitle);
                textExplanation.setText(R.string.selectRootDirectoryExplanation);
            }
        }
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_permission;
    }

    @Override
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.linearScreen);
    }

    private boolean deviceUsesRuntimePermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private boolean writePermissionWasGranted() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (permissionCheck == PackageManager.PERMISSION_GRANTED);
    }

    public void askForWritePermission(View view) {
        StorageManagerCompat storageManagerCompat = new StorageManagerCompat(this);
        Root root = storageManagerCompat.getRoot(StorageManagerCompat.DEF_MAIN_ROOT);
        if ((writePermissionWasGranted()) && ((root == null) || (!root.isAccessGranted(this)))) {
            Intent getExternalFolderAccessIntent = storageManagerCompat.requireExternalAccess(this);
            startActivityForResult(getExternalFolderAccessIntent, REQUEST_WRITE_ROOT_DIRECTORY);
        }
        else {
            if (preferences.getBoolean(AppPreferences.WRITE_PERMISSION_PROMPT_IS_ENABLED, true)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                goToAppSettings();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // On Android SDK 29 and above, we need to ask for permission to
                    // access the root of the user's external storage.
                    StorageManagerCompat storageManagerCompat = new StorageManagerCompat(this);
                    Root root = storageManagerCompat.getRoot(StorageManagerCompat.DEF_MAIN_ROOT);
                    if ((root == null) || (!root.isAccessGranted(this))) {
                        textWelcome.setText(R.string.selectRootDirectoryTitle);
                        textExplanation.setText(R.string.selectRootDirectoryExplanation);
                    }
                    else {
                        preferences.edit().putString(AppPreferences.ROOT_DIRECTORY_URI,
                                root.toRootDirectory(this).getUri().toString())
                            .apply();
                        enableWritePermissionPrompt();
                        generateMissingAppDirectoryAndFiles();
                        goToNextActivity();
                    }
                } else if (userDisabledAskingForWritePermission()) {
                    recordDisablingOfWritePermissionPrompt();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Runs after permission is granted to read/write on root external directory on SDK 29 and above
        if (requestCode == REQUEST_WRITE_ROOT_DIRECTORY && resultCode == RESULT_OK) {
            StorageManagerCompat storageManagerCompat = new StorageManagerCompat(this);
            storageManagerCompat.addRoot(this, StorageManagerCompat.DEF_MAIN_ROOT, data);
            Root root = storageManagerCompat.getRoot(StorageManagerCompat.DEF_MAIN_ROOT);
            preferences.edit().putString(AppPreferences.ROOT_DIRECTORY_URI,
                    root.toRootDirectory(this).getUri().toString())
                .apply();
            enableWritePermissionPrompt();
            generateMissingAppDirectoryAndFiles();
            goToNextActivity();
        }
    }

    private void enableWritePermissionPrompt() {
        preferences.edit().putBoolean(AppPreferences.WRITE_PERMISSION_PROMPT_IS_ENABLED,
                true).apply();
    }

    private void recordDisablingOfWritePermissionPrompt() {
        preferences.edit().putBoolean(AppPreferences.WRITE_PERMISSION_PROMPT_IS_ENABLED,
                false).apply();
    }

    private boolean userDisabledAskingForWritePermission() {
        return (!(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)));
    }

    private void goToAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * Generates the app directory and any necessary configuration files if they
     * are missing from the user's external storage.
     */
    private void generateMissingAppDirectoryAndFiles() {
        if (!(ExternalReader.appDirectoryExists(this))) {
            DocumentFile appDirectory = ExternalWriter.createAppDirectory(this);
            if  (appDirectory == null) {
                String rootUriString = preferences.getString(AppPreferences.ROOT_DIRECTORY_URI, null);
                throw new RuntimeException("Failed to create app directory. Device file root URI: " + rootUriString);
            }
        }

        if (!(ExternalReader.noMediaFileExists(this))) {
            ExternalWriter.createNoMediaFile(this);
        }
    }

    private void goToNextActivity() {
        String lastOpenedWorldName = preferences.getString(AppPreferences.LAST_OPENED_WORLD, "");
        if ((!(lastOpenedWorldName.isEmpty())) && (ExternalReader.worldAlreadyExists(this, lastOpenedWorldName))) {
            goToLastOpenedWorld(lastOpenedWorldName);

        } else {
            setLastOpenedWorldToNothing();

            if (ExternalReader.worldListIsEmpty(this)) {
                goToWorldCreation();
            } else {
                goToCreateOrLoadWorld();
            }
        }
    }

    private void goToLastOpenedWorld(String lastOpenedWorldName) {
        ActivityUtilities.goToWorld(this, lastOpenedWorldName);
        finish();
    }

    private void setLastOpenedWorldToNothing() {
        preferences.edit().putString(AppPreferences.LAST_OPENED_WORLD, "").apply();
    }

    private void goToWorldCreation() {
        Intent goToWorldCreationIntent = new Intent(this, CreateWorldActivity.class);
        startActivity(goToWorldCreationIntent);
        finish();
    }

    private void goToCreateOrLoadWorld() {
        Intent goToCreateOrLoadWorldIntent = new Intent(this, CreateOrLoadWorldActivity.class);
        startActivity(goToCreateOrLoadWorldIntent);
        finish();
    }

}
