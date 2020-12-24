package com.averi.worldscribe.utilities.tasks;

import android.content.Context;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.WorldScribeApplication;
import com.averi.worldscribe.utilities.ExternalWriter;

import java.util.concurrent.Callable;

public class CreateArticleTask implements Callable<Void> {
    private final String worldName;
    private final Category category;
    private final String articleName;

    /**
     * Instantiates a new CreateArticleTask for creating a new Article's folder and subfolders.
     * @param worldName The name of the World the Article will belong to
     * @param category The Category of the new Article
     * @param articleName The new Article's name
     */
    public CreateArticleTask(String worldName, Category category, String articleName) {
        this.worldName = worldName;
        this.category = category;
        this.articleName = articleName;
    }

    @Override
    public Void call() throws Exception {
        Context context = WorldScribeApplication.getAppContext();
        ExternalWriter.createArticleDirectory(context, worldName, category, articleName);
        return null;
    }
}
