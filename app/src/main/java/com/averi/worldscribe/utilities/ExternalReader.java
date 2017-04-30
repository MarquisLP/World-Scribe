package com.averi.worldscribe.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;
import com.averi.worldscribe.Membership;
import com.averi.worldscribe.R;
import com.averi.worldscribe.Residence;

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

    /**
     * @return True if the app's directory exists on the user's external storage.
     */
    public static boolean appDirectoryExists() {
        return FileRetriever.getAppDirectory().exists();
    }

    public static ArrayList<String> getWorldList() {
        ArrayList<String> worldNames = new ArrayList<>();
        File worldsFolder = FileRetriever.getAppDirectory();
        worldsFolder.mkdirs();
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
        categoryFolder.mkdirs();
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
     * Checks if a certain Article already exists.
     * @param context The Context calling this method.
     * @param worldName The name of the World that the Article belongs to.
     * @param category The {@link Category} that the Article belongs to.
     * @param articleName The name of the Article to check.
     * @return True if the Article exists with a directory in the World folder; false otherwise.
     */
    public static boolean articleExists(Context context, String worldName, Category category,
                                        String articleName) {
        File articleDirectory = FileRetriever.getArticleDirectory(context, worldName, category,
                articleName);
        return ((articleDirectory.exists()) && (articleDirectory.isDirectory()));
    }

    /**
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param category The {@link Category} of the current Article.
     * @param articleName The name of the current Article.
     * @param viewWidth The width of the view that will display the Bitmap.
     * @param viewHeight The height of the view that will display the Bitmap.
     * @return The Article's image, scaled to fit the View as best as possible; null if the image
     * doesn't exist or couldn't be loaded.
     */
    public static Bitmap getArticleImage(Context context, String worldName, Category category,
                                         String articleName, int viewWidth, int viewHeight) {
        File imageFile = FileRetriever.getArticleFile(context, worldName, category, articleName,
                "." + context.getResources().getString(R.string.imageFileName) +
                        IMAGE_FILE_EXTENSION);
        Bitmap articleBitmap = ImageDecoder.decodeBitmapFromFile(imageFile, viewWidth, viewHeight);
        return articleBitmap;
    }

    /**
     * Gets the Bitmap for an unset Article image, based on the Article's Category.
     * @param context The Context calling this method.
     * @param category The Article's Category.
     * @return
     */
    public static Bitmap getUnsetImageBitmap(Context context, Category category) {
        int unsetImageID;
        switch (category) {
            case Person:
                unsetImageID = R.drawable.blank_person;
                break;
            case Group:
                unsetImageID = R.drawable.unset_image_group;
                break;
            case Place:
                unsetImageID = R.drawable.unset_image_place;
                break;
            case Item:
                unsetImageID = R.drawable.unset_image_item;
                break;
            case Concept:
            default:
                unsetImageID = R.drawable.unset_image_concept;
        }

        return BitmapFactory.decodeResource(context.getResources(), unsetImageID);
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

    /**
     * Gets the relation an Article has to one of its connected Articles.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param category The {@link Category} of the current Article.
     * @param articleName The name of the current Article.
     * @param connectedArticleName The name of the Article connected to the current Article.
     * @return A description of the main Article's relation to the connected Article.
     */
    public static String getConnectionRelation(Context context, String worldName, Category category,
                                           String articleName, String connectedArticleName) {
        return "";
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
        connectionCategoryFolder.mkdirs();

        for (File mainArticleRelationFile : connectionCategoryFolder.listFiles()) {
            String mainArticleRelationFilename = mainArticleRelationFile.getName();
            String connectedArticleName = mainArticleRelationFilename.substring(0,
                    mainArticleRelationFilename.length() - TEXT_FILE_EXTENSION_LENGTH);
            File connectedArticleRelationFile = FileRetriever.getConnectionRelationFile(context,
                    worldName, connectionCategory, connectedArticleName, category, articleName);

            Connection connection = makeConnectionFromFile(worldName, category, articleName,
                    mainArticleRelationFile, connectionCategory, connectedArticleName,
                    connectedArticleRelationFile);

            if (connection != null) {
                connections.add(connection);
            }
        }

        return connections;
    }

    private static Connection makeConnectionFromFile(String worldName, Category mainArticleCategory,
                                                     String mainArticleName,
                                                     File mainArticleRelationFile,
                                                     Category connectedArticleCategory,
                                                     String connectedArticleName,
                                                     File connectedArticleRelationFile) {
        Connection connection = new Connection();
        connection.worldName = worldName;
        connection.articleCategory = mainArticleCategory;
        connection.articleName = mainArticleName;
        connection.connectedArticleCategory = connectedArticleCategory;
        connection.connectedArticleName = connectedArticleName;

        try {
            FileInputStream inputStream = new FileInputStream(mainArticleRelationFile);
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                connection.articleRelation = line;
            }
            inputStream.close();
        }
        catch (java.io.IOException e) {
            connection = null;
        }

        if (connection != null) {
            try {
                FileInputStream inputStream = new FileInputStream(connectedArticleRelationFile);
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = reader.readLine()) != null) {
                    connection.connectedArticleRelation = line;
                }
                inputStream.close();
            }
            catch (java.io.IOException e) {
                connection = null;
            }
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
        snippetsDirectory.mkdirs();

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
     * Checks if a certain Snippet already exists.
     * @param context The Context calling this method.
     * @param worldName The name of the World that the Article belongs to.
     * @param category The {@link Category} that the Article belongs to.
     * @param articleName The name of the Article that the Snippet belongs to.
     * @param snippetName The name of the Snippet to check.
     * @return True if the Snippet exists within its respective Article's directory; false
     * otherwise.
     */
    public static boolean snippetExists(Context context, String worldName, Category category,
                                        String articleName, String snippetName) {
        File snippetFile = FileRetriever.getSnippetFile(context, worldName, category, articleName,
                snippetName);
        return ((snippetFile.exists()) && (snippetFile.isFile()));
    }

    /**
     * Retrieves all {@link com.averi.worldscribe.Residence Residences} for a specified Person.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param personName The name of the Person whose Residences are being retrieved.
     * @return An ArrayList of all of the Article's Residences.
     */
    public static ArrayList<Residence> getResidences(Context context, String worldName,
                                                     String personName) {
        File residencesDirectory = FileRetriever.getResidencesDirectory(context, worldName,
                personName);
        residencesDirectory.mkdirs();
        ArrayList<Residence> residences = new ArrayList<>();

        for (File residenceFile : residencesDirectory.listFiles()) {
            if (residenceFile.isFile()) {
                Residence newResidence = new Residence();
                String residenceFilename = residenceFile.getName();

                newResidence.worldName = worldName;
                newResidence.residentName = personName;
                newResidence.placeName = residenceFilename.substring(0,
                        residenceFilename.length() - TEXT_FILE_EXTENSION_LENGTH);

                residences.add(newResidence);
            }
        }

        return residences;
    }

    /**
     * Retrieves Residence data for all residents of a specified Place.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param placeName The name of the Place whose Resident data is being retrieved.
     * @return An ArrayList of all of Residences within the specified Place.
     */
    public static ArrayList<Residence> getResidents(Context context, String worldName,
                                                  String placeName) {
        File residentsDirectory = FileRetriever.getResidentsDirectory(context, worldName,
                placeName);
        residentsDirectory.mkdirs();
        ArrayList<Residence> residences = new ArrayList<>();

        for (File residentFile : residentsDirectory.listFiles()) {
            if (residentFile.isFile()) {
                Residence newResidence = new Residence();
                String residentFilename = residentFile.getName();

                newResidence.worldName = worldName;
                newResidence.placeName = placeName;
                newResidence.residentName = residentFilename.substring(0,
                        residentFilename.length() - TEXT_FILE_EXTENSION_LENGTH);

                residences.add(newResidence);
            }
        }

        return residences;
    }

    /**
     * Retrieves all Memberships for a specified Person.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param personName The name of the Person whose Memberships are being retrieved.
     * @return An ArrayList of all Memberships belonging to the specified Person.
     */
    public static ArrayList<Membership> getMembershipsForPerson(Context context, String worldName,
                                                                String personName) {
        ArrayList<Membership> memberships = new ArrayList<>();
        File membershipsDirectory = FileRetriever.getMembershipsDirectory(context, worldName,
                personName);
        membershipsDirectory.mkdirs();

        for (File membershipFile : membershipsDirectory.listFiles()) {
            if (membershipFile.isFile()) {
                String membershipFilename = membershipFile.getName();
                String groupName = membershipFilename.substring(0,
                        membershipFilename.length() - TEXT_FILE_EXTENSION_LENGTH);

                Membership membership = makeMembershipFromFile(worldName, groupName, personName,
                        membershipFile);
                if (membership != null) {
                    memberships.add(membership);
                }
            }
        }

        return memberships;
    }

    /**
     * Return a Membership containing data from the specified Membership file.
     * @param worldName The name of the World where the Group and its members reside.
     * @param groupName The name of the Group in the Membership.
     * @param memberName The name of the Person in the Membership.
     * @param membershipFile A text file containing Membership data.
     * @return A Membership with the extracted data; null if an I/O error occurs.
     */
    private static Membership makeMembershipFromFile(String worldName, String groupName,
                                                     String memberName,
                                                     File membershipFile) {
        Membership membership = new Membership();
        membership.worldName = worldName;
        membership.groupName = groupName;
        membership.memberName = memberName;

        try {
            FileInputStream inputStream = new FileInputStream(membershipFile);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            membership.memberRole = reader.readLine();

            inputStream.close();
        }
        catch (java.io.IOException e) {
            membership = null;
        }

        return membership;
    }

    /**
     * Retrieves all Memberships to a specified Group.
     * @param context The Context calling this method.
     * @param worldName The name of the current World.
     * @param groupName The name of the Group whose Memberships are being retrieved.
     * @return An ArrayList containing all Memberships belonging to the specified Group; may be
     * empty if the Group has no members.
     */
    public static ArrayList<Membership> getMembershipsInGroup(Context context, String worldName,
                                                          String groupName) {
        ArrayList<Membership> memberships = new ArrayList<>();
        File membersDirectory = FileRetriever.getMembersDirectory(context, worldName,
                groupName);
        membersDirectory.mkdirs();

        for (File membershipFile : membersDirectory.listFiles()) {
            if (membershipFile.isFile()) {
                String membershipFilename = membershipFile.getName();
                String personName = membershipFilename.substring(0,
                        membershipFilename.length() - TEXT_FILE_EXTENSION_LENGTH);

                Membership membership = makeMembershipFromFile(worldName, groupName, personName,
                        membershipFile);
                if (membership != null) {
                    memberships.add(membership);
                }
            }
        }

        return memberships;
    }

}
