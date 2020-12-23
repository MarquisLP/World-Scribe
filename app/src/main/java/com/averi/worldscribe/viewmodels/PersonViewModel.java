package com.averi.worldscribe.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Membership;
import com.averi.worldscribe.Residence;
import com.averi.worldscribe.utilities.TaskRunner;
import com.averi.worldscribe.utilities.tasks.GetFilenamesInFolderTask;
import com.averi.worldscribe.utilities.tasks.GetMembershipsTask;

import java.util.ArrayList;

public class PersonViewModel extends ViewModel {
    private final TaskRunner taskRunner = new TaskRunner();
    private final MutableLiveData<ArrayList<Membership>> memberships = new MutableLiveData<>(null);
    private final MutableLiveData<ArrayList<Residence>> residences = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>("");

    public MutableLiveData<ArrayList<Membership>> getMemberships() { return memberships; }

    public MutableLiveData<ArrayList<Residence>> getResidences() {
        return residences;
    }

    public void loadMemberships(String worldName, String groupName) {
        memberships.postValue(null);
        taskRunner.executeAsync(new GetMembershipsTask(worldName, Category.Person, groupName),
                memberships::postValue, this::handleLoadError);
    }

    public void loadResidences(String worldName, String personName) {
        String residencesFolderPath = worldName + "/People/" + personName + "/" + "Residences";

        residences.postValue(null);
        taskRunner.executeAsync(new GetFilenamesInFolderTask(residencesFolderPath, true),
                (residenceNames) -> onLoadResidenceNames(worldName, personName, residenceNames),
                this::handleLoadError);
    }

    private void onLoadResidenceNames(String worldName, String residentName, ArrayList<String> residenceNames) {
        ArrayList<Residence> updatedResidences = new ArrayList<>();
        for (String placeName : residenceNames) {
            Residence residence = new Residence();
            residence.worldName = worldName;
            residence.placeName = placeName;
            residence.residentName = residentName;
            updatedResidences.add(residence);
        }
        residences.postValue(updatedResidences);
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
