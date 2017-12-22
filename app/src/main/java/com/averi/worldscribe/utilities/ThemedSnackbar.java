package com.averi.worldscribe.utilities;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.averi.worldscribe.R;

/**
 * Created by mark on 20/06/16.
 */
public class ThemedSnackbar {
    /**
     * The maximum number of lines that can be displayed by a Snackbar message.
     */
    public static final int SNACKBAR_MAX_LINES = 5;

    public static void showSnackbarMessage(Context context, View parentView, String message) {
        Snackbar snackbar = Snackbar
                .make(parentView, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(AttributeGetter.getColorAttribute(context, R.attr.colorPrimary));
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setMaxLines(SNACKBAR_MAX_LINES);
        snackbar.show();
    }
}
