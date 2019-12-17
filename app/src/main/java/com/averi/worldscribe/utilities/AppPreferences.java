package com.averi.worldscribe.utilities;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by mark on 21/06/16.
 */
public class AppPreferences {
    public static final String PREFERENCES_FILE_NAME = "com.averi.worldscribe";
    public static final String LAST_OPENED_WORLD = "lastOpenedWorld";
    public static final String WRITE_PERMISSION_PROMPT_IS_ENABLED = "permissionPromptIsEnabled";
    public static final String APP_THEME = "appTheme";
    public static final String NIGHT_MODE_IS_ENABLED = "nightModeIsEnabled";
    public static final String DROPBOX_ACCESS_TOKEN = "dropboxAccessToken";
    public static final String LAST_OPENED_VERSION_CODE = "lastOpenedVersionCode";
    public static final String LAST_NEXTCLOUD_SERVER = "nextcloudServer";
    public static final String LAST_NEXTCLOUD_USER = "nextcloudUser";

    public static void saveLastNextcloudServer(Context context, String server) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        preferences.edit().putString(AppPreferences.LAST_NEXTCLOUD_SERVER, server).apply();
    }

    public static void saveLastNextcloudUser(Context context, String user) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        preferences.edit().putString(AppPreferences.LAST_NEXTCLOUD_USER, user).apply();
    }

    public static String getLastNextcloudServer(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        return preferences.getString(LAST_NEXTCLOUD_SERVER, "");
    }

    public static String getLastNextcloudUser(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        return preferences.getString(LAST_NEXTCLOUD_USER, "");
    }

    /**
     * Save the name of the World that was last opened, so that it can automatically be loaded the
     * next time the app is launched.
     * @param context The Context calling this method.
     * @param worldName The name of the last opened World.
     */
    public static void saveLastOpenedWorld(Context context, String worldName) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        preferences.edit().putString(AppPreferences.LAST_OPENED_WORLD, worldName).apply();
    }

    /**
     * Checks if Night Mode is enabled.
     * @param context The The Context calling this method.
     * @return True if Night Mode is enabled; false otherwise.
     */
    public static boolean nightModeIsEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        return preferences.getBoolean(NIGHT_MODE_IS_ENABLED, false);
    }

    /**
     * Checks if this app has access to the user's Dropbox account by seeing if an access token
     * for that account exists in preferences.
     * @param context The context calling this method
     * @return True if a Dropbox access token exists in SharedPreferences; false otherwise
     */
    public static boolean dropboxAccessTokenExists(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        return preferences.contains(DROPBOX_ACCESS_TOKEN);
    }
}
