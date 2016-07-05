package com.averi.worldscribe.utilities;

import android.content.Context;
import android.os.Environment;

import com.averi.worldscribe.Category;

import java.io.File;

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
}
