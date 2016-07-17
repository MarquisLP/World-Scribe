package com.averi.worldscribe.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.MembersAdapter;
import com.averi.worldscribe.views.BottomBar;

public class GroupActivity extends ArticleActivity {

    private RecyclerView membersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        membersList = (RecyclerView) findViewById(R.id.recyclerMembers);

        populateMembers();
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_group;
    }

    @Override
    protected ImageView getImageView() { return (ImageView) findViewById(R.id.imageGroup); }

    @Override
    protected BottomBar getBottomBar() {
        return (BottomBar) findViewById(R.id.bottomBar);
    }

    @Override
    protected RecyclerView getConnectionsRecycler() {
        return (RecyclerView) findViewById(R.id.recyclerConnections);
    }

    @Override
    protected RecyclerView getSnippetsRecycler() {
        return (RecyclerView) findViewById(R.id.recyclerSnippets);
    }

    private void populateMembers() {
        membersList.setLayoutManager(new LinearLayoutManager(this));
        membersList.setAdapter(new MembersAdapter(this, getWorldName(), getArticleName()));
    }
}
