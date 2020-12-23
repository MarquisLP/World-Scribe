package com.averi.worldscribe.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.averi.worldscribe.Membership;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.AttributeGetter;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.TaskRunner;
import com.averi.worldscribe.utilities.tasks.SaveMembershipTask;

public class EditMembershipActivity extends BackButtonActivity {

    private Membership membership;

    private TextView groupNameText;
    private TextView memberNameText;
    private EditText memberRoleField;
    private LinearLayout mainLayout;
    private LinearLayout loadingLayout;

    private final TaskRunner taskRunner = new TaskRunner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupNameText = (TextView) findViewById(R.id.groupName);
        memberNameText = (TextView) findViewById(R.id.memberName);
        memberRoleField = (EditText) findViewById(R.id.memberRoleField);
        mainLayout = (LinearLayout) findViewById(R.id.linearScreen);
        loadingLayout = (LinearLayout) findViewById(R.id.linearLoadingEditMembership);

        Intent startupIntent = getIntent();
        membership = (Membership) startupIntent.getSerializableExtra(IntentFields.MEMBERSHIP);

        setAppBar();
        setTextFields();
        matchArrowColorToTheme();
        ActivityUtilities.enableWordWrapOnSingleLineEditText(memberRoleField);
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_edit_membership;
    }

    @Override
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.coordinatorLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveEditItem:
                saveMembership();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    @Override
    protected void setAppBar() {
        Toolbar appBar = (Toolbar) findViewById(R.id.my_toolbar);
        if (appBar != null) {
            appBar.setTitle(R.string.editMembershipTitle);
            setSupportActionBar(appBar);
        }

        super.setAppBar();
    }

    /**
     * Recolors the Membership arrow to the primary color of the current theme.
     */
    private void matchArrowColorToTheme() {
        ImageView arrowsView = (ImageView) findViewById(R.id.membershipArrow);
        assert arrowsView != null;
        arrowsView.setColorFilter(AttributeGetter.getColorAttribute(this, R.attr.colorPrimary),
                PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * Saves the Membership in both the Group's and the member's directories.
     * If an error occurs while saving, an error message is displayed.
     */
    private void saveMembership() {
        membership.memberRole = memberRoleField.getText().toString();

        loadingLayout.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);

        final Activity activity = this;
        taskRunner.executeAsync(new SaveMembershipTask(membership),
                (result) -> { activity.finish(); },
                this::displayErrorDialog);
    }

    private void displayErrorDialog(Exception exception) {
        mainLayout.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
        ActivityUtilities.buildExceptionDialog(this,
                Log.getStackTraceString(exception), (dialogInterface -> {})).show();
    }

}
