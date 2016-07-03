package com.averi.worldscribe;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;

public class GroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        highlightGroupButton();
    }

    private  void highlightGroupButton() {
        ImageButton groupButton = (ImageButton) (findViewById(R.id.bottomBar).findViewById(R.id.groupsButton));
        groupButton.setBackgroundColor(ContextCompat.getColor(this, R.color.averiBlueHighlight));
    }
}
