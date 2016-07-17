package com.averi.worldscribe.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;
import com.averi.worldscribe.Member;
import com.averi.worldscribe.Membership;
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

    /**
     * Retrieves the names of all Snippets belonging to the specified Article.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param category The {@link Category} of the current Article.
     * @param articleName The name of the current Article.
     * @return An ArrayList of all of the Article's Snippets' names.
     */
    public static ArrayList<String> getSnippetNames(Context context, String worldName,
                                                    Category category, String articleName) {
        File snippetsDirectory = FileRetriever.getSnippetsDirectory(context, worldName, category,
                articleName);
        return getSortedFileNames(snippetsDirectory);
    }

    /**
     * Get the names of all files in a given directory as a sorted list of Strings.
     * @param folder A File referencing the specified directory to read.
     * @return An ArrayList containing all filenames in the specified directory, sorted in
     * descending order.
     */
    private static ArrayList<String> getSortedFileNames(File folder) {
        ArrayList<String> fileNames = new ArrayList<>();
        File[] listOfFiles = folder.listFiles();

        for (File snippetFile : listOfFiles) {
            if (snippetFile.isFile()) {
                String snippetName = snippetFile.getName();
                fileNames.add(snippetName.substring(0,
                        snippetName.length() - TEXT_FILE_EXTENSION_LENGTH));
            }
        }

        Collections.sort(fileNames, String.CASE_INSENSITIVE_ORDER);

        return fileNames;
    }

    /**
     * Get the text stored in an Article's Snippet.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param category The {@link Category} of Article the Snippet belongs to.
     * @param articleName The name of the Article the Snippet belongs to.
     * @param snippetName The name of the Snippet being loaded.
     * @return The contents of the specified Snippet if the Snippet's file exists and can be read;
     * empty string otherwise.
     */
    public static String getSnippetText(Context context, String worldName, Category category,
                                 String articleName, String snippetName) {
        StringBuilder snippetData = new StringBuilder();
        File snippetFile = FileRetriever.getSnippetFile(context, worldName, category, articleName,
                snippetName);

        if (snippetFile.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(snippetFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                snippetData.append(reader.readLine());
                while ((line = reader.readLine()) != null) {
                    snippetData.append("\n");
                    snippetData.append(line);
                }
            } catch (java.io.IOException e) {
                snippetData.setLength(0);
            }
        } else {
            snippetData.setLength(0);
        }

        return snippetData.toString();
    }

    /**
     * Retrieves the names of all Residences for a specified Person.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param personName The name of the Person whose Residences are being retrieved.
     * @return An ArrayList of all of the Article's Residences' names.
     */
    public static ArrayList<String> getResidences(Context context, String worldName,
                                                  String personName) {
        File residencesDirectory = FileRetriever.getResidencesDirectory(context, worldName,
                personName);
        return getSortedFileNames(residencesDirectory);
    }

    /**
     * Retrieves the names of all Residents for a specified Place.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param placeName The name of the Place whose Residents are being retrieved.
     * @return An ArrayList of all of the Places' Residents' names.
     */
    public static ArrayList<String> getResidents(Context context, String worldName,
                                                  String placeName) {
        File residentsDirectory = FileRetriever.getResidentsDirectory(context, worldName,
                placeName);
        return getSortedFileNames(residentsDirectory);
    }

    /**
     * Retrieves all Memberships for a specified Person.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param personName The name of the Person whose Memberships are being retrieved.
     * @return An ArrayList of all Memberships belonging to the specified Person.
     */
    public static ArrayList<Membership> getMemberships(Context context, String worldName,
                                                       String personName) {
        ArrayList<Membership> memberships = new ArrayList<>();
        File membershipsDirectory = FileRetriever.getMembershipsDirectory(context, worldName,
                personName);

        for (File membershipFile : membershipsDirectory.listFiles()) {
            if (membershipFile.isFile()) {
                Membership membership = makeMembershipFromFile(membershipFile);
                if (membership != null) {
                    memberships.add(membership);
                }
            }
        }

        return memberships;
    }

    /**
     * Return a Membership containing data from the specified Membership file.
     * @param membershipFile A text file containing Membership data.
     * @return A Membership with the extracted data; null if an I/O error occurs.
     */
    private static Membership makeMembershipFromFile(File membershipFile) {
        Membership membership;

        try {
            FileInputStream inputStream = new FileInputStream(membershipFile);
            membership = new Membership();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            membership.memberRole = reader.readLine();

            String groupName = membershipFile.getName();
            membership.groupName = groupName.substring(0,
                    groupName.length() - TEXT_FILE_EXTENSION_LENGTH);

            inputStream.close();
        }
        catch (java.io.IOException e) {
            membership = null;
        }

        return membership;
    }

    /**
     * Retrieves all Members of a specified Group.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param groupName The name of the Group whose Members are being retrieved.
     * @return An ArrayList containing all Members belonging to the specified Group; may be empty
     * if the Group has no Members.
     */
    public static ArrayList<Member> getMembers(Context context, String worldName,
                                               String groupName) {
        ArrayList<Member> members = new ArrayList<>();
        File membersDirectory = FileRetriever.getMembersDirectory(context, worldName,
                groupName);

        for (File memberFile : membersDirectory.listFiles()) {
            if (memberFile.isFile()) {
                Member member = makeMemberFromFile(memberFile);
                if (member != null) {
                    members.add(member);
                }
            }
        }

        return members;
    }

    /**
     * Return a Member containing data from the specified Member file.
     * @param memberFile A text file containing Member data.
     * @return A Member with the extracted data; null if an I/O error occurs.
     */
    private static Member makeMemberFromFile(File memberFile) {
        Member member;

        try {
            FileInputStream inputStream = new FileInputStream(memberFile);
            member = new Member();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            member.memberRole = reader.readLine();

            String memberName = memberFile.getName();
            member.memberName = memberName.substring(0,
                    memberName.length() - TEXT_FILE_EXTENSION_LENGTH);

            inputStream.close();
        }
        catch (java.io.IOException e) {
            member = null;
        }

        return member;
    }

}
