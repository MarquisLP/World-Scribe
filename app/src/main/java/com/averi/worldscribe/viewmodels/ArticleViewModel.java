package com.averi.worldscribe.viewmodels;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.Connection;
import com.averi.worldscribe.R;
import com.averi.worldscribe.WorldScribeApplication;
import com.averi.worldscribe.utilities.TaskRunner;
import com.averi.worldscribe.utilities.tasks.GetConnectionsTask;
import com.averi.worldscribe.utilities.tasks.GetFilenamesInFolderTask;
import com.averi.worldscribe.utilities.tasks.LoadExternalImageTask;
import com.averi.worldscribe.utilities.tasks.LoadResourceImageTask;

import java.util.ArrayList;

public class ArticleViewModel extends ViewModel {
    private final TaskRunner taskRunner = new TaskRunner();
    private final MutableLiveData<Bitmap> image = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> imageColorFilterIsOn = new MutableLiveData<>(false);
    private final MutableLiveData<ArrayList<String>> snippetNames = new MutableLiveData<>(null);
    private final MutableLiveData<ArrayList<Connection>> connections = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>("");

    public MutableLiveData<Bitmap> getImage() {
        return image;
    }

    public MutableLiveData<Boolean> getImageColorFilterIsOn() {
        return imageColorFilterIsOn;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    private void handleLoadError(Exception exception) {
        errorMessage.postValue(Log.getStackTraceString(exception));
    }

    public void clearErrorMessage() {
        errorMessage.postValue("");
    }

    public void loadImage(String worldName, Category category, String articleName,
                          int imageWidth, int imageHeight) {
        Context context = WorldScribeApplication.getAppContext();
        String imagePath = worldName + "/" + category.pluralName(context) + "/"
                + articleName + "/" + "Image.jpg";
        image.postValue(null);
        taskRunner.executeAsync(new LoadExternalImageTask(imagePath, imageWidth, imageHeight),
                (externalImage) -> handleLoadedExternalImage(category, externalImage),
                this::handleLoadError);
    }

    private void handleLoadedExternalImage(Category category, Bitmap externalImage) {
        if (externalImage == null) {
            loadPlaceholderImage(category);
        }
        else {
            setExternalImage(externalImage);
        }
    }

    private void setExternalImage(Bitmap externalImage) {
        imageColorFilterIsOn.postValue(false);
        image.postValue(externalImage);
    }

    private void loadPlaceholderImage(Category category) {
        int placeholderImageResourceID;
        switch (category) {
            case Person:
                placeholderImageResourceID = R.drawable.blank_person;
                break;
            case Group:
                placeholderImageResourceID = R.drawable.unset_image_group;
                break;
            case Place:
                placeholderImageResourceID = R.drawable.unset_image_place;
                break;
            case Item:
                placeholderImageResourceID = R.drawable.unset_image_item;
                break;
            case Concept:
            default:
                placeholderImageResourceID = R.drawable.unset_image_concept;
        }

        taskRunner.executeAsync(new LoadResourceImageTask(placeholderImageResourceID),
                this::setResourceImage, this::handleLoadError);
    }

    private void setResourceImage(Bitmap resourceImage) {
        imageColorFilterIsOn.postValue(true);
        image.postValue(resourceImage);
    }

    public MutableLiveData<ArrayList<String>> getSnippetNames() {
        return snippetNames;
    }

    public void loadSnippetNames(String worldName, Category category, String articleName) {
        Context context = WorldScribeApplication.getAppContext();
        String snippetsPath = worldName + "/" + category.pluralName(context) + "/"
                + articleName + "/" + "Snippets";
        snippetNames.postValue(null);
        taskRunner.executeAsync(new GetFilenamesInFolderTask(snippetsPath, true),
                snippetNames::postValue, this::handleLoadError);
    }

    public MutableLiveData<ArrayList<Connection>> getConnections() {
        return connections;
    }

    public void loadConnections(String worldName, Category category, String articleName) {
        connections.postValue(null);
        taskRunner.executeAsync(new GetConnectionsTask(worldName, category, articleName),
                connections::postValue, this::handleLoadError);
    }
}
