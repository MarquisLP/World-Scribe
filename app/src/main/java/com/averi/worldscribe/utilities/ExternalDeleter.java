package com.averi.worldscribe.utilities;

import android.content.Context;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;
import com.averi.worldscribe.Membership;
import com.averi.worldscribe.Residence;

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
    public static boolean deleteWorld(Context context, String worldName) {
        DocumentFile worldDirectory = FileRetriever.getWorldDirectory(context, worldName, false);
        return deleteRecursive(worldDirectory);
    }

    /**
     * Delete a file, or a directory and its contents.
     * @param fileOrDirectory The file or directory to be deleted.
     * @return True if the file/directory was deleted successfully; false otherwise.
     */
    public static boolean deleteRecursive(DocumentFile fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (DocumentFile child : fileOrDirectory.listFiles()) {
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
                snippetName, false).delete();
    }

    /**
     * Deletes a {@link Connection} between two Articles.
     * @param context The Context calling this method.
     * @param connection The Connection that will be deleted.
     * @return True if the Connection was deleted successfully; false otherwise.
     */
    public static boolean deleteConnection(Context context, Connection connection) {
        DocumentFile fileInMainArticleDirectory = FileRetriever.getConnectionRelationFile(context,
                connection.worldName, connection.articleCategory, connection.articleName,
                connection.connectedArticleCategory, connection.connectedArticleName, false);
        DocumentFile fileInConnectedArticleDirectory = FileRetriever.getConnectionRelationFile(context,
                connection.worldName, connection.connectedArticleCategory,
                connection.connectedArticleName, connection.articleCategory,
                connection.articleName, false);
        return ((fileInMainArticleDirectory.delete()) &&
                (fileInConnectedArticleDirectory.delete()));
    }

    /**
     * Deletes a Person's {@link Membership} within a Group.
     * @param context The Context calling this method.
     * @param membership The Membership that will be deleted.
     * @return True if the Membership was deleted successfully; false otherwise.
     */
    public static boolean deleteMembership(Context context, Membership membership) {
        DocumentFile fileInPersonDirectory = FileRetriever.getMembershipFile(context,
                membership.worldName, membership.memberName, membership.groupName, false);
        DocumentFile fileInGroupDirectory = FileRetriever.getMemberFile(context,
                membership.worldName, membership.groupName, membership.memberName, false);
        return ((fileInPersonDirectory.delete()) &&
                (fileInGroupDirectory.delete()));
    }

    /**
     * Deletes a Person's {@link Residence} at a Place.
     * @param context The Context calling this method.
     * @param residence The Residence that will be deleted.
     * @return True if the Residence was deleted successfully; false otherwise.
     */
    public static boolean deleteResidence(Context context, Residence residence) {
        DocumentFile fileInPersonDirectory = FileRetriever.getResidenceFile(context,
                residence.worldName, residence.residentName, residence.placeName, false);
        DocumentFile fileInPlaceDirectory = FileRetriever.getResidentFile(context,
                residence.worldName, residence.placeName, residence.residentName, false);
        return ((fileInPersonDirectory.delete()) &&
                (fileInPlaceDirectory.delete()));
    }

    /**
     * Deletes the directory of the specified Article.
     * @param context The The Context calling this method.
     * @param worldName The name of the World the Article belongs to.
     * @param category The Article's category.
     * @param articleName The Article's name.
     * @return True if the Article's directory was deleted successfully; false otherwise.
     */
    public static boolean deleteArticleDirectory(Context context, String worldName,
                                                 Category category, String articleName) {
        DocumentFile articleDirectory = FileRetriever.getArticleDirectory(context, worldName, category,
                articleName, false);
        return deleteRecursive(articleDirectory);
    }

    /**
     * Deletes all files for this app in internal storage.
     * @return True if the deletion was successful; false otherwise
     */
    public static boolean clearInternalStorageDirectory(Context context) {
        File internalStorageDirectory = context.getFilesDir();
        boolean result = true;
        for (File file : internalStorageDirectory.listFiles()) {
            result = deleteLocalFileRecursive(file);
            if (!(result)) {
                break;
            }
        }
        return result;
    }

    /**
     * Deletes a file or folder stored in internal or external storage. Use this when
     * dealing with normal Java File objects, rather than DocumentFiles.
     * @param fileToDelete The file that will be deleted
     * @return True if the file/folder was deleted successfully; false otherwise
     */
    public static boolean deleteLocalFileRecursive(File fileToDelete) {
        boolean result = true;
        if (fileToDelete.isDirectory()) {
            for (File subFile : fileToDelete.listFiles()) {
                result = deleteLocalFileRecursive(subFile);
                if (!(result)) {
                    break;
                }
            }
        }
        else {
            result = fileToDelete.delete();
        }
        return result;
    }
}
