package com.averi.worldscribe.utilities.tasks;

import android.content.Context;

import com.averi.worldscribe.WorldScribeApplication;
import com.averi.worldscribe.utilities.ExternalWriter;

import java.io.IOException;
import java.util.concurrent.Callable;

public class CreateWorldTask implements Callable<Void> {
    private final String worldName;

    /**
     * Instantiates a CreateWorldTask for creating a new World's folder and subfolders.
     * @param worldName The name of the World to create
     */
    public CreateWorldTask(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public Void call() throws IOException {
        Context context = WorldScribeApplication.getAppContext();

        if (ExternalWriter.createWorldDirectory(context, worldName) == null) {
            throw new IOException("Failed to create folder at 'WorldScribe" + worldName + "'");
        }

        return null;
    }
}
