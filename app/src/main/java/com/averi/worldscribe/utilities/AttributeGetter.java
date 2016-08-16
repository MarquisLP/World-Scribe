package com.averi.worldscribe.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.renderscript.Type;
import android.util.TypedValue;

import com.averi.worldscribe.R;

/**
 * Created by mark on 24/06/16.
 */
public class AttributeGetter {

    public static int getColorAttribute(Context context, int colorID) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(colorID, typedValue, true);
        return typedValue.data;
    }

    /**
     * Get a color attribute from a specific style.
     * @param context The Context calling this method.
     * @param styleID The ID of the style resource to retrieve the color from.
     * @param colorID The ID of the color attribute.
     * @return The resource ID of the color attribute as specified by the style.
     */
    public static int getColorAttribute(Context context, int styleID, int colorID) {
        TypedArray typedArray = context.obtainStyledAttributes(styleID, new int[]{colorID});
        int colorResource = typedArray.getColor(0, Color.BLACK);
        typedArray.recycle();
        return colorResource;
    }

    /**
     * Get the name of a style.
     * @param context The Context calling this method.
     * @param styleID The ID of the style resource to retrieve the name from.
     * @return The name of the specified style.
     */
    public static String getStyleName(Context context, int styleID) {
        TypedArray typedArray = context.obtainStyledAttributes(styleID,
                new int[]{android.R.attr.name});
        String styleName = typedArray.getString(0);
        typedArray.recycle();
        return styleName;
    }

}
