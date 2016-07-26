package com.averi.worldscribe.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.R;

public class CreateArticleActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner categorySpinner;
    private Category category = Category.Person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_article);

        categorySpinner = (Spinner) findViewById(R.id.categorySelection);

        populateCategorySpinner();
    }

    private void populateCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories_array, R.layout.spinner_item);
        categorySpinner.setAdapter(adapter);
    }

    public void createWorld(View view) {

    }

    // Listeners for the Category Spinner.

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        String categoryName = (String) parent.getItemAtPosition(pos);
        category = Category.getCategoryFromName(this, categoryName);
    }

    public void onNothingSelected(AdapterView<?> parent) {}

}
