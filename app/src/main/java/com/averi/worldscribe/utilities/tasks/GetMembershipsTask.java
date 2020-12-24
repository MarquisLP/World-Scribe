package com.averi.worldscribe.utilities.tasks;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Membership;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class GetMembershipsTask implements Callable<ArrayList<Membership>> {
    private final String worldName;
    private final String articleName;
    private final Category articleCategory;

    /**
     * Instantiates a {@link GetMembershipsTask} for retrieving the
     * {@link Membership}s associated with a Person or Group.
     * @param worldName The name of the World that the Person or Group belongs to.
     * @param articleName The name of the Person or Group.
     * @param articleCategory The Category of the Article whose Memberships will be retrieved.
     *                        Must be Category.Person or Category.Group.
     */
    public GetMembershipsTask(String worldName, Category articleCategory, String articleName) {
        this.worldName = worldName;
        this.articleName = articleName;
        this.articleCategory = articleCategory;
    }

    @Override
    public ArrayList<Membership> call() throws IOException, IllegalArgumentException {
        ArrayList<Membership> memberships = new ArrayList<>();

        String membershipsFolderPath;
        if (articleCategory == Category.Person) {
            membershipsFolderPath = worldName + "/People/" + articleName + "/Memberships";
        }
        else if (articleCategory == Category.Group) {
            membershipsFolderPath = worldName + "/Groups/" + articleName + "/Members";
        }
        else {
            throw new IllegalArgumentException("Attempted to load Memberships for an Article that was not a Person or a Group.");
        }

        DocumentFile membershipsFolder = TaskUtils.getFolder(membershipsFolderPath, true);
        if (membershipsFolder == null) {
            throw new FileNotFoundException("Could not access folder at 'WorldScribe/" + membershipsFolderPath + "'");
        }

        for (DocumentFile membershipFile : membershipsFolder.listFiles()) {
            Membership membership = new Membership();
            membership.worldName = worldName;
            membership.memberRole = TaskUtils.readFileContents(membershipFile);
            if (articleCategory == Category.Person) {
                membership.groupName = TaskUtils.stripFileExtension(membershipFile.getName());
                membership.memberName = articleName;
            }
            else {
                membership.groupName = articleName;
                membership.memberName = TaskUtils.stripFileExtension(membershipFile.getName());
            }
            memberships.add(membership);
        }

        return memberships;
    }
}
