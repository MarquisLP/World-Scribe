package com.averi.worldscribe.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.activities.ArticleListActivity;

import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.AttributeGetter;

public class BottomBar extends RelativeLayout {

    private Animation slideOutAnimation;
    private Animation slideInAnimation;

    private LayoutInflater inflater;
    private BottomBarActivity activity;

    public BottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!(isInEditMode())) {
            this.activity = (BottomBarActivity) context;

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.activity_bottom_bar, this, true);

            slideOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom);
            slideInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_bottom);

            this.findViewById(R.id.peopleButton).setOnClickListener(peopleOnClickListener);
            this.findViewById(R.id.groupsButton).setOnClickListener(groupsOnClickListener);
            this.findViewById(R.id.placesButton).setOnClickListener(placesOnClickListener);
            this.findViewById(R.id.itemsButton).setOnClickListener(itemsOnClickListener);
            this.findViewById(R.id.conceptsButton).setOnClickListener(conceptsOnClickListener);
        }
    }

    private OnClickListener peopleOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            activity.respondToBottomBarButton(Category.Person);
        }
    };

    private OnClickListener groupsOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            activity.respondToBottomBarButton(Category.Group);
        }
    };

    private OnClickListener placesOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            activity.respondToBottomBarButton(Category.Place);
        }
    };

    private OnClickListener itemsOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            activity.respondToBottomBarButton(Category.Item);
        }
    };

    private OnClickListener conceptsOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            activity.respondToBottomBarButton(Category.Concept);
        }
    };

    public void highlightCategoryButton(Context context, Category category) {
        ImageButton categoryButton;

        unhighlightAllButtons(context);

        switch (category) {
            case Person:
                categoryButton = (ImageButton) findViewById(R.id.peopleButton);
                break;
            case Group:
                categoryButton = (ImageButton) findViewById(R.id.groupsButton);
                break;
            case Place:
                categoryButton = (ImageButton) findViewById(R.id.placesButton);
                break;
            case Item:
                categoryButton = (ImageButton) findViewById(R.id.itemsButton);
                break;
            case Concept:
            default:
                categoryButton = (ImageButton) findViewById(R.id.conceptsButton);
        }

        int selectedButtonColor = AttributeGetter.getColorAttribute(context, R.attr.colorPrimaryDark);
        categoryButton.setBackgroundColor(selectedButtonColor);
    }

    /**
     * Changes the background color of all buttons to their normal, unhighlighted color.
     * @param context The Context containing this BottomBar.
     */
    private void unhighlightAllButtons(Context context) {
        int normalButtonColor = AttributeGetter.getColorAttribute(context, R.attr.colorPrimary);
        (findViewById(R.id.peopleButton)).setBackgroundColor(normalButtonColor);
        (findViewById(R.id.groupsButton)).setBackgroundColor(normalButtonColor);
        (findViewById(R.id.placesButton)).setBackgroundColor(normalButtonColor);
        (findViewById(R.id.itemsButton)).setBackgroundColor(normalButtonColor);
        (findViewById(R.id.conceptsButton)).setBackgroundColor(normalButtonColor);
    }

    /**
     * Translates the bar down off the bottom edge of the screen so that it is not visible.
     */
    public void slideOut() {
        this.startAnimation(slideOutAnimation);
        this.setVisibility(GONE);
    }

    /**
     * Translates the bar up from the bottom edge of the screen so that it is visible.
     */
    public void slideIn() {
        this.setVisibility(VISIBLE);
        this.startAnimation(slideInAnimation);
    }

}