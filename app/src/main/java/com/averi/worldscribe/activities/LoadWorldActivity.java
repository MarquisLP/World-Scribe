package com.averi.worldscribe.activities;

import android.os.Bundle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.StringListAdapter;
import com.averi.worldscribe.adapters.StringListContext;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.ExternalReader;

import java.util.ArrayList;

public class LoadWorldActivity extends BackButtonActivity implements StringListContext {

    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_load_world;
    }

    @Override
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.coordinatorLayout);
    }

    @Override
    protected void setAppBar() {
        String title = this.getResources().getString(R.string.loadWorldTitle);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(title);

        super.setAppBar();
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
