package com.averi.worldscribe.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.ActivityUtilities;

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
                readerModeIsEnabled = true;
                this.invalidateOptionsMenu(); // Reloads the items in the Action Bar
                return true;
            }
            case R.id.enableEditorModeItem: {
                ViewGroup rootLayout = this.getRootLayout();
                ActivityUtilities.toggleAllEditTexts(rootLayout, true);
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
        if (readerModeIsEnabled) {
            menu.findItem(R.id.enableReaderModeItem).setVisible(false);
            menu.findItem(R.id.enableEditorModeItem).setVisible(true);
        } else {
            menu.findItem(R.id.enableReaderModeItem).setVisible(true);
            menu.findItem(R.id.enableEditorModeItem).setVisible(false);
        }

        return true;
    }

}
