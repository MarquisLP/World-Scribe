package com.averi.worldscribe.activities;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.averi.worldscribe.views.BottomBar;
import com.averi.worldscribe.R;

public class PersonActivity extends ArticleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_person;
    }

    @Override
    protected BottomBar getBottomBar() {
        return (BottomBar) findViewById(R.id.bottomBar);
    }

    @Override
    protected RecyclerView getConnectionsRecycler() {
        return (RecyclerView) findViewById(R.id.recyclerConnections);
    }

}
