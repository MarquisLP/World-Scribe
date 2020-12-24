package com.averi.worldscribe.utilities.tasks;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.utilities.ExternalDeleter;

import java.io.IOException;
import java.util.concurrent.Callable;

public class DeleteWorldTask implements Callable<Void> {
    private final String worldName;

    /**
     * Instantiates a DeleteWorldTask for deleting a World's folder.
     * @param worldName The name of the World to delete
     */
    public DeleteWorldTask(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public Void call() throws IOException {
        DocumentFile worldFolder = TaskUtils.getFolder(worldName, false);
        if (worldFolder == null) {
            throw new IOException("Could not access folder at 'WorldScribe/" + worldName + "'");
        }

        if (!ExternalDeleter.deleteRecursive(worldFolder)) {
            throw new IOException("Failed to delete folder at 'WorldScribe/" + worldName + "'");
        }

        return null;
    }
}
