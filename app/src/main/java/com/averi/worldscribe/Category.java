package com.averi.worldscribe;

import android.content.Context;

/**
 * Created by mark on 14/06/16.
 */
public enum Category {
    Person,
    Group,
    Place,
    Item,
    Concept;

    public String name(Context context) {
        switch (this) {
            case Person:
                return context.getResources().getString(R.string.personText);
            case Group:
                return context.getResources().getString(R.string.groupText);
            case Place:
                return context.getResources().getString(R.string.placeText);
            case Item:
                return context.getResources().getString(R.string.itemText);
            case Concept:
            default:
                return context.getResources().getString(R.string.conceptText);
        }
    }

    public String pluralName(Context context) {
        switch (this) {
            case Person:
                return context.getResources().getString(R.string.peopleText);
            case Group:
                return context.getResources().getString(R.string.groupsText);
            case Place:
                return context.getResources().getString(R.string.placesText);
            case Item:
                return context.getResources().getString(R.string.itemsText);
            case Concept:
            default:
                return context.getResources().getString(R.string.conceptsText);
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
