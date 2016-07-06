package com.averi.worldscribe.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;
import com.averi.worldscribe.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by mark on 14/06/16.
 */
public class ExternalReader {

    public static final String TEXT_FIELD_FILE_EXTENSION = ".txt";
    public static final int TEXT_FILE_EXTENSION_LENGTH = 4;
    public static final String IMAGE_FILE_EXTENSION = ".jpg";

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

    /**
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param category The {@link Category} of the current Article.
     * @param articleName The name of the current Article.
     * @param viewWidth The width of the view that will display the Bitmap.
     * @param viewHeight The height of the view that will display the Bitmap.
     * @return The Article's image, scaled to fit the View as best as possible.
     */
    public static Bitmap getArticleImage(Context context, String worldName, Category category,
                                         String articleName, int viewWidth, int viewHeight) {
        File imageFile = FileRetriever.getArticleFile(context, worldName, category, articleName,
                context.getResources().getString(R.string.imageFileName) + IMAGE_FILE_EXTENSION);
        Bitmap articleBitmap = ImageDecoder.decodeBitmapFromFile(imageFile, viewWidth, viewHeight);

        // If the Article's image doesn't exist or can't be decoded, then return a default image
        // based on the Article's Category.
        if (articleBitmap == null) {
            // TODO: Create and set default images for all Categories.
            articleBitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.blank_person);
        }

        return articleBitmap;
    }

    /**
     * Given the name of a text field in the current Article, return the String data entered for
     * that field.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param category The {@link Category} of the current Article.
     * @param articleName The name of the current Article.
     * @param textFieldName The name of the text field from the current Article.
     * @return The data stored in the text field for this Article, if a file for that field exists
     * and it can be read; empty String otherwise.
     */
    public static String getArticleTextFieldData(Context context, String worldName,
            Category category, String articleName, String textFieldName) {
        StringBuilder textFieldData = new StringBuilder();
        File textFieldFile = FileRetriever.getArticleFile(context, worldName, category, articleName,
                textFieldName + TEXT_FIELD_FILE_EXTENSION);

        if (textFieldFile.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(textFieldFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                textFieldData.append(reader.readLine());
                while ((line = reader.readLine()) != null) {
                    textFieldData.append("\n");
                    textFieldData.append(line);
                }
            } catch (java.io.IOException e) {
                textFieldData.setLength(0);
            }
        } else {
            textFieldData.setLength(0);
        }

        return textFieldData.toString();
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
