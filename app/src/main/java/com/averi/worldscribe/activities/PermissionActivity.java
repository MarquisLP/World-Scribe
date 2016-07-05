package com.averi.worldscribe.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;

import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.ExternalReader;

public class PermissionActivity extends AppCompatActivity {

    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private SharedPreferences preferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        preferences = getSharedPreferences("com.averi.worldscribe", MODE_PRIVATE);

        if ((!(deviceUsesRuntimePermissions())) || (writePermissionWasGranted())) {
            goToNextActivity();
        }
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
        if (preferences.getBoolean("permissionPromptIsEnabled", true)) {
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
                    goToNextActivity();
                } else if (userDisabledAskingForWritePermission()) {
                    recordDisablingOfWritePermissionPrompt();
                }
        }
    }

    private void enableWritePermissionPrompt() {
        preferences.edit().putBoolean("permissionPromptIsEnabled", true).apply();
    }

    private void recordDisablingOfWritePermissionPrompt() {
        preferences.edit().putBoolean("permissionPromptIsEnabled", false).apply();
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

    private void goToNextActivity() {
        String lastOpenedWorldName = preferences.getString("lastOpenedWorldName", "");
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
        Intent goToPeopleListIntent = new Intent(this, ArticleListActivity.class);
        goToPeopleListIntent.putExtra(AppPreferences.WORLD_NAME, lastOpenedWorldName);
        goToPeopleListIntent.putExtra("category", Category.Person);
        startActivity(goToPeopleListIntent);
        finish();
    }

    private void setLastOpenedWorldToNothing() {
        preferences.edit().putString("lastOpenedWorldName", "").apply();
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
