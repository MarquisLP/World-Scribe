package com.averi.worldscribe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class BeginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);
    }

    private void goToWorldCreation() {
        Intent goToWorldCreationIntent = new Intent(this, CreateWorldActivity.class);
        startActivity(goToWorldCreationIntent);
    }
}
