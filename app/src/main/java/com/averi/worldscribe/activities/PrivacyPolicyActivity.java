package com.averi.worldscribe.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.AppPreferences;

public class PrivacyPolicyActivity extends ThemedActivity {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("com.averi.worldscribe", MODE_PRIVATE);

        AlertDialog.Builder builder = ActivityUtilities.getThemedDialogBuilder(this,
                nightModeIsEnabled());
        LayoutInflater inflater = this.getLayoutInflater();
        View content = inflater.inflate(R.layout.announcements_dialog, null);
        final AlertDialog dialog = builder.setView(content)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (preferences.getBoolean(AppPreferences.HAS_AGREED_TO_PRIVACY_POLICY, false)) {
                            goToPermissionsActivity();
                        }
                    }
                }).create();
        dialog.show();
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_privacy_policy;
    }

    @Override
    protected ViewGroup getRootLayout() {
        return findViewById(R.id.linearScreen);
    }

    public void handleTapAgreeButton(View agreeButton) {
        preferences.edit()
                .putBoolean(AppPreferences.HAS_AGREED_TO_PRIVACY_POLICY, true)
                .apply();
        goToPermissionsActivity();
    }

    public void handleTapDisagreeButton(View disagreeButton) {
        finish();
    }

    private void goToPermissionsActivity() {
        Intent goToPermissionsIntent = new Intent(this, PermissionActivity.class);
        startActivity(goToPermissionsIntent);
        finish();
    }

}
