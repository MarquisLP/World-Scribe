package tools;

import android.content.Context;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by mark on 14/06/16.
 */
public class ExternalReader {

    public static final int TEXT_FILE_EXTENSION_LENGTH = 4;

    public static ArrayList<String> getWorldList() {
        ArrayList<String> worldNames = new ArrayList<>();
        File worldsFolder = FileRetriever.getAppDirectory();
        File[] listOfFiles = worldsFolder.listFiles();

        for (File file : listOfFiles) {
            if (file.isDirectory()) {
                worldNames.add(file.getName());
            }
        }

        Collections.sort(worldNames, String.CASE_INSENSITIVE_ORDER);

        return worldNames;
    }

    public static boolean worldListIsEmpty() {
        ArrayList<String> worldList = getWorldList();
        return worldList.isEmpty();
    }

    public static boolean worldAlreadyExists(String worldName) {
        File worldsFolder = FileRetriever.getAppDirectory();
        return (new File(worldsFolder, worldName).exists());
    }

    public static ArrayList<String> getArticleNamesInCategory(Context context, String worldName,
                                                              Category category) {
        ArrayList<String> articleNames = new ArrayList<String>();
        File categoryFolder = FileRetriever.getCategoryDirectory(context, worldName, category);
        File[] listOfArticles = categoryFolder.listFiles();

        for (File articleFolder : listOfArticles) {
            if (articleFolder.isDirectory()) {
                articleNames.add(articleFolder.getName());
            }
        }

        Collections.sort(articleNames, String.CASE_INSENSITIVE_ORDER);

        return articleNames;
    }

    public static ArrayList<Connection> getConnections(Context context, String worldName,
                                                       Category category, String articleName) {
        ArrayList<Connection> connections = new ArrayList<>();

        for (Category connectionCategory : Category.values()) {
            connections.addAll(getConnectionsInCategory(context, worldName, category, articleName,
                    connectionCategory));
        }

        return connections;
    }

    private static ArrayList<Connection> getConnectionsInCategory(Context context, String worldName,
            Category category, String articleName, Category connectionCategory) {
        ArrayList<Connection> connections = new ArrayList<>();
        File connectionCategoryFolder = FileRetriever.getConnectionCategoryDirectory(context,
                worldName, category, articleName, connectionCategory);

        for (File connectionFile : connectionCategoryFolder.listFiles()) {
            Connection connection = makeConnectionFromFile(connectionCategory, connectionFile);
            if (connection != null) {
                connections.add(connection);
            }
        }

        return connections;
    }

    private static Connection makeConnectionFromFile(Category connectionCategory,
                                                     File connectionFile) {
        Connection connection;

        try {
            FileInputStream inputStream = new FileInputStream(connectionFile);
            connection = new Connection();

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                connection.articleRole = line;
            }

            connection.connectedArticleCategory = connectionCategory;

            String connectionFileName = connectionFile.getName();
            connection.connectedArticleName = connectionFileName.substring(0,
                    connectionFileName.length() - TEXT_FILE_EXTENSION_LENGTH);

            inputStream.close();
        }
        catch (java.io.IOException e) {
            connection = null;
        }

        return connection;
    }

}
