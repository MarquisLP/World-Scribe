package com.averi.worldscribe.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.AppPreferences;

public class CreateArticleActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final int PERSON_ITEM_POSITION = 0;
    public static final int GROUP_ITEM_POSITION = 1;
    public static final int PLACE_ITEM_POSITION = 2;
    public static final int ITEM_ITEM_POSITION = 3;
    public static final int CONCEPT_ITEM_POSITION = 4;

    private Spinner categorySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_article);

        categorySpinner = (Spinner) findViewById(R.id.categorySelection);

        populateCategorySpinner();
        selectInitialCategory();
    }

    private void populateCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories_array, R.layout.spinner_item);
        categorySpinner.setAdapter(adapter);
    }

    /**
     * Select the initial Category to display in the spinner, based on the Category of the
     * previous activity.
     */
    private void selectInitialCategory() {
        Category previousCategory = (Category) getIntent().getSerializableExtra(
                AppPreferences.CATEGORY);
        switch (previousCategory) {
            case Person:
                categorySpinner.setSelection(PERSON_ITEM_POSITION);
                break;
            case Group:
                categorySpinner.setSelection(GROUP_ITEM_POSITION);
                break;
            case Place:
                categorySpinner.setSelection(PLACE_ITEM_POSITION);
                break;
            case Item:
                categorySpinner.setSelection(ITEM_ITEM_POSITION);
                break;
            case Concept:
            default:
                categorySpinner.setSelection(CONCEPT_ITEM_POSITION);
                break;
        }
    }

    public void createWorld(View view) {

    }

    // Listeners for the Category Spinner.

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        String categoryName = (String) parent.getItemAtPosition(pos);
    }

    public void onNothingSelected(AdapterView<?> parent) {}

}
