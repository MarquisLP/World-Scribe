package com.averi.worldscribe;

import android.content.Context;

import com.averi.worldscribe.exceptions.InvalidCategoryNameException;

/**
 * Created by mark on 14/06/16.
 */
public enum Category {
    Person,
    Group,
    Place,
    Item,
    Concept;

    /* We use hard-coded, non-localized English here because these functions are used for file operations.
       For consistency, all files and folders should have the same name on every decide. */

    public String name(Context context) {
        switch (this) {
            case Person:
                return "Person";
            case Group:
                return "Group";
            case Place:
                return "Place";
            case Item:
                return "Item";
            case Concept:
            default:
                return "Concept";
        }
    }

    /* Added for the menu titles of connections, because the text was a mixed of translation and english.
       This function is used in SelectArticleActivity@setAppBar*/

    public String translatedName(Context context) {
        switch (this) {
            case Person:
                return context.getResources().getString(R.string.personText);
            case Group:
                return context.getResources().getString(R.string.groupText);
            case Place:
                return context.getResources().getString(R.string.placeText);
            case Item:
                return context.getResources().getString(R.string.itemsText);
            case Concept:
            default:
                return context.getResources().getString(R.string.conceptText);
        }
    }

    public String pluralName(Context context) {
        switch (this) {
            case Person:
                return "People";
            case Group:
                return "Groups";
            case Place:
                return "Places";
            case Item:
                return "Items";
            case Concept:
            default:
                return "Concepts";
        }
    }

    public static Category getCategoryFromName(Context context, String categoryName) {
        if (categoryName.equals(context.getResources().getString(R.string.personText))) {
            return Person;
        } else if (categoryName.equals(context.getResources().getString(R.string.groupText))) {
            return Group;
        } else if (categoryName.equals(context.getResources().getString(R.string.placeText))) {
            return Place;
        } else if (categoryName.equals(context.getResources().getString(R.string.itemText))) {
            return Item;
        } else if (categoryName.equals(context.getResources().getString(R.string.conceptText))) {
            return Concept;
        } else {
            throw new InvalidCategoryNameException("An invalid Category name was given.");
        }
    }
}
