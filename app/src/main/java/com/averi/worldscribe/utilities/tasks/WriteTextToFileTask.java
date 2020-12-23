package com.averi.worldscribe.utilities.tasks;

import android.content.Context;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.WorldScribeApplication;
import com.averi.worldscribe.utilities.ExternalWriter;

import java.io.IOException;
import java.util.concurrent.Callable;

public class WriteTextToFileTask implements Callable<Void> {
    private final String filepath;
    private final String fileContents;

    /**
     * Instantiates a WriteTextToFileTask for writing a text string to an external .txt file.
     * If the file does not currently exist, it will be created.
     * @param filepath The path of the file to be written to
     * @param fileContents The text to write into the file
     */
    public WriteTextToFileTask(String filepath, String fileContents) {
        this.filepath = filepath;
        this.fileContents = fileContents;
    }

    @Override
    public Void call() throws IOException {
        DocumentFile textFile = TaskUtils.getFile(filepath, "text/plain");
        if (textFile == null) {
            throw new IOException("Could not access file at 'WorldScribe/" + filepath + "'");
        }

        Context context = WorldScribeApplication.getAppContext();
        if (!ExternalWriter.writeStringToFile(context, textFile, fileContents)) {
            throw new IOException("Failed to write text into 'WorldScribe/" + filepath + "'");
        }

        return null;
    }
}
