package com.averi.worldscribe.utilities.tasks;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.WorldScribeApplication;
import com.averi.worldscribe.utilities.AppPreferences;
import com.balda.flipper.DocumentFileCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

class TaskUtils {
    /**
     * Gets a {@link DocumentFile} instance for the file at the given path.
     * @param filepath The path of the file to retrieve. This filepath
     i*                is relative to the app folder.
     *                 For example, "World1/People/Person1/Age.txt" would retrieve the folder
     *                 at "/storage/emulated/0/WorldScribe/World1/People/Person1/Age.txt".
     * @param mimeType If non-null, then if the file and/or its ancestor folders don't
     *                 currently exist, they will be created and the file will be given
     *                 this MIME-type, for example, "text/plain".
     * @return A DocumentFile instance for the file at filepath, or null
     *         if no such file could be accessed
     * @throws FileNotFoundException if either the file root directory or the app folder
     *                               cannot be accessed
     */
    public static DocumentFile getFile(String filepath, @Nullable String mimeType) throws FileNotFoundException {
        Context context = WorldScribeApplication.getAppContext();
        String rootUriString = context.getSharedPreferences("com.averi.worldscribe", Context.MODE_PRIVATE)
                .getString(AppPreferences.ROOT_DIRECTORY_URI, null);
        Uri rootUri = Uri.parse(rootUriString);

        DocumentFile fileRoot;
        if (rootUriString.startsWith("file")) {
            File rootFile = new File(rootUri.getPath());
            fileRoot = DocumentFile.fromFile(rootFile);
        }
        else {
            fileRoot = DocumentFile.fromTreeUri(context, rootUri);
        }

        if (fileRoot == null) {
            throw new FileNotFoundException("Failed to read root file directory.");
        }

        // Get the app's root folder.
        DocumentFile appFolder = DocumentFileCompat.peekSubFolder( fileRoot, "WorldScribe");
        if (appFolder == null) {
            throw new FileNotFoundException("Failed to read 'WorldScribe' directory.");
        }

        DocumentFile fileToReturn = null;
        String[] pathTokens = filepath.split("/");
        int currentPathTokenIndex = 0;
        DocumentFile currentFile = appFolder;
        while ((currentFile != null) && (currentPathTokenIndex < pathTokens.length)) {
            if (currentPathTokenIndex == pathTokens.length - 1) {
                // If we're on the last part of the filepath, that means we're done with folders
                // and are now accessing the file.
                if (mimeType != null) {
                    currentFile = DocumentFileCompat.getFile(currentFile, pathTokens[currentPathTokenIndex], mimeType);
                }
                else {
                    currentFile = DocumentFileCompat.peekFile(currentFile, pathTokens[currentPathTokenIndex], null);
                }
            }
            else {
                if (mimeType != null) {
                    currentFile = DocumentFileCompat.getSubFolder(currentFile, pathTokens[currentPathTokenIndex]);
                }
                else {
                    currentFile = DocumentFileCompat.peekSubFolder(currentFile, pathTokens[currentPathTokenIndex]);
                }
            }
            currentPathTokenIndex++;
        }
        fileToReturn = currentFile;

        return fileToReturn;
    }

    /**
     * Gets a {@link DocumentFile} instance for the folder at the given path.
     * @param filepath The path of the folder to retrieve. This filepath
    i*                 is relative to the app folder.
     *                 For example, "World1/People" would retrieve the folder
     *                 at "/storage/emulated/0/WorldScribe/World1/People".
     *                 Passing in "/" or "" (the empty string) will return
     *                 the app's root folder.
     * @param createIfNonexistent If true, then if the folder and/or its ancestor folders
     *                            don't currently exist, they will be created.
     * @return A DocumentFile instance for the folder at filepath, or null
     *         if no such folder could be accessed
     * @throws FileNotFoundException if either the file root directory or the app folder
     *                               cannot be accessed
     */
    public static DocumentFile getFolder(String filepath, boolean createIfNonexistent) throws FileNotFoundException {
        Context context = WorldScribeApplication.getAppContext();
        String rootUriString = context.getSharedPreferences("com.averi.worldscribe", Context.MODE_PRIVATE)
                .getString(AppPreferences.ROOT_DIRECTORY_URI, null);
        Uri rootUri = Uri.parse(rootUriString);

        DocumentFile fileRoot;
        if (rootUriString.startsWith("file")) {
            File rootFile = new File(rootUri.getPath());
            fileRoot = DocumentFile.fromFile(rootFile);
        }
        else {
            fileRoot = DocumentFile.fromTreeUri(context, rootUri);
        }

        if (fileRoot == null) {
            throw new FileNotFoundException("Failed to read root file directory.");
        }

        // Get the app's root folder.
        DocumentFile appFolder = DocumentFileCompat.peekSubFolder( fileRoot, "WorldScribe");
        if (appFolder == null) {
            throw new FileNotFoundException("Failed to read 'WorldScribe' directory.");
        }

        if ((filepath.isEmpty()) || ("/".equals(filepath))) {
            return appFolder;
        }
        else {
            List<String> folderNames = Arrays.asList(filepath.split("/"));
            if (createIfNonexistent) {
                return DocumentFileCompat.getSubFolderTraverse(appFolder, folderNames);
            }
            else {
                return DocumentFileCompat.peekSubFolderTraverse(appFolder, folderNames);
            }
        }
    }

    /**
     * Retrieve the contents of a text file.
     * @param file The file whose contents will be retrieved.
     * @return The contents of the file.
     * @throws IOException if the file could not be accessed or read.
     */
    public static String readFileContents(DocumentFile file) throws IOException {
        String contents = "";

        Context context = WorldScribeApplication.getAppContext();
        InputStream inputStream = context.getContentResolver().openInputStream(file.getUri());
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = reader.readLine()) != null) {
            contents = line;
        }
        inputStream.close();

        return contents;
    }

    /**
     * Strips the file extension from the end of a filepath string.
     * @param filepath The filepath string to strip
     * @return The given filepath without the file extension at the end
     */
    public static String stripFileExtension(String filepath) {
        int lastDotIndex = filepath.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return filepath;
        }
        else {
            return filepath.substring(0, lastDotIndex);
        }
    }
}
