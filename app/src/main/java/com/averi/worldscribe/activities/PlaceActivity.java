package com.averi.worldscribe.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;

import com.averi.worldscribe.R;

public class PlaceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        highlightPeopleButton();
    }

    private  void highlightPeopleButton() {
        ImageButton groupButton = (ImageButton) (findViewById(R.id.bottomBar).findViewById(R.id.placesButton));
        groupButton.setBackgroundColor(ContextCompat.getColor(this, R.color.averiBlueHighlight));
    }
}
