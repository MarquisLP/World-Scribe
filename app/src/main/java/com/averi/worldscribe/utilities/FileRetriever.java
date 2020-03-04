package com.averi.worldscribe.utilities;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.balda.flipper.DocumentFileCompat;
import com.balda.flipper.Root;
import com.balda.flipper.StorageManagerCompat;

import org.w3c.dom.Document;

import java.io.File;

/**
 * Created by mark on 14/06/16.
 */
public class FileRetriever {

    public static final String APP_DIRECTORY_NAME = "WorldScribe";
    public static final String SNIPPET_FILE_EXTENSION = ".txt";

    public static DocumentFile getFileRootDirectory(Context context) {
        String rootUriString = context.getSharedPreferences("com.averi.worldscribe", Context.MODE_PRIVATE)
                .getString(AppPreferences.ROOT_DIRECTORY_URI, null);
        Uri rootUri = Uri.parse(rootUriString);

        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        if (rootUriString.startsWith("file")) {
            File rootFile = new File(rootUri.getPath());
            return DocumentFile.fromFile(rootFile);
        }
        else {
            return DocumentFile.fromTreeUri(context, rootUri);
        }
    }

    public static DocumentFile getAppDirectory(Context context, boolean createIfNotExists) {
        //StorageManagerCompat storageManagerCompat = new StorageManagerCompat(context);
        DocumentFile rootDirectory = getFileRootDirectory(context);
        //Root root = storageManagerCompat.getRoot(StorageManagerCompat.DEF_MAIN_ROOT);

        if (createIfNotExists) {
            return DocumentFileCompat.getSubFolder(rootDirectory, APP_DIRECTORY_NAME);
        }
        else {
            return DocumentFileCompat.peekSubFolder(rootDirectory, APP_DIRECTORY_NAME);
        }
    }

    /**
     * @return The .nomedia file located in the top-level app folder.
     */
    public static DocumentFile getNoMediaFile(Context context, boolean createIfNotExists) {
        DocumentFile appDirectory = getAppDirectory(context, createIfNotExists);
        if (appDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getFile(appDirectory, ".nomedia", "blank/blank");
        }
        else {
            return DocumentFileCompat.peekFile(appDirectory, ".nomedia", null);
        }
    }

    public static DocumentFile getWorldDirectory(Context context, String worldName, boolean createIfNotExists) {
        DocumentFile appDirectory = getAppDirectory(context, createIfNotExists);
        if (appDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getSubFolder(appDirectory, worldName);
        }
        else {
            return DocumentFileCompat.peekSubFolder(appDirectory, worldName);
        }
    }

    public static DocumentFile getCategoryDirectory(Context context, String worldName, Category category,
                                                    boolean createIfNotExists) {
        DocumentFile worldDirectory = getWorldDirectory(context, worldName, createIfNotExists);
        if (worldDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getSubFolder(worldDirectory, category.pluralName(context));
        }
        else {
            return DocumentFileCompat.peekSubFolder(worldDirectory, category.pluralName(context));
        }
    }

    public static DocumentFile getArticleDirectory(Context context, String worldName, Category category,
                                           String articleName, boolean createIfNotExists) {
        DocumentFile categoryDirectory = getCategoryDirectory(context, worldName, category, createIfNotExists);
        if (categoryDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getSubFolder(categoryDirectory, articleName);
        }
        else {
            return DocumentFileCompat.peekSubFolder(categoryDirectory, articleName);
        }
    }

    public static DocumentFile getArticleFile(Context context, String worldName, Category category,
                                      String articleName, String fileName, boolean createIfNotExists,
                                              String mimeType) {
        DocumentFile articleDirectory = getArticleDirectory(
                context, worldName, category, articleName, createIfNotExists);
        if (articleDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getFile(articleDirectory, fileName, mimeType);
        }
        else {
            return DocumentFileCompat.peekFile(articleDirectory, fileName, null);
        }
    }

    public static DocumentFile getConnectionsDirectory(Context context, String worldName, Category category,
                                               String articleName, boolean createIfNotExists) {
        DocumentFile articleDirectory = getArticleDirectory(
                context, worldName, category, articleName, createIfNotExists);
        if (articleDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getSubFolder(articleDirectory, "Connections");
        }
        else {
            return DocumentFileCompat.peekSubFolder(articleDirectory, "Connections");
        }
    }

    public static DocumentFile getConnectionCategoryDirectory(Context context, String worldName,
                                                      Category category, String articleName,
                                                      Category connectionCategory,
                                                      boolean createIfNotExists) {
        DocumentFile connectionsDirectory = getConnectionsDirectory(
                context, worldName, category, articleName, createIfNotExists);
        if (connectionsDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getSubFolder(
                    connectionsDirectory, connectionCategory.pluralName(context));
        }
        else {
            return DocumentFileCompat.peekSubFolder(
                    connectionsDirectory, connectionCategory.pluralName(context));
        }
    }

