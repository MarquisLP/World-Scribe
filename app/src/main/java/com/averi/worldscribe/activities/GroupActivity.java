package com.averi.worldscribe.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.averi.worldscribe.ArticleTextField;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.Membership;
import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.MembersAdapter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.views.BottomBar;

import java.util.ArrayList;

public class GroupActivity extends ArticleActivity {

    public static final int RESULT_NEW_MEMBER = 300;

    private RecyclerView membersList;
    private Button addMemberButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        membersList = (RecyclerView) findViewById(R.id.recyclerMembers);
        addMemberButton = (Button) findViewById(R.id.buttonAddMember);

        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { addMember(); }
        });
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
    protected Button getAddConnectionButton() {
        return (Button) findViewById(R.id.buttonAddConnection);
    }

    @Override
    protected RecyclerView getSnippetsRecycler() {
        return (RecyclerView) findViewById(R.id.recyclerSnippets);
    }

    @Override
    protected ArrayList<ArticleTextField> getTextFields() {
        Resources resources = getResources();
        ArrayList<ArticleTextField> textFields = new ArrayList<>();

        textFields.add(new ArticleTextField(resources.getString(R.string.mandateText),
                (EditText) findViewById(R.id.editMandate),
                this, getWorldName(), Category.Group, getArticleName()));
        textFields.add(new ArticleTextField(resources.getString(R.string.historyHint),
                (EditText) findViewById(R.id.editHistory),
                this, getWorldName(), Category.Group, getArticleName()));

        return textFields;
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateMembers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_NEW_MEMBER:
                if (resultCode == RESULT_OK) {
                    Membership newMembership = new Membership();
                    newMembership.worldName = getWorldName();
                    newMembership.groupName = getArticleName();
                    newMembership.memberName = data.getStringExtra(IntentFields.ARTICLE_NAME);

                    Intent editMembershipIntent = new Intent(this, EditMembershipActivity.class);
                    editMembershipIntent.putExtra(IntentFields.MEMBERSHIP, newMembership);
                    startActivity(editMembershipIntent);
                }
        }
    }

    private void populateMembers() {
        membersList.setLayoutManager(new LinearLayoutManager(this));
        membersList.setAdapter(new MembersAdapter(this, getWorldName(), getArticleName()));
    }

    /**
     * Opens SelectArticleActivity so the user can select a new Member to add to this Group.
     */
    private void addMember() {
        Intent selectGroupIntent = new Intent(this, SelectArticleActivity.class);
        MembersAdapter membersAdapter = (MembersAdapter) membersList.getAdapter();

        selectGroupIntent.putExtra(IntentFields.WORLD_NAME, getWorldName());
        selectGroupIntent.putExtra(IntentFields.CATEGORY, Category.Person);
        selectGroupIntent.putExtra(IntentFields.MAIN_ARTICLE_CATEGORY, Category.Group);
        selectGroupIntent.putExtra(IntentFields.MAIN_ARTICLE_NAME, getArticleName());
        selectGroupIntent.putExtra(IntentFields.EXISTING_LINKS,
                membersAdapter.getLinkedArticleList());
        startActivityForResult(selectGroupIntent, RESULT_NEW_MEMBER);
    }

}
