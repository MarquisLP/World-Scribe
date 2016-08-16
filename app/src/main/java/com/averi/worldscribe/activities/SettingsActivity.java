package com.averi.worldscribe.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Spinner;

import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.AppThemeArrayAdapter;

public class SettingsActivity extends AppCompatActivity {

    private Spinner appThemeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        appThemeSpinner = (Spinner) findViewById(R.id.themeSelector);

        setAppBar();
        populateAppThemeSpinner();
    }

    /**
     * Setup this Activity's app bar.
     */
    private void setAppBar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        assert myToolbar != null;
        myToolbar.setTitle(R.string.settingsTitle);
        setSupportActionBar(myToolbar);
    }

    /**
     * Populate the App Theme spinner with all available app themes.
     */
    private void populateAppThemeSpinner() {
        Integer allThemes[] = {R.style.AppTheme, R.style.SunsetOrange, R.style.EnchantedGreen,
            R.style.UbePurple, R.style.LovelyRed};
        AppThemeArrayAdapter adapter = new AppThemeArrayAdapter(this, allThemes);
        appThemeSpinner.setAdapter(adapter);
    }

}
