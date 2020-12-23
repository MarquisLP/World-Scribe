
package com.averi.worldscribe.utilities.tasks;

import android.content.Context;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;
import com.averi.worldscribe.WorldScribeApplication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class GetConnectionsTask implements Callable<ArrayList<Connection>> {
    private final String worldName;
    private final Category mainArticleCategory;
    private final String mainArticleName;

    /**
     * Instantiates a {@link GetConnectionsTask} for retrieving the
     * {@link Connection}s of an Article.
     */
    public GetConnectionsTask(String worldName, Category mainArticleCategory, String mainArticleName) {
        this.worldName = worldName;
        this.mainArticleCategory = mainArticleCategory;
        this.mainArticleName = mainArticleName;
    }

    @Override
    public ArrayList<Connection> call() throws IOException {
        ArrayList<Connection> connections = new ArrayList<>();

        Context context = WorldScribeApplication.getAppContext();
        String connectionsPath = worldName + "/" + mainArticleCategory.pluralName(context) + "/"
                + mainArticleName + "/" + "Connections";
        for (Category currentCategory : Category.values()) {
            String connectionsCategoryPath = connectionsPath + "/" + currentCategory.pluralName(context);

            DocumentFile connectionsCategoryFolder = TaskUtils.getFolder(connectionsCategoryPath, true);
            if (connectionsCategoryFolder == null) {
                throw new FileNotFoundException("Could not access folder at 'WorldScribe/"  + connectionsCategoryPath + "'");
            }

            for (DocumentFile connectedArticleFile : connectionsCategoryFolder.listFiles()) {
                String connectedArticleName = connectedArticleFile.getName();
                if (connectedArticleName == null) {
                    throw new IOException("An error occurred while retrieving the name of a connection file");
                }
                connectedArticleName = TaskUtils.stripFileExtension(connectedArticleName);
                String mainConnectionFileContents = TaskUtils.readFileContents(connectedArticleFile);
                String otherConnectionFilepath = worldName + "/" + currentCategory.pluralName(context) + "/"
                        + connectedArticleName + "/Connections/" + mainArticleCategory.pluralName(context)
                        + "/" + mainArticleName + ".txt";
                DocumentFile otherConnectionFile = TaskUtils.getFile(otherConnectionFilepath, null);
                if (otherConnectionFile == null) {
                    Log.d("WorldScribe", otherConnectionFilepath);
                    throw new FileNotFoundException("Could not access file at 'WorldScribe/" + otherConnectionFilepath + "'");
                }
                String otherConnectionFileContents = TaskUtils.readFileContents(otherConnectionFile);

                Connection connection = new Connection();
                connection.worldName = worldName;
                connection.articleCategory = mainArticleCategory;
                connection.articleName = mainArticleName;
                connection.articleRelation = mainConnectionFileContents;
                connection.connectedArticleCategory = currentCategory;
                connection.connectedArticleName = connectedArticleName;
                connection.connectedArticleRelation = otherConnectionFileContents;
                connections.add(connection);
            }
        }

        return connections;
    }
}
