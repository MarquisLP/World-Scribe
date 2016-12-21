package com.averi.worldscribe.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.AttributeGetter;

public class BottomBar extends RelativeLayout {

    private Animation slideOutAnimation;
    private Animation slideInAnimation;

    private LayoutInflater inflater;
    private BottomBarActivity activity;
    private View peopleButton;
    private View groupsButton;
    private View placesButton;
    private View itemsButton;
    private View conceptsButton;

    public BottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!(isInEditMode())) {
            this.activity = (BottomBarActivity) context;

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.activity_bottom_bar, this, true);

            slideOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom);
            slideInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_bottom);

            peopleButton = findViewById(R.id.peopleButton);
            groupsButton = findViewById(R.id.groupsButton);
            placesButton = findViewById(R.id.placesButton);
            itemsButton = findViewById(R.id.itemsButton);
            conceptsButton = findViewById(R.id.conceptsButton);

            this.findViewById(R.id.peopleButton).setOnClickListener(peopleOnClickListener);
            this.findViewById(R.id.groupsButton).setOnClickListener(groupsOnClickListener);
            this.findViewById(R.id.placesButton).setOnClickListener(placesOnClickListener);
            this.findViewById(R.id.itemsButton).setOnClickListener(itemsOnClickListener);
            this.findViewById(R.id.conceptsButton).setOnClickListener(conceptsOnClickListener);
        }
    }

    /**
     * Gets the View acting as the button for a specific Category.
     * @param category The Category whose button will be retrieved
     * @return The button View corresponding to the given Category
     */
    private View getCategoryButton(Category category) {
        View categoryButton;

        switch (category) {
            case Person:
                categoryButton = peopleButton;
                break;
            case Group:
                categoryButton = groupsButton;
                break;
            case Place:
                categoryButton = placesButton;
                break;
            case Item:
                categoryButton = itemsButton;
                break;
            case Concept:
            default:
                categoryButton = conceptsButton;
        }

        return categoryButton;
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

    /**
     * Puts focus on the given Category by highlighting the corresponding button, showing its
     * name text, and unhighlighting and hiding the name text for all other buttons.
     * @param context The Context this BottomBar belongs to
     * @param category The Category whose button will be highlighted
     */
    public void focusCategoryButton(Context context, Category category) {
        unhighlightAllButtons(context);
        highlightCategoryButton(context, category);

        hideAllCategoryButtonText();
        showCategoryButtonText(category);
    }

    /**
     * Changes the color of a Category button's icon so that it is highlighted.
     * @param context The Context this BottomBar belongs to
     * @param category The Category whose button icon will be highlighted
     */
    private void highlightCategoryButton(Context context, Category category) {
        View categoryButton = getCategoryButton(category);
        ImageView buttonIcon = (ImageView) categoryButton.findViewById(R.id.buttonIcon);
        buttonIcon.clearColorFilter();
    }

    /**
     * Changes the icon color of all buttons to their normal, unhighlighted color.
     * @param context The Context containing this BottomBar.
     */
    private void unhighlightAllButtons(Context context) {
        int notSelectedColor = AttributeGetter.getColorAttribute(context,
                R.attr.colorPrimaryDark);

        for (Category category : Category.values()) {
            View categoryButton = getCategoryButton(category);
            ImageView buttonIcon = (ImageView) categoryButton.findViewById(R.id.buttonIcon);
            buttonIcon.setColorFilter(notSelectedColor, PorterDuff.Mode.SRC_ATOP);
        }
    }

    /**
     * Hides the name text for all Category buttons.
     */
    private void hideAllCategoryButtonText() {
        for (Category category : Category.values()) {
            View categoryButton = getCategoryButton(category);
            (categoryButton.findViewById(R.id.buttonText)).setVisibility(GONE);
        }
    }

    /**
     * Displays the name of a Category on its corresponding button.
     * @param category The Category whose name will be displayed
     */
    private void showCategoryButtonText(Category category) {
        View categoryButton = getCategoryButton(category);
        (categoryButton.findViewById(R.id.buttonText)).setVisibility(VISIBLE);
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

    /**
     * Sets whether this BottomBar is visible or not, and whether it takes up space when hidden.
     * @param visibility One of the visibility constants in Android's View class: VISIBLE,
     *                   INVISIBLE, or GONE.
     */
    public void setVisibility(int visibility) {
        (findViewById(R.id.linearBar)).setVisibility(visibility);
    }

}