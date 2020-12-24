package com.averi.worldscribe.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.averi.worldscribe.Residence;
import com.averi.worldscribe.utilities.TaskRunner;
import com.averi.worldscribe.utilities.tasks.GetFilenamesInFolderTask;

import java.util.ArrayList;

public class PlaceViewModel extends ViewModel {
    private final TaskRunner taskRunner = new TaskRunner();
    private final MutableLiveData<ArrayList<Residence>> residents = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>("");

    public MutableLiveData<ArrayList<Residence>> getResidents() {
        return residents;
    }

    public void loadResidents(String worldName, String placeName) {
        String residentsFolderPath = worldName + "/Places/" + placeName + "/" + "Residents";

        residents.postValue(null);
        taskRunner.executeAsync(new GetFilenamesInFolderTask(residentsFolderPath, true),
                (residentNames) -> onLoadResidentNames(worldName, placeName, residentNames),
                this::handleLoadError);
    }

    private void onLoadResidentNames(String worldName, String placeName, ArrayList<String> residentNames) {
        ArrayList<Residence> updatedResidents = new ArrayList<>();
        for (String residentName : residentNames) {
            Residence residence = new Residence();
            residence.worldName = worldName;
            residence.placeName = placeName;
            residence.residentName = residentName;
            updatedResidents.add(residence);
        }
        residents.postValue(updatedResidents);
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void clearErrorMessage() {
        errorMessage.postValue("");
    }

    private void handleLoadError(Exception exception) {
        errorMessage.postValue(Log.getStackTraceString(exception));
    }
}
