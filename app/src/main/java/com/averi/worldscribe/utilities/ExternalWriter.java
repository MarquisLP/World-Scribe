package com.averi.worldscribe.utilities;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;
import com.averi.worldscribe.Membership;
import com.averi.worldscribe.Residence;
import com.balda.flipper.DocumentFileCompat;
import com.balda.flipper.Root;
import com.balda.flipper.StorageManagerCompat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 08/06/16.
 */
public final class ExternalWriter {

    public static final int IMAGE_BYTE_SIZE = 1024;

    private static boolean externalStorageIsWritable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }

    /**
     * Creates the app's directory on the phone's external storage.
     * @return The DocumentFile representing the app directory, or null if it couldn't be created
     */
    public static DocumentFile createAppDirectory(Context context) {
        DocumentFile rootDirectory = FileRetriever.getFileRootDirectory(context);
        return rootDirectory.createDirectory(FileRetriever.APP_DIRECTORY_NAME);
    }

    /**
     * Creates a .nomedia file in the top-level folder of the app's directory, in order to
     * prevent media files (such as images) from appearing in other Android apps (such as the
     * Gallery app).
     * @return True if the .nomedia file was created successfully in the top-level app folder
     */
    public static boolean createNoMediaFile(Context context) {
        DocumentFile appDirectory = FileRetriever.getAppDirectory(context, true);
        return (appDirectory.createFile("blank/blank", ".nomedia") == null);
    }

    public static DocumentFile createWorldDirectory(Context context, String worldName) {
        DocumentFile appDirectory = FileRetriever.getAppDirectory(context, true);
        if (appDirectory == null) {
            appDirectory = createAppDirectory(context);
        }
        DocumentFile worldDirectory = appDirectory.createDirectory(worldName);

        if (worldDirectory != null) {
            boolean allCategoryFoldersWereCreated = createArticleTypeDirectories(context, worldDirectory);

            // If one or more subfolders couldn't be created, delete the World folder so that
            // subsequent attempts can start building the World folder again from scratch.
            if (!(allCategoryFoldersWereCreated)) {
                // Delete the World directory.
                worldDirectory.delete();
                worldDirectory = null;
            }
        }

        return worldDirectory;
    }

    private static boolean createArticleTypeDirectories(Context context, DocumentFile worldDirectory) {
        boolean directoriesWereCreated = true;

        for (Category category : Category.values()) {
            DocumentFile categoryFolder = worldDirectory.createDirectory(category.pluralName(context));

            if (categoryFolder == null) {
                directoriesWereCreated = false;
            }
        }

        return directoriesWereCreated;
    }

    /**
     * Create a directory for a new Article.
     * Note that this does not overwrite an existing directory, so make sure to check before calling
     * this method.
     * @param context The context calling this method.
     * @param worldName The name of the World where the Article belongs.
     * @param category The {@link Category} the Article belongs to.
     * @param articleName The name of the new Article.
     * @return The DocumentFile representing the newly-created Article directory
     */
    public static DocumentFile createArticleDirectory(Context context, String worldName,
                                                  Category category, String articleName) {
        DocumentFile articleDirectory = FileRetriever.getArticleDirectory(
                context, worldName, category, articleName, true);
        if (articleDirectory == null) {
            return null;
        }

        DocumentFile articleConnectionsDirectory;
        articleConnectionsDirectory = articleDirectory.createDirectory("Connections");
        if (articleConnectionsDirectory == null) {
            articleDirectory.delete();
            return null;
        }

        boolean allConnectionFoldersWereCreated = false;
        allConnectionFoldersWereCreated = (
                (articleConnectionsDirectory.createDirectory("People") != null)
                && (articleConnectionsDirectory.createDirectory("Groups") != null)
                && (articleConnectionsDirectory.createDirectory("Places") != null)
                && (articleConnectionsDirectory.createDirectory("Items") != null)
                && (articleConnectionsDirectory.createDirectory("Concepts") != null)
        );
        if (!(allConnectionFoldersWereCreated)) {
            articleDirectory.delete();
            return null;
        }

        if (category == Category.Person) {
            if (articleDirectory.createDirectory("Memberships") == null) {
                articleDirectory.delete();
                return null;
            }
            if (articleDirectory.createDirectory("Residences") == null) {
                articleDirectory.delete();
                return null;
            }
        } else if (category == Category.Group) {
            if (articleDirectory.createDirectory("Members") == null) {
                articleDirectory.delete();
                return null;
            }
        } else if (category == Category.Place) {
            if (articleDirectory.createDirectory("Residents") == null) {
                articleDirectory.delete();
                return null;
            }
        }

        return articleDirectory;
    }

    /**
     * Saves a String to a text file within an Article's directory.
     * @param context The Context calling this method.
     * @param worldName The name of the world the Article belongs to.
     * @param category The name of the Category the Article belongs to.
     * @param articleName The name of the Article whose directory will possess the text file.
     * @param fileName The name of the text file that will be written to, with the file extension
     *                 omitted.
     * @param contents The String that will be saved in a text file.
     * @return True if the String was saved successfully; false if an I/O error occurs.
     */
    public static boolean writeStringToArticleFile(Context context, String worldName,
                                                   Category category, String articleName,
                                                   String fileName, String contents) {
        DocumentFile articleDirectory = FileRetriever.getArticleDirectory(
                context, worldName, category, articleName, true);
        DocumentFile articleFile = DocumentFileCompat.getFile(articleDirectory,
                fileName + ExternalReader.TEXT_FIELD_FILE_EXTENSION, "text/plain");
        return writeStringToFile(context, articleFile, contents);
    }

    /**
     * Saves a String message to a file.
     * @param textFile The File that will be written to.
     * @param contents The message to save to the file.
     * @return True if the String was saved successfully; false if an I/O error occurs.
     */
    public static boolean writeStringToFile(Context context, DocumentFile textFile, String contents) {
        Boolean result = true;

        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(textFile.getUri(), "rwt");
            PrintWriter writer = new PrintWriter(outputStream);
            writer.print(contents);
            writer.close();
        } catch (IOException error) {
            result = false;
        }

        return result;
    }

    /**
     * Saves an image to a specific Article's directory.
     * If the Article already has an image, it will be overwritten.
     * @param context The Context calling this method.
     * @param worldName The name of the world the Article belongs to.
     * @param category The name of the Category the Article belongs to.
     * @param articleName The name of the Article whose image will be saved.
     * @param imageUri The URI to the new image's original location.
     * @return True if the image was saved successfully; false otherwise.
     */
    public static boolean saveArticleImage(Context context, String worldName, Category category,
                                           String articleName, Uri imageUri) {
        Boolean result = true;


        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        DocumentFile newImageFile = null;

        DocumentFile articleDirectory = FileRetriever.getArticleDirectory(
                context, worldName, category, articleName, true);
        if (articleDirectory == null) {
            return false;
        }
        try {
            newImageFile = articleDirectory.createFile("image/jpg", "New_Image" + ExternalReader.IMAGE_FILE_EXTENSION);
            if (newImageFile == null) {
                throw new IOException();
            }

            bufferedInputStream = new BufferedInputStream(
                    context.getContentResolver().openInputStream(imageUri));
            bufferedOutputStream = new BufferedOutputStream(
                    context.getContentResolver().openOutputStream(newImageFile.getUri()));
            byte[] buf = new byte[IMAGE_BYTE_SIZE];
            bufferedInputStream.read(buf);
            do {
                bufferedOutputStream.write(buf);
            } while (bufferedInputStream.read(buf) != -1);

            DocumentFile existingImageFile = articleDirectory.findFile("Image" + ExternalReader.IMAGE_FILE_EXTENSION);
            if (existingImageFile != null) {
                if (!(existingImageFile.delete())) {
                    throw new IOException();
                }
            }
            result = newImageFile.renameTo("Image" + ExternalReader.IMAGE_FILE_EXTENSION);
        } catch (IOException e) {
            result = false;
        } finally {
            try {
                if (bufferedInputStream != null) bufferedInputStream.close();
                if (bufferedOutputStream != null) bufferedOutputStream.close();
                if ((!(result)) && (newImageFile != null)) {
                    newImageFile.delete();
                }
            } catch (IOException e) { }
        }

        return result;
    }

    /**
     * Saves the relation between an Article and one of its connected Articles.
     * @param context The Context calling this method.
     * @param worldName The name of the world the Article belongs to.
     * @param category The name of the Category the Article belongs to.
     * @param articleName The name of the Article who possesses the specified Connection.
     * @param connectedArticleCategory
     * @param connectedArticleName
     * @param relation A description of how the specified Article is related to the connected
     *                 Article.
     * @return True if the relation was saved successfully; false an I/O error occurs.
     */
    public static boolean saveConnectionRelation(Context context, String worldName,
                                                 Category category, String articleName,
                                                 Category connectedArticleCategory,
                                                 String connectedArticleName, String relation) {
        DocumentFile connectionFile = FileRetriever.getConnectionRelationFile(
                context, worldName, category, articleName, connectedArticleCategory,
                connectedArticleName, true);
        if (connectionFile == null) {
            return false;
        }
        return writeStringToFile(context, connectionFile, relation);
    }

    /**
     * Saves a String as a Snippet's new contents.
     * @param context The Context calling this method.
     * @param worldName The name of the world the Article belongs to.
     * @param category The name of the Category the Article belongs to.
     * @param articleName The name of the Article who possesses the specified Snippet.
     * @param snippetName The name of the Snippet that will be written to.
     * @param contents The String that will be saved to the Snippet.
     * @return True if the String was saved successfully; false if an I/O error occurs.
     */
    public static boolean writeSnippetContents(Context context, String worldName, Category category,
                                               String articleName, String snippetName,
                                               String contents) {
        DocumentFile snippetsDirectory = FileRetriever.getSnippetsDirectory(context,
                worldName, category, articleName, true);
        if (snippetsDirectory == null) {
            return false;
        }

        DocumentFile snippetFile = snippetsDirectory.findFile(snippetName + FileRetriever.SNIPPET_FILE_EXTENSION);
        if (snippetFile == null) {
            snippetFile = snippetsDirectory.createFile("text/plain", snippetName);
            if (snippetFile == null) {
                return false;
            }
        }
        return writeStringToFile(context, snippetFile, contents);
    }

    /**
     * Renames an Article's Snippet.
     * @param context The Context calling this method.
     * @param worldName The name of the world the Article belongs to.
     * @param category The name of the Category the Article belongs to.
     * @param articleName The name of the Article who possesses the specified Snippet.
     * @param oldSnippetName The name of the Snippet that will be renamed.
     * @param newSnippetName The new name for the Snippet.
     * @return True if the Snippet was renamed successfully; false if an I/O error occurs.
     */
    public static boolean renameSnippet(Context context, String worldName, Category category,
                                               String articleName, String oldSnippetName,
                                               String newSnippetName) {
        DocumentFile snippetFile = FileRetriever.getSnippetFile(context, worldName, category, articleName,
                oldSnippetName, false);
        return snippetFile.renameTo(newSnippetName + ExternalReader.TEXT_FIELD_FILE_EXTENSION);
    }

    /**
     * Saves a Membership between a Person and a Group.
     * @param context The Context calling this method.
     * @param membership Contains data on the Membership that will be saved.
     * @return True if the Membership was successfully saved to both the Person's and the Group's
     * directories; false if an I/O error occurs.
     */
    public static boolean saveMembership(Context context, Membership membership) {
        DocumentFile fileInMemberDirectory = FileRetriever.getMembershipFile(context,
                membership.worldName, membership.memberName, membership.groupName, true);
        if (fileInMemberDirectory == null) {
            return false;
        }
        DocumentFile fileInGroupDirectory = FileRetriever.getMemberFile(context,
                membership.worldName, membership.groupName, membership.memberName, true);
        if (fileInGroupDirectory == null) {
            fileInMemberDirectory.delete();
            return false;
        }

        boolean savedToMemberDirectory = writeStringToFile(context, fileInMemberDirectory,
                membership.memberRole);
        boolean savedToGroupDirectory = writeStringToFile(context, fileInGroupDirectory,
                membership.memberRole);

        return ((savedToMemberDirectory) && (savedToGroupDirectory));
    }

    /**
     * Saves a Person's Residence within a Place.
     * @param context The Context calling this method.
     * @param residence The Residence data that will be saved to file.
     * @return True if the Residence data was saved successfully to both the Person's and the
     * Place's directories; false if an I/O error occurs.
     */
    public static boolean saveResidence(Context context, Residence residence) {
        DocumentFile fileInPersonDirectory = FileRetriever.getResidenceFile(context,
                residence.worldName, residence.residentName, residence.placeName, true);
        if (fileInPersonDirectory == null) {
            return false;
        }
        DocumentFile fileInPlaceDirectory = FileRetriever.getResidentFile(context,
                residence.worldName, residence.placeName, residence.residentName, true);
        if (fileInPlaceDirectory == null) {
            fileInPersonDirectory.delete();
            return false;
        }

        return true;
    }

    /**
     * Updates Connection files to reflect a rename for the "main" Article in a Connection.
     * @param context The The Context calling this method.
     * @param connection The Connection that will be updated.
     * @param newMainArticleName The new name for the main Article in the Connection.
     * @return True if the Connection was updated successfully; false otherwise.
     */
    public static boolean renameArticleInConnection(Context context, Connection connection,
                                                    String newMainArticleName) {
        DocumentFile connectionRelationFile = FileRetriever.getConnectionRelationFile(context,
                connection.worldName, connection.connectedArticleCategory,
                connection.connectedArticleName, connection.articleCategory,
                connection.articleName, false);
        if (connectionRelationFile == null) {
            return false;
        }
        return connectionRelationFile.renameTo(newMainArticleName + ExternalReader.TEXT_FIELD_FILE_EXTENSION);
    }

    /**
     * Updates Membership files to reflect a rename for the member in a Membership.
     * @param context The The Context calling this method.
     * @param membership The Membership that will be updated.
     * @param newMemberName The new name for the member in the Membership.
     * @return True if the Membership was updated successfully; false otherwise.
     */
    public static boolean renameMemberInMembership(Context context, Membership membership,
                                                   String newMemberName) {
        DocumentFile memberFile = FileRetriever.getMemberFile(context,
                membership.worldName, membership.groupName, membership.memberName, false);
        if (memberFile == null) {
            return false;
        }
        return memberFile.renameTo(newMemberName + ExternalReader.TEXT_FIELD_FILE_EXTENSION);
    }

    /**
     * Updates Membership files to reflect a rename for the Group in a Membership.
     * @param context The The Context calling this method.
     * @param membership The Membership that will be updated.
     * @param newGroupName The new name for the Group in the Membership.
     * @return True if the Membership was updated successfully; false otherwise.
     */
    public static boolean renameGroupInMembership(Context context, Membership membership,
                                                  String newGroupName) {
        DocumentFile membershipFile = FileRetriever.getMembershipFile(context,
                membership.worldName, membership.memberName, membership.groupName, false);
        if (membershipFile == null) {
            return false;
        }
        return membershipFile.renameTo(newGroupName + ExternalReader.TEXT_FIELD_FILE_EXTENSION);
    }

    /**
     * Updates Residence files to reflect a rename for the resident of a Residence.
     * @param context The The Context calling this method.
     * @param residence The Residence that will be updated.
     * @param newResidentName The new name for the resident in the Residence.
     * @return True if the Residence was updated successfully; false otherwise.
     */
    public static boolean renameResidentInResidence(Context context, Residence residence,
                                                    String newResidentName) {
        DocumentFile residentFile = FileRetriever.getResidentFile(context,
                residence.worldName, residence.placeName, residence.residentName, false);
        if (residentFile == null) {
            return false;
        }
        return residentFile.renameTo(newResidentName + ExternalReader.TEXT_FIELD_FILE_EXTENSION);
    }

    /**
     * Updates Residence files to reflect a rename for the Place of a Residence.
     * @param context The The Context calling this method.
     * @param residence The Residence that will be updated.
     * @param newResidentName The new name for the Place of Residence.
     * @return True if the Residence was updated successfully; false otherwise.
     */
    public static boolean renamePlaceInResidence(Context context, Residence residence,
                                                    String newResidentName) {
        DocumentFile residenceFile = FileRetriever.getResidenceFile(context,
                residence.worldName, residence.residentName, residence.placeName, false);
        if (residenceFile == null) {
            return false;
        }
        return residenceFile.renameTo(newResidentName + ExternalReader.TEXT_FIELD_FILE_EXTENSION);
    }

    /**
     * Renames the directory for a specific World.
     * @param currentWorldName The World's current name.
     * @param newWorldName The World's new name.
     * @return True if the directory was renamed successfully; false otherwise.
     */
    public static boolean renameWorldDirectory(Context context, String currentWorldName, String newWorldName) {
        DocumentFile worldDirectory = FileRetriever.getWorldDirectory(context, currentWorldName, false);
        if (worldDirectory == null) {
            return false;
        }
        return worldDirectory.renameTo(newWorldName);
    }

    /**
     * Renames the directory for a specific Article.
     * @param context The The Context calling this method.
     * @param worldName The name of The name of the world the Article belongs to.
     * @param category The name of the Category the Article belongs to.
     * @param articleName The name of the Article that will be renamed.
     * @param newArticleName The Article's new name.
     * @return True if the directory was renamed successfully; false otherwise.
     */
    public static boolean renameArticleDirectory(Context context, String worldName,
                                                 Category category, String articleName,
                                                 String newArticleName) {
        DocumentFile articleDirectory = FileRetriever.getArticleDirectory(context, worldName, category,
                articleName, false);
        if (articleDirectory == null) {
            return false;
        }
        return articleDirectory.renameTo(newArticleName);
    }

}
