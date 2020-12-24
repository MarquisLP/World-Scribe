package com.averi.worldscribe.utilities.tasks;

import android.content.Context;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.WorldScribeApplication;

import java.io.IOException;
import java.util.concurrent.Callable;

public class RenameArticleTask implements Callable<Void> {
    private final String worldName;
    private final Category category;
    private final String articleName;
    private final String newArticleName;

    /**
     * Instantiates a RenameArticleTask to rename an Article's folder
     * @param worldName The name of the World the Article belongs to
     * @param category The Article's Category
     * @param articleName The name of the Article to rename
     * @param newArticleName The Article's new name
     */
    public RenameArticleTask(String worldName, Category category, String articleName, String newArticleName) {
        this.worldName = worldName;
        this.category = category;
        this.articleName = articleName;
        this.newArticleName = newArticleName;
    }

    @Override
    public Void call() throws IOException {
        Context context = WorldScribeApplication.getAppContext();
        String articleFolderPath = worldName + "/" + category.pluralName(context) + "/"
                + articleName;

        for (Category connectionCategory : Category.values()) {
            String connectionCategoryPath = articleFolderPath + "/Connections/" + connectionCategory.pluralName(context);
            DocumentFile connectionCategoryFolder = TaskUtils.getFolder(connectionCategoryPath, true);
            for (DocumentFile connectedArticleFile : connectionCategoryFolder.listFiles()) {
                String otherArticleName = TaskUtils.stripFileExtension(connectedArticleFile.getName());
                String otherConnectionFilepath = worldName + "/" + connectionCategory.pluralName(context)
                        + "/" + otherArticleName + "/Connections/" + category.pluralName(context) + "/" + articleName + ".txt";
                DocumentFile otherConnectionFile = TaskUtils.getFile(otherConnectionFilepath, null);
                otherConnectionFile.renameTo(newArticleName + ".txt");
            }
        }

        if (category == Category.Person) {
            DocumentFile membershipsFolder = TaskUtils.getFolder(articleFolderPath + "/Memberships", true);
            for (DocumentFile membershipFile : membershipsFolder.listFiles()) {
                String groupName = TaskUtils.stripFileExtension(membershipFile.getName());
                String groupMembershipFilepath = worldName + "/Groups/" + groupName + "/Members/"
                        + articleName + ".txt";
                DocumentFile groupMembershipFile = TaskUtils.getFile(groupMembershipFilepath, null);
                groupMembershipFile.renameTo(newArticleName + ".txt");
            }

            DocumentFile residencesFolder = TaskUtils.getFolder(articleFolderPath + "/Residences", true);
            for (DocumentFile residenceFile : residencesFolder.listFiles()) {
                String placeName = TaskUtils.stripFileExtension(residenceFile.getName());
                String placeResidenceFilepath = worldName + "/Places/" + placeName + "/Residents/"
                        + articleName + ".txt";
                DocumentFile placeResidenceFile = TaskUtils.getFile(placeResidenceFilepath, null);
                placeResidenceFile.renameTo(newArticleName + ".txt");
            }
        }
        else if (category == Category.Group) {
            DocumentFile membersFolder = TaskUtils.getFolder(articleFolderPath + "/Members", true);
            for (DocumentFile membershipFile : membersFolder.listFiles()) {
                String personName = TaskUtils.stripFileExtension(membershipFile.getName());
                String personMembershipFilepath = worldName + "/People/" + personName + "/Memberships/"
                        + articleName + ".txt";
                DocumentFile personMembershipFile = TaskUtils.getFile(personMembershipFilepath, null);
                personMembershipFile.renameTo(newArticleName + ".txt");
            }
        }
        else if (category == Category.Place) {
            DocumentFile residentsFolder = TaskUtils.getFolder(articleFolderPath + "/Residents", true);
            for (DocumentFile residenceFile : residentsFolder.listFiles()) {
                String personName = TaskUtils.stripFileExtension(residenceFile.getName());
                String personResidenceFilepath = worldName + "/People/" + personName + "/Residences/"
                        + articleName + ".txt";
                DocumentFile personResidenceFile = TaskUtils.getFile(personResidenceFilepath, null);
                personResidenceFile.renameTo(newArticleName + ".txt");
            }
        }

        DocumentFile articleFolder = TaskUtils.getFolder(articleFolderPath, false);
        if (!(articleFolder.renameTo(newArticleName))) {
            throw new IOException("Failed to rename Article '" + articleName + "' to '" + newArticleName + "'");
        }
        return null;
    }
}
