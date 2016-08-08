package com.averi.worldscribe.utilities;

import android.content.Context;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;

import java.io.File;

/**
 * Created by mark on 28/07/16.
 *
 * Handles the deletion of external app files.
 */
public class ExternalDeleter {

    /**
     * Deletes a World.
     * @param worldName The name of the world to delete.
     * @return True if the World was deleted successfully; false otherwise.
     */
    public static boolean deleteWorld(String worldName) {
        File worldDirectory = FileRetriever.getWorldDirectory(worldName);
        return deleteRecursive(worldDirectory);
    }

    /**
     * Delete a file, or a directory and its contents.
     * @param fileOrDirectory The file or directory to be deleted.
     * @return True if the file/directory was deleted successfully; false otherwise.
     */
    private static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        return fileOrDirectory.delete();
    }

    /**
     * Deletes a Snippet belonging to the specified Article.
     * @param context The Context calling this method.
     * @param worldName The name of the World the Article belongs to.
     * @param category The Category of the Article.
     * @param articleName The name of the Article the Snippet belongs to.
     * @param snippetName The name of the Snippet to delete.
     * @return True if the Snippet was deleted successfully; false otherwise.
     */
    public static boolean deleteSnippet(Context context, String worldName, Category category,
                                        String articleName, String snippetName) {
        return FileRetriever.getSnippetFile(context, worldName, category, articleName,
                snippetName).delete();
    }

    /**
     * Deletes a {@link Connection} between two Articles.
     * @param context
     * @param connection
     * @return
     */
    public static boolean deleteConnection(Context context, Connection connection) {
        File fileInMainArticleDirectory = FileRetriever.getConnectionRelationFile(context,
                connection.worldName, connection.articleCategory, connection.articleName,
                connection.connectedArticleCategory, connection.connectedArticleName);
        File fileInConnectedArticleDirectory = FileRetriever.getConnectionRelationFile(context,
                connection.worldName, connection.connectedArticleCategory,
                connection.connectedArticleName, connection.articleCategory,
                connection.articleName);
        return ((fileInMainArticleDirectory.delete()) &&
                (fileInConnectedArticleDirectory.delete()));
    }
}
