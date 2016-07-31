package com.averi.worldscribe.utilities;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
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
        String destinationFilename = FileRetriever.getArticleFile(context, worldName, category,
                articleName, context.getString(R.string.imageFileName)).getAbsolutePath();
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
        return writeStringToFile(FileRetriever.getConnectionRelationFile( context, worldName,
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
}