    /**
     * Retrieve the file containing an Article's relation to a connected Article.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param category The {@link Category} of the current Article.
     * @param articleName The name of the current Article.
     * @param connectionCategory The Category of the connected Article.
     * @param connectedArticleName The name of the connected Article.
     * @return A File referring to the specified Article's Snippets directory.
     */
    public static DocumentFile getConnectionRelationFile(Context context, String worldName,
                                                 Category category, String articleName,
                                                 Category connectionCategory,
                                                 String connectedArticleName,
                                                 boolean createIfNotExists) {
        DocumentFile connectionsCategoryDirectory = getConnectionCategoryDirectory(
                context, worldName, category, articleName, connectionCategory, createIfNotExists);
        if (connectionsCategoryDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getFile(
                    connectionsCategoryDirectory,
                    connectedArticleName + ExternalReader.TEXT_FIELD_FILE_EXTENSION, "text/plain");
        }
        else {
            return DocumentFileCompat.peekFile(
                    connectionsCategoryDirectory,
                    connectedArticleName + ExternalReader.TEXT_FIELD_FILE_EXTENSION, "text/plain");
        }
    }

    /**
     * Retrieve the folder containing all of an Article's Snippets.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param category The {@link Category} of the current Article.
     * @param articleName The name of the current Article.
     * @return A File referring to the specified Article's Snippets directory.
     */
    public static DocumentFile getSnippetsDirectory(Context context, String worldName, Category category,
                                            String articleName, boolean createIfNotExists) {
        DocumentFile articleDirectory = getArticleDirectory(
                context, worldName, category, articleName, createIfNotExists);
        if (articleDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getSubFolder(articleDirectory, "Snippets");
        }
        else {
            return DocumentFileCompat.peekSubFolder(articleDirectory, "Snippets");
        }
    }

    /**
     * Retrieve a Snippet's file.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param category The {@link Category} of Article the Snippet belongs to.
     * @param articleName The name of the Article the Snippet belongs to.
     * @param snippetName The name of the Snippet being loaded.
     * @return A File referencing the specified Snippet.
     */
    public static DocumentFile getSnippetFile(Context context, String worldName, Category category,
                                      String articleName, String snippetName, boolean createIfNotExists) {
        DocumentFile snippetsDirectory = getSnippetsDirectory(
                context, worldName, category, articleName, createIfNotExists);
        if (snippetsDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getFile(snippetsDirectory,
                    snippetName + SNIPPET_FILE_EXTENSION, "text/plain");
        }
        else {
            return DocumentFileCompat.peekFile(snippetsDirectory,
                    snippetName + SNIPPET_FILE_EXTENSION, "text/plain");
        }
    }

    /**
     * Retrieve the folder containing all of a Person's Residences.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param personName The name of the Person whose Residences are being retrieved.
     * @return A File referring to the specified Person's Residences folder.
     */
    public static DocumentFile getResidencesDirectory(Context context, String worldName,
                                              String personName, boolean createIfNotExists) {
        DocumentFile articleDirectory = getArticleDirectory(
                context, worldName,Category.Person, personName, createIfNotExists);
        if (articleDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getSubFolder(articleDirectory, "Residences");
        }
        else {
            return DocumentFileCompat.peekSubFolder(articleDirectory, "Residences");
        }
    }

    /**
     * Retrieves the Residence file linking the specified Person to a Place.
     * @param context The Context calling this method.
     * @param worldName The name of the World where this Residence takes place.
     * @param personName The name of the Person whose Residences are being searched.
     * @param placeName The name of a Place where the specified Person resides.
     * @return The File named after the Place of Residence.
     */
    public static DocumentFile getResidenceFile(Context context, String worldName, String personName,
                                        String placeName, boolean createIfNotExists) {
        DocumentFile residencesDirectory = getResidencesDirectory(
                context, worldName, personName, createIfNotExists);
        if (residencesDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getFile(residencesDirectory,
                    placeName + ExternalReader.TEXT_FIELD_FILE_EXTENSION, "text/plain");
        }
        else {
            return DocumentFileCompat.peekFile(residencesDirectory,
                    placeName + ExternalReader.TEXT_FIELD_FILE_EXTENSION, "text/plain");
        }
    }

