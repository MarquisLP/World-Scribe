package com.averi.worldscribe.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.WorldScribeApplication;
import com.averi.worldscribe.utilities.TaskRunner;
import com.averi.worldscribe.utilities.tasks.GetFilenamesInFolderTask;

import java.util.ArrayList;

public class SelectArticleViewModel extends ViewModel {
    private final TaskRunner taskRunner = new TaskRunner();
    private final MutableLiveData<ArrayList<String>> articleNames = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>("");

    public MutableLiveData<Boolean> isLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void clearErrorMessage() {
        errorMessage.postValue("");
    }

    public LiveData<ArrayList<String>> getArticleNames() {
        return articleNames;
    }

    public void loadArticleNamesFromStorage(String worldName, Category category) {
        Context context = WorldScribeApplication.getAppContext();
        String categoryFolderPath = worldName + "/" + category.pluralName(context);
        isLoading.postValue(true);
        taskRunner.executeAsync(new GetFilenamesInFolderTask(categoryFolderPath, true),
                this::updateArticleNames, this::handleLoadArticleNamesError);
    }

    private void updateArticleNames(ArrayList<String> newArticleNames) {
        isLoading.postValue(false);
        articleNames.postValue(newArticleNames);
    }

    private void handleLoadArticleNamesError(Exception exception) {
        errorMessage.postValue(Log.getStackTraceString(exception));
    }
}
