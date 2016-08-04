package com.averi.worldscribe.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

import com.averi.worldscribe.Membership;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.IntentFields;

public class EditMembershipActivity extends AppCompatActivity {

    private Membership membership;

    private TextView groupNameText;
    private TextView memberNameText;
    private EditText memberRoleField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_membership);

        groupNameText = (TextView) findViewById(R.id.groupName);
        memberNameText = (TextView) findViewById(R.id.memberName);
        memberRoleField = (EditText) findViewById(R.id.memberRoleField);

        Intent startupIntent = getIntent();
        membership = (Membership) startupIntent.getSerializableExtra(IntentFields.MEMBERSHIP);

        setAppBar();
        setTextFields();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Displays the Group name, member name, and member's role (if it exists) when this Activity is
     * created.
     */
    private void setTextFields() {
        groupNameText.setText(membership.groupName);
        memberNameText.setText(membership.memberName);
        if (membership.memberRole != null) {
            memberRoleField.setText(membership.memberRole);
        }
    }

    /**
     * Set up this Activity's app bar.
     */
    private void setAppBar() {
        Toolbar appBar = (Toolbar) findViewById(R.id.my_toolbar);
        if (appBar != null) {
            appBar.setTitle(R.string.editMembershipTitle);
            setSupportActionBar(appBar);
        }
    }
}