    /**
     * Retrieve the folder containing all of a Place's Residents.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param placeName The name of the Place whose Residents are being retrieved.
     * @return A File referring to the specified Place's folder.
     */
    public static DocumentFile getResidentsDirectory(Context context, String worldName,
                                              String placeName, boolean createIfNotExists) {
        DocumentFile articleDirectory = getArticleDirectory(
                context, worldName, Category.Place, placeName, createIfNotExists);
        if (articleDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getSubFolder(articleDirectory, "Residents");
        }
        else {
            return DocumentFileCompat.peekSubFolder(articleDirectory, "Residents");
        }
    }

    /**
     * Retrieves the file representing a certain resident of the specified Place.
     * @param context The Context calling this method.
     * @param worldName The name of the World where this Residence takes place.
     * @param placeName The name of the Place whose residents are being searched.
     * @param personName The name of a Person who resides in the specified Place.
     * @return The File named after the specified resident.
     */
    public static DocumentFile getResidentFile(Context context, String worldName, String placeName,
                                        String personName, boolean createIfNotExists) {
        DocumentFile residentsDirectory = getResidentsDirectory(
                context, worldName, placeName, createIfNotExists);
        if (residentsDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getFile(residentsDirectory,
                    personName + ExternalReader.TEXT_FIELD_FILE_EXTENSION, "text/plain");
        }
        else {
            return DocumentFileCompat.peekFile(residentsDirectory,
                    personName + ExternalReader.TEXT_FIELD_FILE_EXTENSION, "text/plain");
        }
    }

    /**
     * Retrieve the folder containing all of a Person's Memberships.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param personName The name of the Person whose Memberships are being retrieved.
     * @return A File referring to the specified Person's Memberships folder.
     */
    public static DocumentFile getMembershipsDirectory(Context context, String worldName,
                                              String personName, boolean createIfNotExists) {
        DocumentFile articleDirectory = getArticleDirectory(
                context, worldName, Category.Person, personName, createIfNotExists);
        if (articleDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getSubFolder(articleDirectory, "Memberships");
        }
        else {
            return DocumentFileCompat.peekSubFolder(articleDirectory, "Memberships");
        }
    }

    /**
     * Retrieves the Membership file linking the specified Person to a Group.
     * @param context The Context calling this method.
     * @param worldName The name of the World where the Person and Group reside.
     * @param personName The name of the Person whose Memberships are being searched.
     * @param groupName The name of the Group who Membership is being retrieved.
     * @return The File containing the Person's role within the Group.
     */
    public static DocumentFile getMembershipFile(Context context, String worldName, String personName,
                                         String groupName, boolean createIfNotExists) {
        DocumentFile membershipsDirectory = getMembershipsDirectory(
                context, worldName, personName, createIfNotExists);
        if (membershipsDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            DocumentFile membershipFile = DocumentFileCompat.getFile(membershipsDirectory,
                    groupName + ExternalReader.TEXT_FIELD_FILE_EXTENSION, "text/plain");
            return membershipFile;
        }
        else {
            return DocumentFileCompat.peekFile(membershipsDirectory,
                    groupName + ExternalReader.TEXT_FIELD_FILE_EXTENSION, "text/plain");
        }
    }

    /**
     * Retrieve the folder containing all of a Group's Members.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param groupName The name of the Group whose Members are being retrieved.
     * @return A File referring to the specified Group's Members folder.
     */
    public static DocumentFile getMembersDirectory(Context context, String worldName,
                                               String groupName, boolean createIfNotExists) {
        DocumentFile articleDirectory = getArticleDirectory(
                context, worldName, Category.Group, groupName, createIfNotExists);
        if (articleDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getSubFolder(articleDirectory, "Members");
        }
        else {
            return DocumentFileCompat.peekSubFolder(articleDirectory, "Members");
        }
    }

    /**
     * Retrieves the file representing a certain member within the specified Group.
     * @param context The Context calling this method.
     * @param worldName The name of the World where the Group and Person reside.
     * @param groupName The name of the Group whose members are being searched..
     * @param memberName The name of a Person who has a Membership with the Group.
     * @return The File containing the member's role within the Group.
     */
    public static DocumentFile getMemberFile(Context context, String worldName, String groupName,
                                         String memberName, boolean createIfNotExists) {
        DocumentFile membersDirectory = getMembersDirectory(
                context, worldName, groupName, createIfNotExists);
        if (membersDirectory == null) {
            return null;
        }
        if (createIfNotExists) {
            return DocumentFileCompat.getFile(membersDirectory,
                    memberName + ExternalReader.TEXT_FIELD_FILE_EXTENSION, "text/plain");
        }
        else {
            return DocumentFileCompat.peekFile(membersDirectory,
                    memberName + ExternalReader.TEXT_FIELD_FILE_EXTENSION, "text/plain");
        }
    }

}
