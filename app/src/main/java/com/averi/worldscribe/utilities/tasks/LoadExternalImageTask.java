package com.averi.worldscribe.utilities.tasks;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.documentfile.provider.DocumentFile;

import com.averi.worldscribe.WorldScribeApplication;
import com.averi.worldscribe.utilities.ImageDecoder;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class LoadExternalImageTask implements Callable<Bitmap> {
    private final String imagePath;
    private final int imageWidth;
    private final int imageHeight;

    /**
     * Instantiates a LoadImageTask for retrieving an external image as a Bitmap.
     * If the given image cannot be found, the task returns null.
     * @param imagePath The filepath of the image whose contents will be
     *                  read. This filepath is relative to the app folder.
     *                  For example, "World1/Image.jpg" would retrieve the contents
     *                  of "/storage/emulated/0/WorldScribe/World1/Image.jpg".
     * @param imageWidth Width to resize the image to, in pixels.
     * @param imageHeight Height to resize the image to, in pixels.
     */
    public LoadExternalImageTask(String imagePath, int imageWidth, int imageHeight) {
        this.imagePath = imagePath;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public Bitmap call() throws FileNotFoundException {
        DocumentFile imageFile = TaskUtils.getFile(imagePath, null);

        // Some external images might have a dot prepended to the file name.
        // See issue #8 to see why only some image files have a dot.
        if (imageFile == null) {
            List<String> pathTokens = Arrays.asList(imagePath.split("/"));
            StringBuilder stringBuilder = new StringBuilder();
            for (String token : pathTokens.subList(0, pathTokens.size() - 1)) {
                stringBuilder.append(token);
                stringBuilder.append("/");
            }
            String dotFilename = "." + pathTokens.get(pathTokens.size() - 1);
            stringBuilder.append(dotFilename);
            String dotFilepath = stringBuilder.toString();

            imageFile = TaskUtils.getFile(dotFilepath, null);
            if (imageFile == null) {
                return null;
            }
        }

        Context context = WorldScribeApplication.getAppContext();
        return ImageDecoder.decodeBitmapFromFile(context, imageFile, imageWidth, imageHeight);
    }
}
