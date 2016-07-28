package com.averi.worldscribe.utilities;

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
}
