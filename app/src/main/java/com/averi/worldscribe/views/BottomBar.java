package com.averi.worldscribe.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.activities.ArticleListActivity;

import com.averi.worldscribe.utilities.AppPreferences;
import com.averi.worldscribe.utilities.AttributeGetter;

public class BottomBar extends RelativeLayout {

    private LayoutInflater inflater;
    private Context context;

    public BottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.activity_bottom_bar, this, true);

        this.findViewById(R.id.peopleButton).setOnClickListener(peopleOnClickListener);
        this.findViewById(R.id.groupsButton).setOnClickListener(groupsOnClickListener);
        this.findViewById(R.id.placesButton).setOnClickListener(placesOnClickListener);
        this.findViewById(R.id.itemsButton).setOnClickListener(itemsOnClickListener);
        this.findViewById(R.id.conceptsButton).setOnClickListener(conceptsOnClickListener);
    }

    private OnClickListener peopleOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            goToCategoryList(Category.Person);
        }
    };

    private OnClickListener groupsOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            goToCategoryList(Category.Group);
        }
    };

    private OnClickListener placesOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            goToCategoryList(Category.Place);
        }
    };

    private OnClickListener itemsOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            goToCategoryList(Category.Item);
        }
    };

    private OnClickListener conceptsOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            goToCategoryList(Category.Concept);
        }
    };

    private void goToCategoryList(Category category) {
        Intent intent = new Intent(context, ArticleListActivity.class);
        intent.putExtra(AppPreferences.WORLD_NAME,
                ((Activity) context).getIntent().getStringExtra(AppPreferences.WORLD_NAME));
        intent.putExtra("category", category);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    public void highlightCategoryButton(Context context, Category category) {
        ImageButton categoryButton;

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
            default: //Concept
                categoryButton = (ImageButton) findViewById(R.id.conceptsButton);
        }

        int selectedButtonColor = AttributeGetter.getColorAttribute(context, R.attr.colorPrimaryDark);
        categoryButton.setBackgroundColor(selectedButtonColor);
    }

}