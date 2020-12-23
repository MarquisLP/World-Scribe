package com.averi.worldscribe.utilities.tasks;

import android.content.Context;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.Connection;
import com.averi.worldscribe.WorldScribeApplication;
import com.averi.worldscribe.utilities.ExternalWriter;

import java.io.IOException;
import java.util.concurrent.Callable;

public class SaveConnectionTask implements Callable<Void> {
    private final Connection connection;

    /**
     * Instantiates a SaveConnectionTask for saving a Connection to external storage.
     * @param connection The Connection to save
     */
    public SaveConnectionTask(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Void call() throws IOException {
        Context context = WorldScribeApplication.getAppContext();

        String mainArticleConnectionFilepath = connection.worldName + "/"
                + connection.articleCategory.pluralName(context) + "/"
                + connection.articleName + "/Connections/"
                + connection.connectedArticleCategory.pluralName(context) + "/"
                + connection.connectedArticleName + ".txt";
        DocumentFile mainArticleConnectionFile = TaskUtils.getFile(mainArticleConnectionFilepath,
                "text/plain");
        if (!(ExternalWriter.writeStringToFile(context, mainArticleConnectionFile, connection.articleRelation))) {
            throw new IOException("Could not write to file 'WorldScribe/" + mainArticleConnectionFilepath + "'");
        }

        String connectedArticleConnectionFilepath = connection.worldName + "/"
                + connection.connectedArticleCategory.pluralName(context) + "/"
                + connection.connectedArticleName + "/Connections/"
                + connection.articleCategory.pluralName(context) + "/"
                + connection.articleName + ".txt";
        DocumentFile connectedArticleConnectionFile = TaskUtils.getFile(connectedArticleConnectionFilepath,
                "text/plain");
        if (!(ExternalWriter.writeStringToFile(context, connectedArticleConnectionFile, connection.connectedArticleRelation))) {
            throw new IOException("Could not write to file 'WorldScribe/" + connectedArticleConnectionFilepath + "'");
        }

        return null;
    }
}
