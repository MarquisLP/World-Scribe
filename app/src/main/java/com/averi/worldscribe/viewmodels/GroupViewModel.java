package com.averi.worldscribe.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Membership;
import com.averi.worldscribe.utilities.TaskRunner;
import com.averi.worldscribe.utilities.tasks.GetMembershipsTask;

import java.util.ArrayList;

public class GroupViewModel extends ViewModel {
    private final TaskRunner taskRunner = new TaskRunner();
    private final MutableLiveData<ArrayList<Membership>> members = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>("");

    public MutableLiveData<ArrayList<Membership>> getMembers() {
        return members;
    }

    public void loadMembers(String worldName, String groupName) {
        members.postValue(null);
        taskRunner.executeAsync(new GetMembershipsTask(worldName, Category.Group, groupName),
                members::postValue, this::handleLoadError);
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
