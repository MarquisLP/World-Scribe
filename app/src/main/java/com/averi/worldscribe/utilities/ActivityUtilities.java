package com.averi.worldscribe.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.activities.ArticleListActivity;
import com.averi.worldscribe.activities.CreateWorldActivity;
import com.averi.worldscribe.activities.LoadWorldActivity;
import com.averi.worldscribe.activities.SettingsActivity;
import com.averi.worldscribe.adapters.StringListAdapter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mark on 23/06/16.
 */
public class ActivityUtilities {

    public static final int WORD_WRAP_MAX_LINES = 999;

    public static void goToWorld(Context context, String worldName) {
        AppPreferences.saveLastOpenedWorld(context, worldName);

        Intent goToWorldIntent = new Intent(context, ArticleListActivity.class);
        goToWorldIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        goToWorldIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        goToWorldIntent.putExtra(IntentFields.WORLD_NAME, worldName);
        goToWorldIntent.putExtra("category", Category.Person);
        context.startActivity(goToWorldIntent);
    }

    /**
     * Handle app bar items that are common to all Activities that have an app bar.
     * For example, the "Create World" option is an item that can always be accessed from the app
     * bar.
     * @param context The Context calling this method.
     * @param item The item that was selected from the app bar's menu.
     */
    public static void handleCommonAppBarItems(Context context, String worldName, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.createWorldItem:
                Intent goToWorldCreationIntent = new Intent(context, CreateWorldActivity.class);
                context.startActivity(goToWorldCreationIntent);
                break;
            case R.id.loadWorldItem:
                Intent goToLoadWorldIntent = new Intent(context, LoadWorldActivity.class);
                context.startActivity(goToLoadWorldIntent);
                break;
            case R.id.settingsItem:
                Intent openSettingsIntent = new Intent(context, SettingsActivity.class);
                openSettingsIntent.putExtra(IntentFields.WORLD_NAME, worldName);
                context.startActivity(openSettingsIntent);
        }
    }

    /**
     * Sets a TextView's attributes such that it will only accept a single line of text, and
     * automatically word-wrap that text to fit in the display.
     * @param editText The TextView to modify.
     */
    public static void enableWordWrapOnSingleLineEditText(EditText editText) {
        editText.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editText.setSingleLine(true);
        editText.setMaxLines(WORD_WRAP_MAX_LINES);
        editText.setHorizontallyScrolling(false);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    /**
     * Sets up the appearance of the SearchView within a Menu.
     * @param context The Context calling this method.
     * @param menu The Menu that contains the SearchView.
     * @param hint The hint that will be displayed in the search text box.
     */
    public static void setUpSearchViewAppearance(Context context, Menu menu, String hint) {
        SearchView searchView = (SearchView) menu.findItem(R.id.searchArticles).getActionView();

        searchView.setQueryHint(hint);

        SearchView.SearchAutoComplete searchAutoComplete = (
                SearchView.SearchAutoComplete) searchView.findViewById(
                R.id.search_src_text);
        searchAutoComplete.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        searchAutoComplete.setHintTextColor(AttributeGetter.getColorAttribute(context, R.attr.colorPrimaryDark));

        ImageView searchCloseIcon = (ImageView)searchView.findViewById(R.id.search_close_btn);
        searchCloseIcon.setColorFilter(android.R.color.white, PorterDuff.Mode.SRC_ATOP);

        matchSearchViewCursorColorToText(context, searchView);
    }

    /**
     * Recolors a SearchView's cursor color to match the text color.
     * @param context The The Context calling this method.
     * @param searchView The SearchView whose cursor will be recolored.
     */
    private static void matchSearchViewCursorColorToText(Context context, SearchView searchView) {
        final EditText searchInput = ((EditText) searchView.findViewById(
                R.id.search_src_text));

        try {
            Field cursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            cursorDrawableRes.setAccessible(true);
            cursorDrawableRes.set(searchInput, 0);// set textCursorDrawable to null
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Allows a SearchView within a given Menu to filter out the items in the specified StringListAdapter.
     * @param menu The Menu containing the SearchView
     * @param adapter The StringListAdapter whose items will be filtered
     */
    public static void setSearchViewFiltering(Menu menu, StringListAdapter adapter) {
        SearchView searchView = (SearchView) menu.findItem(R.id.searchArticles).getActionView();
        final StringListAdapter stringListAdapter = adapter;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                stringListAdapter.filterQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                stringListAdapter.filterQuery(newText);
                return true;
            }
        });
    }

    /**
     * Recursively searches through all Views in a ViewGroup, and sets any EditTexts found
     * to be either editable or non-editable.
     *
     * @param rootView The ViewGroup whose entire child View hierarchy will be searched
     * @param enabled Set to true if EditTexts should be editable; false if they should be
     *                non-editable
     */
    public static void toggleAllEditTexts(ViewGroup rootView, boolean enabled) {
        for (int i = 0; i < rootView.getChildCount(); i++) {
            final View currentView = rootView.getChildAt(i);
            if (currentView instanceof ViewGroup) {
                ActivityUtilities.toggleAllEditTexts((ViewGroup) currentView, enabled);
            } else if (currentView instanceof EditText) {
                currentView.setFocusable(enabled);
                currentView.setFocusableInTouchMode(enabled);
            }
        }
    }

    /**
     * Returns an AlertDialog Builder that produces a dialog with the correct background color
     * depending on whether Night Mode is enabled or not.
     *
     * @param context The Context in which the AlertDialog is going to be displayed
     * @param nightModeIsEnabled Set to true if Night Mode has been enabled by the user`
     * @return An AlertDialog Builder that produces a dialog with a dark background if Night Mode
     *         is currently enabled, or a white background if it isn't enabled
     */
    public static AlertDialog.Builder getThemedDialogBuilder(Context context,
                                                             boolean nightModeIsEnabled) {
        AlertDialog.Builder builder;
        if (nightModeIsEnabled) {
            builder = new AlertDialog.Builder(context, R.style.NightModeDialog);
        } else {
            builder = new AlertDialog.Builder(context, R.style.NormalDialog);
        }

        return builder;
    }

    public static AlertDialog.Builder buildExceptionDialog(Context context,
                                                           String stackTraceString,
                                                           DialogInterface.OnDismissListener onClose) {
        String message = context.getResources().getString(R.string.exceptionDialogUserInstructions)
                + "\n\n" + stackTraceString;
        return new AlertDialog.Builder(context, R.style.NormalDialog)
                .setTitle(R.string.exceptionDialogTitle)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> { })
                .setOnDismissListener(onClose);
    }

    public static final List<String> INVALID_NAME_CHARACTERS = Arrays.asList(
            "/",
            "."
    );

    public static boolean nameHasInvalidCharacters(String name) {
        for (String invalidCharacter : INVALID_NAME_CHARACTERS) {
            if (name.contains(invalidCharacter)) {
                return true;
            }
        }
        return false;
    }

    public static String getInvalidNameCharactersString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < INVALID_NAME_CHARACTERS.size(); i++) {
            stringBuilder.append(INVALID_NAME_CHARACTERS.get(i));
            if (i < INVALID_NAME_CHARACTERS.size() - 1) {
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

}
