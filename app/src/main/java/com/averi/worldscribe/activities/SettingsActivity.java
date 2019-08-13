package com.averi.worldscribe.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.AppThemeArrayAdapter;
import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.AttributeGetter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.WorldUtilities;

public class SettingsActivity extends BackButtonActivity {

    public static final int SKY_BLUE = 0;
    public static final int SUNSET_ORANGE = 1;
    public static final int ENCHANTED_GREEN = 2;
    public static final int UBE_PURPLE = 3;
    public static final int LOVELY_RED = 4;

    private int currentThemeIndex;
    private boolean nightModeIsCurrentlyEnabled;

    private Spinner appThemeSpinner;
    private TextView restartNotice;
    private Switch nightModeSwitch;
    private String worldName;
    private Button deleteWorldButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appThemeSpinner = (Spinner) findViewById(R.id.themeSelector);
        restartNotice = (TextView) findViewById(R.id.restartNotice);
        nightModeSwitch = (Switch) findViewById(R.id.nightModeSwitch);

        setAppBar();
        populateAppThemeSpinner();
        selectCurrentAppTheme();
        currentThemeIndex = appThemeSpinner.getSelectedItemPosition();
        nightModeSwitch.setChecked(getSharedPreferences(AppPreferences.PREFERENCES_FILE_NAME,
                MODE_PRIVATE).getBoolean(AppPreferences.NIGHT_MODE_IS_ENABLED, false));
        nightModeIsCurrentlyEnabled = nightModeSwitch.isChecked();

        appThemeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (appThemeWasChanged()) {
                    restartNotice.setVisibility(View.VISIBLE);
                } else if (!(nightModeWasChanged())) {
                    restartNotice.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        nightModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (nightModeWasChanged()) {
                    restartNotice.setVisibility(View.VISIBLE);
                } else if (!(appThemeWasChanged())) {
                    restartNotice.setVisibility(View.GONE);
                }
            }
        });

        Intent intent = getIntent();
        worldName = intent.getStringExtra(IntentFields.WORLD_NAME);
        deleteWorldButton = (Button) findViewById(R.id.deleteWorldButton);
        final Context context = this;
        deleteWorldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WorldUtilities.deleteWorld(context, worldName);
            }
        });
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_settings;
    }

    @Override
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.root);
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

    @Override
    protected void setAppBar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        assert myToolbar != null;
        myToolbar.setTitle(R.string.settingsTitle);
        setSupportActionBar(myToolbar);

        super.setAppBar();
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
     * Sets the selected item in the app theme spinner to be the theme that is currently applied.
     */
    private void selectCurrentAppTheme() {
        String currentThemeName = AttributeGetter.getStyleName(getTheme());

        if (currentThemeName.equals(AttributeGetter.getStyleName(this, R.style.AppTheme))) {
            appThemeSpinner.setSelection(SKY_BLUE);
        } else if (currentThemeName.equals(AttributeGetter.getStyleName(
                this, R.style.SunsetOrange))) {
            appThemeSpinner.setSelection(SUNSET_ORANGE);
        } else if (currentThemeName.equals(AttributeGetter.getStyleName(
                this, R.style.EnchantedGreen))) {
            appThemeSpinner.setSelection(ENCHANTED_GREEN);
        } else if (currentThemeName.equals(AttributeGetter.getStyleName(
                this, R.style.UbePurple))) {
            appThemeSpinner.setSelection(UBE_PURPLE);
        } else {
            appThemeSpinner.setSelection(LOVELY_RED);
        }
    }

    /**
     * Saves the app settings that are set in this Activity.
     */
    private void saveAppSettings() {
        SharedPreferences preferences = getSharedPreferences(AppPreferences.PREFERENCES_FILE_NAME,
                MODE_PRIVATE);
        preferences.edit().putInt(AppPreferences.APP_THEME, getSelectedAppTheme()).apply();
        preferences.edit().putBoolean(AppPreferences.NIGHT_MODE_IS_ENABLED,
                nightModeSwitch.isChecked()).apply();

        if ((appThemeWasChanged()) || (nightModeWasChanged())) {
            relaunchApp();
        }
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

    /**
     * @return True if the Theme selected in the app Theme spinner differs from the
     * currently-applied Theme; false otherwise.
     */
    private boolean appThemeWasChanged() {
        return (appThemeSpinner.getSelectedItemPosition() != currentThemeIndex);
    }

    /**
     * @return True if the Night Mode switch's value was changed.
     */
    private boolean nightModeWasChanged() {
        return (nightModeSwitch.isChecked() != nightModeIsCurrentlyEnabled);
    }

    /**
     * Exits and reopens the entire application.
     */
    private void relaunchApp() {
        Intent restartIntent = getBaseContext().getPackageManager().getLaunchIntentForPackage(
                getBaseContext().getPackageName());
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(restartIntent);
    }

}
