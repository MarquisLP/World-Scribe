package com.averi.worldscribe.utilities.tasks;

import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;
import java.util.concurrent.Callable;

public class RenameWorldTask implements Callable<Void> {
    private final String currentWorldName;
    private final String newWorldName;

    public RenameWorldTask(String currentWorldName, String newWorldName) {
        this.currentWorldName = currentWorldName;
        this.newWorldName = newWorldName;
    }

    @Override
    public Void call() throws IOException {
        DocumentFile worldFolder = TaskUtils.getFolder(currentWorldName, false);
        if (worldFolder == null) {
            throw new IOException("Could not access folder at 'WorldScribe/" + currentWorldName + "'");
        }

        if (!(worldFolder.renameTo(newWorldName))) {
            throw new IOException("Failed to rename 'WorldScribe/" + currentWorldName + "' to + 'WorldScribe/" + newWorldName + "'");
        }

        return null;
    }
}
