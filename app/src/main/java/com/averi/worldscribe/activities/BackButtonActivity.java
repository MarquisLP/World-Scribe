package com.averi.worldscribe.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.MenuItem;

import com.averi.worldscribe.R;

/**
 * <p>
 *     Created by mark on 29/08/16.
 * </p>
 * <p>
 *     An Activity that has a Back Button on the app bar, allowing the user to go up one level.
 * </p>
 * <p>
 *     Subclasses must override setAppBar.
 * </p>
 */
public abstract class BackButtonActivity extends ThemedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * <p>
     *     Sets up this Activity's app bar, including adding the Back Button.
     * </p>
     * <p>
     *     Subclasses must override this method, set up the app bar, and then call super.
     * </p>
     */
    protected void setAppBar() {
        final Drawable upArrow = ContextCompat.getDrawable(this,
                R.drawable.back_button);
        upArrow.setColorFilter(ContextCompat.getColor(this, android.R.color.white),
                PorterDuff.Mode.SRC_ATOP);

        assert getSupportActionBar() != null;
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

}
