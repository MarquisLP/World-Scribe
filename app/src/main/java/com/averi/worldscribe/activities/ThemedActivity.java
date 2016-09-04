package com.averi.worldscribe.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.AppPreferences;

/**
 * Created by mark on 15/08/16.
 *
 * An Activity whose theme is loaded dynamically from SharedPreferences.
 */
public abstract class ThemedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadTheme();
    }

    /**
     * Sets the theme for this Activity based on the style ID stored in SharedPreferences.
     */
    private void loadTheme() {
        SharedPreferences preferences = getSharedPreferences(AppPreferences.PREFERENCES_FILE_NAME,
                MODE_PRIVATE);
        int themeID = preferences.getInt(AppPreferences.APP_THEME, R.style.AppTheme);
        this.setTheme(themeID);
    }

}
