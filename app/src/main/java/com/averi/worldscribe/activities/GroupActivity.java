package com.averi.worldscribe.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.averi.worldscribe.ArticleTextField;
import com.averi.worldscribe.Category;
import com.averi.worldscribe.Membership;
import com.averi.worldscribe.R;
import com.averi.worldscribe.adapters.MembersAdapter;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.ExternalDeleter;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.viewmodels.GroupViewModel;
import com.averi.worldscribe.views.ArticleSectionCollapser;
import com.averi.worldscribe.views.BottomBar;

import java.util.ArrayList;

public class GroupActivity extends ArticleActivity {

    public static final int RESULT_NEW_MEMBER = 300;

    private GroupViewModel viewModel;

    private RecyclerView membersList;
    private Button addMemberButton;
    private ProgressBar membersProgressCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        membersList = (RecyclerView) findViewById(R.id.recyclerMembers);
        addMemberButton = (Button) findViewById(R.id.buttonAddMember);
        membersProgressCircle = (ProgressBar)  findViewById(R.id.membersProgressCircle);

        viewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        setupMembersList();
        setupErrorDialog();

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
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.coordinatorLayout);
    }

    @Override
    protected NestedScrollView getNestedScrollView() {
        return (NestedScrollView) findViewById(R.id.scrollView);
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
    protected Button getAddSnippetButton() {
        return (Button) findViewById(R.id.buttonAddSnippet);
    }

    @Override
    protected ArrayList<ArticleTextField> getTextFields() {
        Resources resources = getResources();
        ArrayList<ArticleTextField> textFields = new ArrayList<>();

        textFields.add(new ArticleTextField("Mandate",
                (EditText) findViewById(R.id.editMandate),
                this, getWorldName(), Category.Group, getArticleName()));
        textFields.add(new ArticleTextField("History",
                (EditText) findViewById(R.id.editHistory),
                this, getWorldName(), Category.Group, getArticleName()));

        return textFields;
    }

    @Override
    protected TextView getGeneralInfoHeader() {
        return (TextView) findViewById(R.id.textGeneralInfo);
    }

    @Override
    protected ViewGroup getGeneralInfoLayout() {
        return (LinearLayout) findViewById(R.id.linearGeneralInfo);
    }

    @Override
    protected TextView getConnectionsHeader() {
        return (TextView) findViewById(R.id.textConnections);
    }

    @Override
    protected ViewGroup getConnectionsLayout() {
        return (LinearLayout) findViewById(R.id.linearConnections);
    }

    @Override
    protected TextView getSnippetsHeader() {
        return (TextView) findViewById(R.id.textSnippets);
    }

    @Override
    protected ViewGroup getSnippetsLayout() {
        return (LinearLayout) findViewById(R.id.linearSnippets);
    }

    @Override
    protected ProgressBar getConnectionsProgressCircle() {
        return (ProgressBar) findViewById(R.id.connectionsProgressCircle);
    }

    @Override
    protected ProgressBar getSnippetsProgressCircle() {
        return (ProgressBar) findViewById(R.id.snippetsProgressCircle);
    }

    @Override
    protected ProgressBar getImageProgressCircle() {
        return (ProgressBar) findViewById(R.id.articleImageProgressCircle);
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

    @Override
    protected void addSectionCollapsers() {
        TextView membersHeader = (TextView) findViewById(R.id.textMembers);

        membersHeader.setOnClickListener(new ArticleSectionCollapser(this, membersHeader,
                (LinearLayout) findViewById(R.id.linearMembers)));

        super.addSectionCollapsers();
    }

    private void setupMembersList() {
        MembersAdapter adapter = new MembersAdapter(this, getWorldName(), getArticleName());
        viewModel.getMembers().observe(this, new Observer<ArrayList<Membership>>() {
            @Override
            public void onChanged(ArrayList<Membership> members) {
                if (members == null) {
                    membersProgressCircle.setVisibility(View.VISIBLE);
                    membersList.setVisibility(View.GONE);
                }
                else {
                    adapter.updateList(members);
                    adapter.notifyDataSetChanged();
                    membersList.setVisibility(View.VISIBLE);
                    membersProgressCircle.setVisibility(View.GONE);
                }
            }
        });
        membersList.setAdapter(adapter);
        membersList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupErrorDialog() {
        final Context context = this;
        viewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newErrorMessage) {
                if (!(newErrorMessage.isEmpty())) {
                    ActivityUtilities.buildExceptionDialog(context, newErrorMessage,
                            dialogInterface -> viewModel.clearErrorMessage()
                    ).show();
                }
            }
        });
    }

    private void populateMembers() {
        viewModel.loadMembers(getWorldName(), getArticleName());
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
