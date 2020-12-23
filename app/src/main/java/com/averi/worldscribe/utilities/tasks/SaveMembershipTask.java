package com.averi.worldscribe.utilities.tasks;

import android.content.Context;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.Membership;
import com.averi.worldscribe.WorldScribeApplication;
import com.averi.worldscribe.utilities.ExternalWriter;

import java.io.IOException;
import java.util.concurrent.Callable;

public class SaveMembershipTask implements Callable<Void> {
    private final Membership membership;

    /**
     * Instantiates a SaveMembershipTask for saving a Membership to external storage.
     * @param membership The Membership to save
     */
    public SaveMembershipTask(Membership membership) {
        this.membership = membership;
    }

    @Override
    public Void call() throws IOException {
        Context context = WorldScribeApplication.getAppContext();

        String personMembershipFilepath = membership.worldName + "/People/"
                + membership.memberName + "/Memberships/"
                + membership.groupName + ".txt";
        DocumentFile personMembershipFile = TaskUtils.getFile(personMembershipFilepath, "text/plain");
        if (!(ExternalWriter.writeStringToFile(context, personMembershipFile, membership.memberRole))) {
            throw new IOException("Could not write to file 'WorldScribe/" + personMembershipFilepath + "'");
        }

        String groupMembershipFilepath = membership.worldName + "/Groups/"
                + membership.groupName + "/Members/"
                + membership.memberName + ".txt";
        DocumentFile groupMembershipFile = TaskUtils.getFile(groupMembershipFilepath, "text/plain");
        if (!(ExternalWriter.writeStringToFile(context, groupMembershipFile, membership.memberRole))) {
            throw new IOException("Could not write to file 'WorldScribe/" + groupMembershipFilepath + "'");
        }

        return null;
    }
}
