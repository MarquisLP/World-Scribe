package com.averi.worldscribe.utilities;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;
import com.averi.worldscribe.Membership;
import com.averi.worldscribe.R;
import com.averi.worldscribe.Residence;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by mark on 08/06/16.
 */
public final class ExternalWriter {

    public static final int IMAGE_BYTE_SIZE = 1024;

    private static boolean externalStorageIsWritable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }

    /**
     * Creates the app's directory on the phone's external storage.
     * @return True if the directory was created successfully; false otherwise.
     */
    public static boolean createAppDirectory() {
        return FileRetriever.getAppDirectory().mkdirs();
    }

    public static boolean createWorldDirectory(Context context, String worldName) {
        boolean directoryWasCreated = true;

        File worldDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + FileRetriever.APP_DIRECTORY_NAME + "/", worldName);

        directoryWasCreated = worldDirectory.mkdirs();

        if (directoryWasCreated) {
            directoryWasCreated = createArticleTypeDirectories(context, worldDirectory);

            // If one or more subfolders couldn't be created, delete the World folder so that
            // subsequent attempts can start building the World folder again from scratch.
            if (!(directoryWasCreated)) {
                // Delete the World directory.
            }
        }

        return directoryWasCreated;
    }

    private static boolean createArticleTypeDirectories(Context context, File worldDirectory) {
        boolean directoriesWereCreated = true;

        for (Category category : Category.values()) {
            File articleFolder = new File(worldDirectory.getAbsolutePath(), category.pluralName(context));

            if (!(articleFolder.mkdirs())) {
                directoriesWereCreated = false;
            }
        }

        return directoriesWereCreated;
    }

    /**
     * Create a directory for a new Article.
     * Note that this does not overwrite an existing directory, so make sure to check before calling
     * this method.
     * @param context The context calling this method.
     * @param worldName The name of the World where the Article belongs.
     * @param category The {@link Category} the Article belongs to.
     * @param articleName The name of the new Article.
     * @return True if the directory was created successfully; false otherwise.
     */
    public static boolean createArticleDirectory(Context context, String worldName,
                                                  Category category, String articleName) {
        Boolean successful = true;

        successful = (
                (FileRetriever.getConnectionCategoryDirectory(context, worldName, category,
                        articleName, Category.Person).mkdirs())
                && (FileRetriever.getConnectionCategoryDirectory(context, worldName, category,
                        articleName, Category.Group).mkdirs())
                && (FileRetriever.getConnectionCategoryDirectory(context, worldName, category,
                        articleName, Category.Place).mkdirs())
                && (FileRetriever.getConnectionCategoryDirectory(context, worldName, category,
                        articleName, Category.Item).mkdirs())
                && (FileRetriever.getConnectionCategoryDirectory(context, worldName, category,
                        articleName, Category.Concept).mkdirs())
                && (FileRetriever.getSnippetsDirectory(context, worldName, category,
                        articleName).mkdirs())
                );

        if (successful) {
            if (category == Category.Person) {
                successful = ((FileRetriever.getMembershipsDirectory(
                                  context, worldName, articleName).mkdirs())
                              && (FileRetriever.getResidencesDirectory(
                                  context, worldName, articleName).mkdirs()));
            } else if (category == Category.Group) {
                successful = FileRetriever.getMembersDirectory(
                        context, worldName, articleName).mkdirs();
            } else if (category == Category.Place) {
                successful = FileRetriever.getResidentsDirectory(
                        context, worldName, articleName).mkdirs();
            }
        }

        // TODO: If any of the folders failed to be created, delete all other folders created during
        // the process.

        return successful;
    }

    /**
     * Saves a String to a text file within an Article's directory.
     * @param context The Context calling this method.
     * @param worldName The name of the world the Article belongs to.
     * @param category The name of the Category the Article belongs to.
     * @param articleName The name of the Article whose directory will possess the text file.
     * @param fileName The name of the text file that will be written to, with the file extension
     *                 omitted.
     * @param contents The String that will be saved in a text file.
     * @return True if the String was saved successfully; false if an I/O error occurs.
     */
    public static boolean writeStringToArticleFile(Context context, String worldName,
                                                   Category category, String articleName,
                                                   String fileName, String contents) {
        return writeStringToFile(FileRetriever.getArticleFile(context, worldName, category,
                articleName, fileName + ExternalReader.TEXT_FIELD_FILE_EXTENSION), contents);
    }

    /**
     * Saves a String message to a file.
     * @param textFile The File that will be written to.
     * @param contents The message to save to the file.
     * @return True if the String was saved successfully; false if an I/O error occurs.
     */
    private static boolean writeStringToFile(File textFile, String contents) {
        Boolean result = true;

        try {
            PrintWriter writer = new PrintWriter(textFile);
            writer.println(contents);
            writer.close();
        } catch (IOException error) {
            result = false;
        }

        return result;
    }

    /**
     * Saves an image to a specific Article's directory.
     * If the Article already has an image, it will be overwritten.
     * @param context The Context calling this method.
     * @param worldName The name of the world the Article belongs to.
     * @param category The name of the Category the Article belongs to.
     * @param articleName The name of the Article whose image will be saved.
     * @param imageUri The URI to the new image's original location.
     * @return True if the image was saved successfully; false otherwise.
     */
    public static boolean saveArticleImage(Context context, String worldName, Category category,
                                           String articleName, Uri imageUri) {
        Boolean result = true;

        String sourceFilename= imageUri.getPath();
        // The "." added at the beginning of the file name prevents this image from showing
        // up in the Gallery app.
        String destinationFilename = FileRetriever.getArticleFile(context, worldName, category,
                articleName, "." + context.getString(R.string.imageFileName)).getAbsolutePath();
        destinationFilename += ExternalReader.IMAGE_FILE_EXTENSION;

        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;

        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(sourceFilename));
            bufferedOutputStream = new BufferedOutputStream(
                    new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[IMAGE_BYTE_SIZE];
            bufferedInputStream.read(buf);
            do {
                bufferedOutputStream.write(buf);
            } while (bufferedInputStream.read(buf) != -1);
        } catch (IOException e) {
            result = false;
        } finally {
            try {
                if (bufferedInputStream != null) bufferedInputStream.close();
                if (bufferedOutputStream != null) bufferedOutputStream.close();
            } catch (IOException e) { }
        }

        return result;
    }

    /**
     * Looks in a specific Article's directory for an image file without a "." at the beginning
     * and, if found, prepends the "." to the beginning of the file name.
     *
     * <p>
     *     This is to correct an oversight where images stored without the preceding "." would
     *     show up in the Gallery app as individual albums, which clutters up the app considerably.
     * </p>
     *
     * @return True if an image file for the given Article matching the old file name format was
     *         found and renamed accordingly; false otherwise
     */
    public static boolean convertOldImageFilenameFormat(Context context, String worldName,
                                                 Category category, String articleName) {
        boolean imageWasRenamedSuccessfully = false;
        String imageFileName = context.getResources().getString(R.string.imageFileName)
                + ExternalReader.IMAGE_FILE_EXTENSION;
        File oldImageFile = FileRetriever.getArticleFile(context, worldName, category, articleName,
                imageFileName);

        if (oldImageFile.exists()) {
            String oldPathToFile = FileRetriever.getArticleDirectory(
                    context, worldName, category, articleName).getAbsolutePath();
            String newFilePath = oldPathToFile + "/." + imageFileName ;
            File newImageFile = new File(newFilePath);
            imageWasRenamedSuccessfully = oldImageFile.renameTo(newImageFile);
        }

        return imageWasRenamedSuccessfully;
    }

    /**
     * Saves the relation between an Article and one of its connected Articles.
     * @param context The Context calling this method.
     * @param worldName The name of the world the Article belongs to.
     * @param category The name of the Category the Article belongs to.
     * @param articleName The name of the Article who possesses the specified Connection.
     * @param connectedArticleCategory
     * @param connectedArticleName
     * @param relation A description of how the specified Article is related to the connected
     *                 Article.
     * @return True if the relation was saved successfully; false an I/O error occurs.
     */
    public static boolean saveConnectionRelation(Context context, String worldName,
                                                 Category category, String articleName,
                                                 Category connectedArticleCategory,
                                                 String connectedArticleName, String relation) {
        return writeStringToFile(FileRetriever.getConnectionRelationFile(context, worldName,
                category, articleName, connectedArticleCategory, connectedArticleName), relation);
    }

    /**
     * Saves a String as a Snippet's new contents.
     * @param context The Context calling this method.
     * @param worldName The name of the world the Article belongs to.
     * @param category The name of the Category the Article belongs to.
     * @param articleName The name of the Article who possesses the specified Snippet.
     * @param snippetName The name of the Snippet that will be written to.
     * @param contents The String that will be saved to the Snippet.
     * @return True if the String was saved successfully; false if an I/O error occurs.
     */
    public static boolean writeSnippetContents(Context context, String worldName, Category category,
                                               String articleName, String snippetName,
                                               String contents) {
        return writeStringToFile(FileRetriever.getSnippetFile(context, worldName, category,
                articleName, snippetName), contents);
    }

    /**
     * Renames an Article's Snippet.
     * @param context The Context calling this method.
     * @param worldName The name of the world the Article belongs to.
     * @param category The name of the Category the Article belongs to.
     * @param articleName The name of the Article who possesses the specified Snippet.
     * @param oldSnippetName The name of the Snippet that will be renamed.
     * @param newSnippetName The new name for the Snippet.
     * @return True if the Snippet was renamed successfully; false if an I/O error occurs.
     */
    public static boolean renameSnippet(Context context, String worldName, Category category,
                                               String articleName, String oldSnippetName,
                                               String newSnippetName) {
        File renamedFile = FileRetriever.getSnippetFile(context, worldName, category, articleName,
                newSnippetName);
        return FileRetriever.getSnippetFile(context, worldName, category, articleName,
                oldSnippetName).renameTo(renamedFile);
    }

    /**
     * Saves a Membership between a Person and a Group.
     * @param context The Context calling this method.
     * @param membership Contains data on the Membership that will be saved.
     * @return True if the Membership was successfully saved to both the Person's and the Group's
     * directories; false if an I/O error occurs.
     */
    public static boolean saveMembership(Context context, Membership membership) {
        File fileInMemberDirectory = FileRetriever.getMembershipFile(context, membership.worldName,
                membership.memberName, membership.groupName);
        File fileInGroupDirectory = FileRetriever.getMemberFile(context, membership.worldName,
                membership.groupName, membership.memberName);

        boolean savedToMemberDirectory = writeStringToFile(fileInMemberDirectory,
                membership.memberRole);
        boolean savedToGroupDirectory = writeStringToFile(fileInGroupDirectory,
                membership.memberRole);

        return ((savedToMemberDirectory) && (savedToGroupDirectory));
    }

    /**
     * Saves a Person's Residence within a Place.
     * @param context The Context calling this method.
     * @param residence The Residence data that will be saved to file.
     * @return True if the Residence data was saved successfully to both the Person's and the
     * Place's directories; false if an I/O error occurs.
     */
    public static boolean saveResidence(Context context, Residence residence) {
        File fileInPersonDirectory = FileRetriever.getResidenceFile(context, residence.worldName,
                residence.residentName, residence.placeName);
        File fileInPlaceDirectory = FileRetriever.getResidentFile(context, residence.worldName,
                residence.placeName, residence.residentName);
        boolean savedToPersonDirectory;
        boolean savedToPlaceDirectory;

        try {
            savedToPersonDirectory = fileInPersonDirectory.createNewFile();
        } catch (IOException error) {
            savedToPersonDirectory = false;
        }

        try {
            savedToPlaceDirectory = fileInPlaceDirectory.createNewFile();
        } catch (IOException error) {
            savedToPlaceDirectory = false;
        }

        return ((savedToPersonDirectory) && (savedToPlaceDirectory));
    }

    /**
     * Updates Connection files to reflect a rename for the "main" Article in a Connection.
     * @param context The The Context calling this method.
     * @param connection The Connection that will be updated.
     * @param newMainArticleName The new name for the main Article in the Connection.
     * @return True if the Connection was updated successfully; false otherwise.
     */
    public static boolean renameArticleInConnection(Context context, Connection connection,
                                                    String newMainArticleName) {
        File connectionRelationFile = FileRetriever.getConnectionRelationFile(context,
                connection.worldName, connection.connectedArticleCategory,
                connection.connectedArticleName, connection.articleCategory,
                connection.articleName);
        File renamedFile = FileRetriever.getConnectionRelationFile(context,
                connection.worldName, connection.connectedArticleCategory,
                connection.connectedArticleName, connection.articleCategory,
                newMainArticleName);
        return connectionRelationFile.renameTo(renamedFile);
    }

    /**
     * Updates Membership files to reflect a rename for the member in a Membership.
     * @param context The The Context calling this method.
     * @param membership The Membership that will be updated.
     * @param newMemberName The new name for the member in the Membership.
     * @return True if the Membership was updated successfully; false otherwise.
     */
    public static boolean renameMemberInMembership(Context context, Membership membership,
                                                   String newMemberName) {
        File memberFile = FileRetriever.getMemberFile(context,
                membership.worldName, membership.groupName, membership.memberName);
        File renamedFile = FileRetriever.getMemberFile(context,
                membership.worldName, membership.groupName, newMemberName);
        return memberFile.renameTo(renamedFile);
    }

    /**
     * Updates Membership files to reflect a rename for the Group in a Membership.
     * @param context The The Context calling this method.
     * @param membership The Membership that will be updated.
     * @param newGroupName The new name for the Group in the Membership.
     * @return True if the Membership was updated successfully; false otherwise.
     */
    public static boolean renameGroupInMembership(Context context, Membership membership,
                                                  String newGroupName) {
        File membershipFile = FileRetriever.getMembershipFile(context,
                membership.worldName, membership.memberName, membership.groupName);
        File renamedFile = FileRetriever.getMembershipFile(context,
                membership.worldName, membership.memberName, newGroupName);
        return membershipFile.renameTo(renamedFile);
    }

    /**
     * Updates Residence files to reflect a rename for the resident of a Residence.
     * @param context The The Context calling this method.
     * @param residence The Residence that will be updated.
     * @param newResidentName The new name for the resident in the Residence.
     * @return True if the Residence was updated successfully; false otherwise.
     */
    public static boolean renameResidentInResidence(Context context, Residence residence,
                                                    String newResidentName) {
        File residentFile = FileRetriever.getResidentFile(context,
                residence.worldName, residence.placeName, residence.residentName);
        File renamedFile = FileRetriever.getResidentFile(context,
                residence.worldName, residence.placeName, newResidentName);
        return residentFile.renameTo(renamedFile);
    }

    /**
     * Updates Residence files to reflect a rename for the Place of a Residence.
     * @param context The The Context calling this method.
     * @param residence The Residence that will be updated.
     * @param newResidentName The new name for the Place of Residence.
     * @return True if the Residence was updated successfully; false otherwise.
     */
    public static boolean renamePlaceInResidence(Context context, Residence residence,
                                                    String newResidentName) {
        File residenceFile = FileRetriever.getResidenceFile(context,
                residence.worldName, residence.residentName, residence.placeName);
        File renamedFile = FileRetriever.getResidenceFile(context,
                residence.worldName, residence.residentName, newResidentName);
        return residenceFile.renameTo(renamedFile);
    }

    /**
     * Renames the directory for a specific World.
     * @param currentWorldName The World's current name.
     * @param newWorldName The World's new name.
     * @return True if the directory was renamed successfully; false otherwise.
     */
    public static boolean renameWorldDirectory(String currentWorldName, String newWorldName) {
        File worldDirectory = FileRetriever.getWorldDirectory(currentWorldName);
        File renamedDirectory = FileRetriever.getWorldDirectory(newWorldName);
        return worldDirectory.renameTo(renamedDirectory);
    }

    /**
     * Renames the directory for a specific Article.
     * @param context The The Context calling this method.
     * @param worldName The name of The name of the world the Article belongs to.
     * @param category The name of the Category the Article belongs to.
     * @param articleName The name of the Article that will be renamed.
     * @param newArticleName The Article's new name.
     * @return True if the directory was renamed successfully; false otherwise.
     */
    public static boolean renameArticleDirectory(Context context, String worldName,
                                                 Category category, String articleName,
                                                 String newArticleName) {
        File articleDirectory = FileRetriever.getArticleDirectory(context, worldName, category,
                articleName);
        File renamedDirectory = FileRetriever.getArticleDirectory(context, worldName, category,
                newArticleName);
        return articleDirectory.renameTo(renamedDirectory);
    }

}
