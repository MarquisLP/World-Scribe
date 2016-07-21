package com.averi.worldscribe.utilities;

import android.content.Context;
import android.os.Environment;

import com.averi.worldscribe.Category;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by mark on 08/06/16.
 */
public final class ExternalWriter {
//    public static boolean createAppDirectory() {
//        boolean directoryExists = true;
//
//        File appDirectory = new File(Environment.getExternalStorageDirectory(), APP_DIRECTORY_NAME);
//        Log.d("WorldScribe", appDirectory.getAbsolutePath());
//
//        if (externalStorageIsWritable()) {
//            if (!(appDirectory.exists())) {
//                directoryExists = appDirectory.mkdirs();
//            }
//        }
//
//        return directoryExists;
//    }

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
        Boolean result = true;

        try {
            PrintWriter writer = new PrintWriter(FileRetriever.getArticleFile(context, worldName,
                    category, articleName, fileName + ExternalReader.TEXT_FIELD_FILE_EXTENSION));
            writer.println(contents);
            writer.close();
        } catch (IOException error) {
            result = false;
        }

        return result;
    }
}
