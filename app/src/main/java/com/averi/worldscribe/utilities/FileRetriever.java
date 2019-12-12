package com.averi.worldscribe.utilities;

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
    public static final String SNIPPET_FILE_EXTENSION = ".txt";

    public static File getAppDirectory() {
        return new File(Environment.getExternalStorageDirectory(), APP_DIRECTORY_NAME);
    }

    /**
     * @return The .nomedia file located in the top-level app folder.
     */
    public static File getNoMediaFile() {
        return new File(getAppDirectory(), ".nomedia");
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

    public static File getArticleFile(Context context, String worldName, Category category,
                                      String articleName, String fileName) {
        return new File(getArticleDirectory(context, worldName, category, articleName),
                fileName);
    }

    public static File getConnectionsDirectory(Context context, String worldName, Category category,
                                               String articleName) {
        return new File(getArticleDirectory(context, worldName, category, articleName),
                "Connections");
    }

    public static File getConnectionCategoryDirectory(Context context, String worldName,
                                                      Category category, String articleName,
                                                      Category connectionCategory) {
        return new File(getConnectionsDirectory(context, worldName, category, articleName),
                connectionCategory.pluralName(context));
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
    public static File getConnectionRelationFile(Context context, String worldName,
                                                 Category category, String articleName,
                                                 Category connectionCategory,
                                                 String connectedArticleName) {
        return new File(getConnectionCategoryDirectory(context, worldName, category, articleName,
                                                       connectionCategory),
                        connectedArticleName + ExternalReader.TEXT_FIELD_FILE_EXTENSION);
    }

    /**
     * Retrieve the folder containing all of an Article's Snippets.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param category The {@link Category} of the current Article.
     * @param articleName The name of the current Article.
     * @return A File referring to the specified Article's Snippets directory.
     */
    public static File getSnippetsDirectory(Context context, String worldName, Category category,
                                            String articleName) {
        return new File(getArticleDirectory(context, worldName, category, articleName),
                "Snippets");
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
    public static File getSnippetFile(Context context, String worldName, Category category,
                                      String articleName, String snippetName) {
        return new File(getSnippetsDirectory(context, worldName, category, articleName),
                snippetName + SNIPPET_FILE_EXTENSION);
    }

    /**
     * Retrieve the folder containing all of a Person's Residences.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param personName The name of the Person whose Residences are being retrieved.
     * @return A File referring to the specified Person's Residences folder.
     */
    public static File getResidencesDirectory(Context context, String worldName,
                                              String personName) {
        return new File(getArticleDirectory(context, worldName, Category.Person, personName),
                "Residences");
    }

    /**
     * Retrieves the Residence file linking the specified Person to a Place.
     * @param context The Context calling this method.
     * @param worldName The name of the World where this Residence takes place.
     * @param personName The name of the Person whose Residences are being searched.
     * @param placeName The name of a Place where the specified Person resides.
     * @return The File named after the Place of Residence.
     */
    public static File getResidenceFile(Context context, String worldName, String personName,
                                        String placeName) {
        return new File(getResidencesDirectory(context, worldName, personName),
                placeName + ExternalReader.TEXT_FIELD_FILE_EXTENSION);
    }

    /**
     * Retrieve the folder containing all of a Place's Residents.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param placeName The name of the Place whose Residents are being retrieved.
     * @return A File referring to the specified Place's folder.
     */
    public static File getResidentsDirectory(Context context, String worldName,
                                              String placeName) {
        return new File(getArticleDirectory(context, worldName, Category.Place, placeName),
                "Residents");
    }

    /**
     * Retrieves the file representing a certain resident of the specified Place.
     * @param context The Context calling this method.
     * @param worldName The name of the World where this Residence takes place.
     * @param placeName The name of the Place whose residents are being searched.
     * @param personName The name of a Person who resides in the specified Place.
     * @return The File named after the specified resident.
     */
    public static File getResidentFile(Context context, String worldName, String placeName,
                                        String personName) {
        return new File(getResidentsDirectory(context, worldName, placeName),
                personName + ExternalReader.TEXT_FIELD_FILE_EXTENSION);
    }

    /**
     * Retrieve the folder containing all of a Person's Memberships.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param personName The name of the Person whose Memberships are being retrieved.
     * @return A File referring to the specified Person's Memberships folder.
     */
    public static File getMembershipsDirectory(Context context, String worldName,
                                              String personName) {
        return new File(getArticleDirectory(context, worldName, Category.Person, personName),
                "Memberships");
    }

    /**
     * Retrieves the Membership file linking the specified Person to a Group.
     * @param context The Context calling this method.
     * @param worldName The name of the World where the Person and Group reside.
     * @param personName The name of the Person whose Memberships are being searched.
     * @param groupName The name of the Group who Membership is being retrieved.
     * @return The File containing the Person's role within the Group.
     */
    public static File getMembershipFile(Context context, String worldName, String personName,
                                         String groupName) {
        return new File(getMembershipsDirectory(context, worldName, personName),
                groupName + ExternalReader.TEXT_FIELD_FILE_EXTENSION);
    }

    /**
     * Retrieve the folder containing all of a Group's Members.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param groupName The name of the Group whose Members are being retrieved.
     * @return A File referring to the specified Group's Members folder.
     */
    public static File getMembersDirectory(Context context, String worldName,
                                               String groupName) {
        return new File(getArticleDirectory(context, worldName, Category.Group, groupName),
                "Members");
    }

    /**
     * Retrieves the file representing a certain member within the specified Group.
     * @param context The Context calling this method.
     * @param worldName The name of the World where the Group and Person reside.
     * @param groupName The name of the Group whose members are being searched..
     * @param memberName The name of a Person who has a Membership with the Group.
     * @return The File containing the member's role within the Group.
     */
    public static File getMemberFile(Context context, String worldName, String groupName,
                                         String memberName) {
        return new File(getMembersDirectory(context, worldName, groupName),
                memberName + ExternalReader.TEXT_FIELD_FILE_EXTENSION);
    }

}
