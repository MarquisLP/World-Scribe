package tools;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import com.averi.worldscribe.ArticleListActivity;
import com.averi.worldscribe.Category;

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

}
