package com.averi.worldscribe.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.ThemedSnackbar;

/**
 * <p>
 *     Created by mark on 22/12/17.
 * </p>
 * <p>
 *     An Activity that has Action Bar Buttons for toggling between Reader Mode and Editor Mode.
 * </p>
 * <p>
 *     Subclasses MUST override {@link #onCreateOptionsMenu(Menu menu)} to inflate the appropriate
 *     menu, and then return <code>super.onCreateOptionsMenu(Menu menu)</code>.<br />
 *     The inflated menu MUST contain <code>R.id.enableReaderModeItem</code> and
 *     <code>R.id.enableEditorModeItem</code>.
 * </p>
 */
public abstract class ReaderModeActivity extends BackButtonActivity {

    /**
     * Set to true if Reader Mode is currently enabled for this Activity.
     */
    private boolean readerModeIsEnabled = false;
    /**
     * Set to true if a Snackbar indicating the current mode should be shown the next time the
     * Options menu is created.
     */
    private boolean showCurrentModeSnackbar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.enableReaderModeItem: {
                ViewGroup rootLayout = this.getRootLayout();
                ActivityUtilities.toggleAllEditTexts(rootLayout, false);
                showCurrentModeSnackbar = true;
                readerModeIsEnabled = true;
                this.invalidateOptionsMenu(); // Reloads the items in the Action Bar
                return true;
            }
            case R.id.enableEditorModeItem: {
                ViewGroup rootLayout = this.getRootLayout();
                ActivityUtilities.toggleAllEditTexts(rootLayout, true);
                showCurrentModeSnackbar = true;
                readerModeIsEnabled = false;
                this.invalidateOptionsMenu(); // Reloads the items in the Action Bar
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This is to prevent the Snackbar from showing up the first time the Activity is loaded,
        // or when the Activity is resumed.
        if (showCurrentModeSnackbar) {
            if (readerModeIsEnabled) {
                menu.findItem(R.id.enableReaderModeItem).setVisible(false);
                menu.findItem(R.id.enableEditorModeItem).setVisible(true);
                ThemedSnackbar.showSnackbarMessage(this, getRootLayout(),
                        getString(R.string.readerModeEnabledMessage));
                showCurrentModeSnackbar = false;
            } else {
                menu.findItem(R.id.enableReaderModeItem).setVisible(true);
                menu.findItem(R.id.enableEditorModeItem).setVisible(false);
                ThemedSnackbar.showSnackbarMessage(this, getRootLayout(),
                        getString(R.string.editorModeEnabledMessage));
                showCurrentModeSnackbar = false;
            }
        }

        return true;
    }

}
