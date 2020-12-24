package com.averi.worldscribe.utilities.tasks;

import android.content.Context;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.WorldScribeApplication;
import com.averi.worldscribe.utilities.ExternalDeleter;

import java.io.IOException;
import java.util.concurrent.Callable;

public class DeleteArticleTask implements Callable<Void> {
    private final String worldName;
    private final Category category;
    private final String articleName;

    /**
     * Instantiates a DeleteArticleTask for deleting an Article and all of its related
     * Snippets, Connections, Memberships, and Residences.
     * @param worldName The name of the World the Article belongs to
     * @param category The Category the Article belongs to
     * @param articleName The name of the Article to delete
     */
    public DeleteArticleTask(String worldName, Category category, String articleName) {
        this.worldName = worldName;
        this.category = category;
        this.articleName = articleName;
    }

    @Override
    public Void call() throws IOException {
        Context context = WorldScribeApplication.getAppContext();
        String articleFolderPath = worldName + "/" + category.pluralName(context) + "/" + articleName;

        for (Category connectionCategory : Category.values()) {
            String connectionCategoryPath = articleFolderPath + "/Connections/" + connectionCategory.pluralName(context);
            DocumentFile connectionCategoryFolder = TaskUtils.getFolder(connectionCategoryPath, true);
            for (DocumentFile connectedArticleFile : connectionCategoryFolder.listFiles()) {
                String otherArticleName = TaskUtils.stripFileExtension(connectedArticleFile.getName());
                String otherConnectionFilepath = worldName + "/" + connectionCategory.pluralName(context)
                        + "/" + otherArticleName + "/Connections/" + category.pluralName(context) + "/" + articleName + ".txt";
                DocumentFile otherConnectionFile = TaskUtils.getFile(otherConnectionFilepath, null);
                if (!otherConnectionFile.delete()) {
                    throw new IOException("Failed to delete 'WorldScribe/" + otherConnectionFilepath + "'");
                }
            }
        }

        if (category == Category.Person) {
            DocumentFile membershipsFolder = TaskUtils.getFolder(articleFolderPath + "/Memberships", true);
            for (DocumentFile membershipFile : membershipsFolder.listFiles()) {
                String groupName = TaskUtils.stripFileExtension(membershipFile.getName());
                String groupMembershipFilepath = worldName + "/Groups/" + groupName + "/Members/"
                        + articleName + ".txt";
                DocumentFile groupMembershipFile = TaskUtils.getFile(groupMembershipFilepath, null);
                if (!groupMembershipFile.delete()) {
                    throw new IOException("Failed to delete 'WorldScribe/" + groupMembershipFilepath + "'");
                }
            }

            DocumentFile residencesFolder = TaskUtils.getFolder(articleFolderPath + "/Residences", true);
            for (DocumentFile residenceFile : residencesFolder.listFiles()) {
                String placeName = TaskUtils.stripFileExtension(residenceFile.getName());
                String placeResidenceFilepath = worldName + "/Places/" + placeName + "/Residents/"
                        + articleName + ".txt";
                DocumentFile placeResidenceFile = TaskUtils.getFile(placeResidenceFilepath, null);
                if (!placeResidenceFile.delete()) {
                    throw new IOException("Failed to delete 'WorldScribe/" + placeResidenceFilepath + "'");
                }
            }
        }
        else if (category == Category.Group) {
            DocumentFile membersFolder = TaskUtils.getFolder(articleFolderPath + "/Members", true);
            for (DocumentFile membershipFile : membersFolder.listFiles()) {
                String personName = TaskUtils.stripFileExtension(membershipFile.getName());
                String personMembershipFilepath = worldName + "/People/" + personName + "/Memberships/"
                        + articleName + ".txt";
                DocumentFile personMembershipFile = TaskUtils.getFile(personMembershipFilepath, null);
                if (!personMembershipFile.delete()) {
                    throw new IOException("Failed to delete 'WorldScribe/" + personMembershipFilepath + "'");
                }
            }
        }
        else if (category == Category.Place) {
            DocumentFile residentsFolder = TaskUtils.getFolder(articleFolderPath + "/Residents", true);
            for (DocumentFile residenceFile : residentsFolder.listFiles()) {
                String personName = TaskUtils.stripFileExtension(residenceFile.getName());
                String personResidenceFilepath = worldName + "/People/" + personName + "/Residences/"
                        + articleName + ".txt";
                DocumentFile personResidenceFile = TaskUtils.getFile(personResidenceFilepath, null);
                if (!personResidenceFile.delete()) {
                    throw new IOException("Failed to delete 'WorldScribe/" + personResidenceFilepath + "'");
                }
            }
        }

        DocumentFile articleFolder = TaskUtils.getFolder(articleFolderPath, false);
        if (articleFolder == null) {
            throw new IOException("Could not access folder at 'WorldScribe/" + articleFolderPath + "'");
        }
        if (!(ExternalDeleter.deleteRecursive(articleFolder))) {
            throw new IOException("Failed to delete folder at 'WorldScribe/" + articleFolderPath + "'");
        }

        return null;
    }
}
