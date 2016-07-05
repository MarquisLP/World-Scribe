package com.averi.worldscribe.activities;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by mark on 05/07/16.
 *
 * ArticleActivity is the superclass for each Activity that displays a certain Category of Articles.
 *
 * All subclasses must implement {@link #getLayoutResourceID} and call
 * {@link #setUpArticleCore} within onCreate.
 */
public abstract class ArticleActivity extends AppCompatActivity {

    /**
     * Load data pertaining to the selected Article, and use it in set-up processes that are common
     * to Article Activities of all Categories.
     */
    protected void setUpArticleCore() {

    }

    /**
     * @return The Android Resource ID of this Activity's layout resource file.
     */
    protected abstract int getLayoutResourceID();

}
