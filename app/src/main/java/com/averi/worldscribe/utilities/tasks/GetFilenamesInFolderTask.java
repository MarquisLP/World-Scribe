package com.averi.worldscribe.utilities.tasks;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.WorldScribeApplication;
import com.averi.worldscribe.utilities.AppPreferences;
import com.balda.flipper.DocumentFileCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class GetFilenamesInFolderTask implements Callable<ArrayList<String>> {
    private final String folderPath;
    private final boolean createNonexistentFolders;

    /**
     * Instantiates a GetFilenamesInFolderTask for retrieving the names of
     * all files and folders within a parent folder.
     * If any folders on the given path cannot be found, a {@link FileNotFoundException}
     * is thrown.
     * @param folderPath The filepath of the folder whose contents will be
     *                   read. This filepath is relative to the app folder.
     *                   For example, "World1/People" would retrieve the contents
     *                   of "/storage/emulated/0/WorldScribe/World1/People".
     *                   Passing in "/" or "" (the empty string) will return the
     *                   contents of the app's root folder.
     */
    public GetFilenamesInFolderTask(String folderPath) {
        this.folderPath = folderPath;
        this.createNonexistentFolders = false;
    }

    /**
     * Instantiates a GetFilenamesInFolderTask for retrieving the names of
     * all files and folders within a parent folder.
     * @param folderPath The filepath of the folder whose contents will be
     *                   read. This filepath is relative to the app folder.
     *                   For example, "World1/People" would retrieve the contents
     *                   of "/storage/emulated/0/WorldScribe/World1/People".
     *                   Passing in "/" or "" (the empty string) will return the
     *                   contents of the app's root folder.
     * @param createNonexistentFolders If true, the folder and its ancestor folders
     *                                 will be created if they don't already exist.
     *                                 If false, a {@link FileNotFoundException}
     *                                 will be thrown if any folders in the path
     *                                 cannot be found.
     */
    public GetFilenamesInFolderTask(String folderPath, boolean createNonexistentFolders) {
        this.folderPath = folderPath;
        this.createNonexistentFolders = createNonexistentFolders;
    }

    @Override
    public ArrayList<String> call() throws FileNotFoundException {
        DocumentFile folder = TaskUtils.getFolder(folderPath, createNonexistentFolders);
        if (folder == null) {
            throw new FileNotFoundException("Could not access folder at 'WorldScribe/"  + folderPath + "'");
        }

        // Create a list of names from the files.
        ArrayList<String> filenames = new ArrayList<>();
        for ( DocumentFile file : folder.listFiles() ) {
            String filename = file.getName();
            int lastDotIndex = filename.lastIndexOf(".");
            if (lastDotIndex == -1) {
                filenames.add(filename);
            }
            else {
                // Don't include the file extension.
                filenames.add(filename.substring(0, lastDotIndex));
            }
        }

        return filenames;
    }
}
