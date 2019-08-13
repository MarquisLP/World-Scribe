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
import android.view.View;
import android.view.ViewGroup;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;

public class PermissionActivity extends ThemedActivity {

    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private SharedPreferences preferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("com.averi.worldscribe", MODE_PRIVATE);

        if ((!(deviceUsesRuntimePermissions())) || (writePermissionWasGranted())) {
            generateMissingAppDirectoryAndFiles();
            goToNextActivity();
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
        if (preferences.getBoolean(AppPreferences.WRITE_PERMISSION_PROMPT_IS_ENABLED, true)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            goToAppSettings();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableWritePermissionPrompt();
                    generateMissingAppDirectoryAndFiles();
                    goToNextActivity();
                } else if (userDisabledAskingForWritePermission()) {
                    recordDisablingOfWritePermissionPrompt();
                }
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
        if (!(ExternalReader.appDirectoryExists())) {
            ExternalWriter.createAppDirectory();
        }

        if (!(ExternalReader.noMediaFileExists())) {
            ExternalWriter.createNoMediaFile();
        }
    }

    private void goToNextActivity() {
        String lastOpenedWorldName = preferences.getString(AppPreferences.LAST_OPENED_WORLD, "");
        if ((!(lastOpenedWorldName.isEmpty())) && (ExternalReader.worldAlreadyExists(lastOpenedWorldName))) {
            goToLastOpenedWorld(lastOpenedWorldName);

        } else {
            setLastOpenedWorldToNothing();

            if (ExternalReader.worldListIsEmpty()) {
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
