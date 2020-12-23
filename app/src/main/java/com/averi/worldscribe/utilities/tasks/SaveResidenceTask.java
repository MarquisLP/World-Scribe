package com.averi.worldscribe.utilities.tasks;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.Residence;

import java.io.IOException;
import java.util.concurrent.Callable;

public class SaveResidenceTask implements Callable<Void> {
    private final Residence residence;

    /**
     * Instantiates a SaveResidenceTask for saving a Residence to external storage.
     * @param residence The Residence to save
     */
    public SaveResidenceTask(Residence residence) {
        this.residence = residence;
    }

    @Override
    public Void call() throws IOException {
        String personResidenceFilepath = residence.worldName + "/People/"
                + residence.residentName + "/Residences/" + residence.placeName + ".txt";
        DocumentFile personResidenceFile = TaskUtils.getFile(personResidenceFilepath, "text/plain");
        if (personResidenceFile == null) {
            throw new IOException("Could not create file at 'WorldScribe/" + personResidenceFilepath + "'");
        }

        String placeResidenceFilepath = residence.worldName + "/Places/"
                + residence.placeName + "/Residents/" + residence.residentName + ".txt";
        DocumentFile placeResidenceFile = TaskUtils.getFile(placeResidenceFilepath, "text/plain");
        if (placeResidenceFile == null) {
            throw new IOException("Could not create file at 'WorldScribe/" + placeResidenceFilepath + "'");
        }

        return null;
    }
}
