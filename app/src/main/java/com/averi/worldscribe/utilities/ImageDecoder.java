package com.averi.worldscribe.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by mark on 06/07/16.
 */
public class ImageDecoder {

    /**
     * (Taken from https://developer.android.com/training/displaying-bitmaps/load-bitmap.html).
     * @param imageFile The file that will be decoded into a Bitmap.
     * @param reqWidth The minimum width; the Bitmap will be scaled so that its width is at least
     *                 this large.
     * @param reqHeight The minimum height; the Bitmap will be scaled so that its height is at least
     *                  this large.
     * @return A Bitmap for the image found in imageFile, scaled accordingly.
     */
    public static Bitmap decodeBitmapFromFile(Context context, DocumentFile imageFile, int reqWidth, int reqHeight)
            throws FileNotFoundException {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageFile.getUri()), null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageFile.getUri()), null, options);
    }

    /**
     * Given a Bitmap's target dimensions, calculate a size value that is a power of two.
     * @param options The set of options used for decoding the Bitmap.
     * @param reqWidth The Bitmap's target width.
     * @param reqHeight The Bitmap's target height.
     * @return The appropriate size value to be passed to options.inSampleSize.
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
