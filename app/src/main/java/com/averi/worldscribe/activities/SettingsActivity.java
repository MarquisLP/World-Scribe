package com.averi.worldscribe.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;

import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.AppThemeArrayAdapter;
import com.averi.worldscribe.utilities.AppPreferences;

public class SettingsActivity extends ThemedActivity {

    public static final int SKY_BLUE = 0;
    public static final int SUNSET_ORANGE = 1;
    public static final int ENCHANTED_GREEN = 2;
    public static final int UBE_PURPLE = 3;
    public static final int LOVELY_RED = 4;

    private Spinner appThemeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        appThemeSpinner = (Spinner) findViewById(R.id.themeSelector);

        setAppBar();
        populateAppThemeSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveEditItem:
                saveAppSettings();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    /**
     * Saves the app settings that are set in this Activity.
     */
    private void saveAppSettings() {
        SharedPreferences preferences = getSharedPreferences(AppPreferences.PREFERENCES_FILE_NAME,
                MODE_PRIVATE);
        preferences.edit().putInt(AppPreferences.APP_THEME, getSelectedAppTheme()).apply();
    }

    /**
     * @return The ID of the theme selected by the user.
     */
    private int getSelectedAppTheme() {
        int themeID;

        switch (appThemeSpinner.getSelectedItemPosition()) {
            case SKY_BLUE:
            default:
                themeID = R.style.AppTheme;
                break;
            case SUNSET_ORANGE:
                themeID = R.style.SunsetOrange;
                break;
            case ENCHANTED_GREEN:
                themeID = R.style.EnchantedGreen;
                break;
            case UBE_PURPLE:
                themeID = R.style.UbePurple;
                break;
            case LOVELY_RED:
                themeID = R.style.LovelyRed;
        }

        return themeID;
    }

}
