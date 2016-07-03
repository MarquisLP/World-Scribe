package tools;

import android.content.Context;
import android.os.Environment;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;

import java.io.File;

/**
 * Created by mark on 14/06/16.
 */
public class FileRetriever {

    public static final String APP_DIRECTORY_NAME = "WorldScribe";

    public static File getAppDirectory() {
        return new File(Environment.getExternalStorageDirectory(), APP_DIRECTORY_NAME);
    }

    public static File getWorldDirectory(String worldName) {
        return new File(getAppDirectory(), worldName);
    }

    public static File getCategoryDirectory(Context context, String worldName, Category category) {
        return new File(getWorldDirectory(worldName), category.pluralName(context));
    }

    public static File getArticleDirectory(Context context, String worldName, Category category,
                                           String articleName) {
        return new File(getCategoryDirectory(context, worldName, category), articleName);
    }

    public static File getConnectionsDirectory(Context context, String worldName, Category category,
                                               String articleName) {
        return new File(getArticleDirectory(context, worldName, category, articleName),
                context.getResources().getString(R.string.connectionsText));
    }

    public static File getConnectionCategoryDirectory(Context context, String worldName,
                                                      Category category, String articleName,
                                                      Category connectionCategory) {
        return new File(getConnectionsDirectory(context, worldName, category, articleName),
                connectionCategory.pluralName(context));
    }
}
