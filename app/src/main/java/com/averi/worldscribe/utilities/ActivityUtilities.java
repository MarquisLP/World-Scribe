package com.averi.worldscribe.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.averi.worldscribe.R;
import com.averi.worldscribe.activities.ArticleListActivity;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.activities.CreateOrLoadWorldActivity;
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
        goToWorldIntent.putExtra(IntentFields.WORLD_NAME, worldName);
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
                deleteWorld(context, worldName);
                break;
            case R.id.settingsItem:
                // Go to the Settings Activity.
        }
    }

    /**
     * Asks the user to confirm deletion of a certain World, then deletes the World once confirmed.
     * If an error occurs during the process, an error is displayed.
     * @param context The context calling this method.
     * @param worldName The name of the world to be deleted.
     */
    private static void deleteWorld(Context context, String worldName) {
        final Context listenerContext = context;
        final String listenerWorldName = worldName;

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirmWorldDeletionTitle, worldName))
                .setMessage(context.getString(R.string.confirmWorldDeletion, worldName))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean worldWasDeleted = ExternalDeleter.deleteWorld(listenerWorldName);

                        if (worldWasDeleted) {
                            goToNextActivityAfterWorldDeletion(listenerContext);
                        } else {
                            Toast.makeText(listenerContext,
                                    listenerContext.getString(R.string.deleteWorldError),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    /**
     * Goes to the appropriate Activity after deleting the World currently opened in the app.
     * If at least one other World exists in the app directory, the CreateOrLoadWorldActivity will
     * be opened.
     * If no Worlds exist after deleting the current one, CreateWorldActivity will be opened.
     * @param context The Context calling this method.
     */
    private static void goToNextActivityAfterWorldDeletion(Context context) {
        Intent nextActivityIntent;

        if (ExternalReader.worldListIsEmpty()) {
            nextActivityIntent = new Intent(context, CreateWorldActivity.class);
        } else {
            nextActivityIntent = new Intent(context, CreateOrLoadWorldActivity.class);
        }

        nextActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(nextActivityIntent);
    }

}
