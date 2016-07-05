package com.averi.worldscribe.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

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

}
