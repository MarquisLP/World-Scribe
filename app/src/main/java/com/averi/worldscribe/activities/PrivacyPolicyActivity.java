package com.averi.worldscribe.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.AppPreferences;

public class PrivacyPolicyActivity extends ThemedActivity {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("com.averi.worldscribe", MODE_PRIVATE);

        if (preferences.getBoolean(AppPreferences.HAS_AGREED_TO_PRIVACY_POLICY, false)) {
            goToPermissionsActivity();
        }
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
