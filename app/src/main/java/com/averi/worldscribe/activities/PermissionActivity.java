package com.averi.worldscribe.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.FileRetriever;
import com.balda.flipper.Root;
import com.balda.flipper.StorageManagerCompat;

import java.io.File;

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
                try {
                    Uri convertedFileRootUri = convertFileRootUriToCorrectFormat(
                            root.toRootDirectory(this).getUri());
                    preferences.edit().putString(AppPreferences.ROOT_DIRECTORY_URI,
                            convertedFileRootUri.toString())
                            .apply();
                    generateMissingAppDirectoryAndFiles();
                    goToNextActivity();
                } catch (Exception exception) {
                    ScrollView scrollView = new ScrollView(this);
                    new AlertDialog.Builder(this)
                        .setTitle("Troubleshooting")
                        .setView(scrollView)
                        .setMessage(exception.getMessage() + ". Stack trace: " + Log.getStackTraceString(exception))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                }
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
                        try {
                            Uri convertedFileRootUri = convertFileRootUriToCorrectFormat(
                                    root.toRootDirectory(this).getUri());
                            preferences.edit().putString(AppPreferences.ROOT_DIRECTORY_URI,
                                    convertedFileRootUri.toString())
                                    .apply();
                            enableWritePermissionPrompt();
                            generateMissingAppDirectoryAndFiles();
                            goToNextActivity();
                        } catch (Exception exception) {
                            ScrollView scrollView = new ScrollView(this);
                            new AlertDialog.Builder(this)
                                .setTitle("Troubleshooting")
                                .setView(scrollView)
                                .setMessage(exception.getMessage() + ". Stack trace: " + Log.getStackTraceString(exception))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                        }
                    }
                } else if (userDisabledAskingForWritePermission()) {
                    recordDisablingOfWritePermissionPrompt();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Runs after permission is granted to read/write on root external directory on SDK 29 and above
        try {
            if (requestCode == REQUEST_WRITE_ROOT_DIRECTORY && resultCode == RESULT_OK) {
                StorageManagerCompat storageManagerCompat = new StorageManagerCompat(this);

                // Fix for #43.
                // Since StorageManagerCompat stores roots in a HashSet, we need to delete DEF_MAIN_ROOT
                // or else the addRoot() call below will think we're adding a duplicate and thus do nothing.
                storageManagerCompat.deleteRoot(StorageManagerCompat.DEF_MAIN_ROOT);

                storageManagerCompat.addRoot(this, StorageManagerCompat.DEF_MAIN_ROOT, data);
                Root root = storageManagerCompat.getRoot(StorageManagerCompat.DEF_MAIN_ROOT);

                Uri convertedFileRootUri = convertFileRootUriToCorrectFormat(
                        root.toRootDirectory(this).getUri());
                preferences.edit().putString(AppPreferences.ROOT_DIRECTORY_URI,
                        convertedFileRootUri.toString())
                        .apply();

                enableWritePermissionPrompt();
                generateMissingAppDirectoryAndFiles();
                goToNextActivity();
            }
        }
        catch (Exception exception) {
            String rootUriString = preferences.getString(AppPreferences.ROOT_DIRECTORY_URI, null);
            if (rootUriString == null) {
                rootUriString = "NULL";
            }
            ScrollView scrollView = new ScrollView(this);
            new AlertDialog.Builder(this)
                .setTitle("Troubleshooting")
                .setView(scrollView)
                .setMessage(exception.getMessage() + ". Stack trace: " + Log.getStackTraceString(exception))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
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

    /**
     * Given a URI for a device's root external storage location, returns the URI formatted
     * as either "file:///" or "content:///" depending on the original format and the
     * Android version.
     *
     * <p>
     *     From what we have learned in issue #43, devices running Android 9 and below
     *     can use "file:///" URIs just fine. However, this is not the case for Android 10
     *     and above, which requires "content:///" URIs. Some brands, such as Samsung,
     *     still return a "file:///" URI in Android 10, so they need to be converted.
     * </p>
     * @param fileRootUri The original URI for the external storage root
     * @return The converted URI for the external storage root
     */
    private Uri convertFileRootUriToCorrectFormat(Uri fileRootUri) {
        if (fileRootUri.toString().startsWith("content")) {
            return fileRootUri;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return fileRootUri;
        } else { // If we have a "file:///" URI and Android version >= 10, convert to "content:///" URI.
            String fileRootPath = fileRootUri.getPath();
            if (fileRootPath == null) {
                throw new RuntimeException("Something went wrong. Please take a screenshot and email it to averistudios@gmail.com. Got null when retrieving file path for URI: " + fileRootUri.toString());
            } else {
                File fileRoot = new File(fileRootUri.getPath());
                return Uri.parse("content:/" + fileRoot.getAbsolutePath());
            }
        }
    }
}
