package com.averi.worldscribe.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.averi.worldscribe.R;
import com.averi.worldscribe.activities.ArticleListActivity;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.activities.CreateWorldActivity;
import com.averi.worldscribe.activities.LoadWorldActivity;

/**
 * Created by mark on 23/06/16.
 */
public class ActivityUtilities {

    public static void goToWorld(Context context, String worldName) {
        saveLastOpenedWorld(context, worldName);

        Intent goToWorldIntent = new Intent(context, ArticleListActivity.class);
        goToWorldIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        goToWorldIntent.putExtra(AppPreferences.WORLD_NAME, worldName);
        goToWorldIntent.putExtra("category", Category.Person);
        context.startActivity(goToWorldIntent);
    }

    private static void saveLastOpenedWorld(Context context, String worldName) {
        SharedPreferences preferences = context.getSharedPreferences("com.averi.worldscribe",
                AppCompatActivity.MODE_PRIVATE);
        preferences.edit().putString("lastOpenedWorldName", worldName).apply();
    }

    /**
     * Handle app bar items that are common to all Activities that have an app bar.
     * For example, the "Create World" option is an item that can always be accessed from the app
     * bar.
     * @param context The Context calling this method.
     * @param item The item that was selected from the app bar's menu.
     */
    public static void handleCommonAppBarItems(Context context, String worldName, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.createWorldItem:
                Intent goToWorldCreationIntent = new Intent(context, CreateWorldActivity.class);
                context.startActivity(goToWorldCreationIntent);
                break;
            case R.id.loadWorldItem:
                Intent goToLoadWorldIntent = new Intent(context, LoadWorldActivity.class);
                context.startActivity(goToLoadWorldIntent);
                break;
            case R.id.deleteWorldItem:
                // Delete the current World.
                break;
            case R.id.settingsItem:
                // Go to the Settings Activity.
        }
    }

}
