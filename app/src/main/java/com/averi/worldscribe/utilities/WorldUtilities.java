package com.averi.worldscribe.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;

import com.averi.worldscribe.R;
import com.averi.worldscribe.activities.CreateOrLoadWorldActivity;
import com.averi.worldscribe.activities.CreateWorldActivity;

/**
 * Created by mark on 12/08/19.
 */
public class WorldUtilities {

    /**
     * Asks the user to confirm deletion of a certain World, then deletes the World once confirmed.
     * If an error occurs during the process, an error is displayed.
     * @param context The context calling this method.
     * @param worldName The name of the world to be deleted.
     */
    public static void deleteWorld(Context context, String worldName) {
        final Context listenerContext = context;
        final String listenerWorldName = worldName;

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirmWorldDeletionTitle, worldName))
                .setMessage(context.getString(R.string.confirmWorldDeletion, worldName))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean worldWasDeleted = ExternalDeleter.deleteWorld(context, listenerWorldName);

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

        if (ExternalReader.worldListIsEmpty(context)) {
            nextActivityIntent = new Intent(context, CreateWorldActivity.class);
        } else {
            nextActivityIntent = new Intent(context, CreateOrLoadWorldActivity.class);
        }

        nextActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(nextActivityIntent);
    }

}
