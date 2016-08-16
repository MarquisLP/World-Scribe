package com.averi.worldscribe.activities;

import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.StringListAdapter;
import com.averi.worldscribe.adapters.StringListContext;

import java.util.ArrayList;

import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.ExternalReader;

public class LoadWorldActivity extends ThemedActivity implements StringListContext {

    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_world);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        textEmpty = (TextView) findViewById(R.id.empty);

        setupRecyclerView();
        setAppBar();
        populateList();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setAppBar() {
        String title = this.getResources().getString(R.string.loadWorldTitle);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(title);
    }

    private void populateList() {
        ArrayList<String> worldNames = ExternalReader.getWorldList();

        StringListAdapter adapter = new StringListAdapter(this, worldNames);
        recyclerView.setAdapter(adapter);

        if (worldNames.isEmpty()) {
            textEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void respondToListItemSelection(String itemName) {
        ActivityUtilities.goToWorld(this, itemName);
    }
}
