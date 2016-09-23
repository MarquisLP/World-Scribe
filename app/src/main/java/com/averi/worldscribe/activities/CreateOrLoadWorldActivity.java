package com.averi.worldscribe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.averi.worldscribe.R;

public class CreateOrLoadWorldActivity extends ThemedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_create_or_load_world;
    }

    @Override
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.root);
    }

    public void clickCreate(View view ) {
        Intent goToWorldCreationIntent = new Intent(this, CreateWorldActivity.class);
        startActivity(goToWorldCreationIntent);
    }

    public void clickLoad(View view) {
        Intent goToLoadWorldIntent = new Intent(this, LoadWorldActivity.class);
        startActivity(goToLoadWorldIntent);
    }
}
