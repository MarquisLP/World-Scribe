package com.averi.worldscribe.utilities.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.averi.worldscribe.WorldScribeApplication;

import java.util.concurrent.Callable;

public class LoadResourceImageTask implements Callable<Bitmap> {
    private final int imageResourceID;

    /**
     * Instantiates a LoadResourceImageTask for retrieving a local resource
     * image as a Bitmap.
     * @param imageResourceID The resource ID of the image to load.
     */
    public LoadResourceImageTask(int imageResourceID) {
        this.imageResourceID = imageResourceID;
    }

    @Override
    public Bitmap call() throws Exception {
        Context context = WorldScribeApplication.getAppContext();
        return BitmapFactory.decodeResource(context.getResources(), imageResourceID);
    }
}
