package com.averi.worldscribe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class CreateOrLoadWorldActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_load_world);
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
