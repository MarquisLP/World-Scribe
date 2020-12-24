package com.averi.worldscribe.activities;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.averi.worldscribe.Connection;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.AttributeGetter;
import com.averi.worldscribe.utilities.TaskRunner;
import com.averi.worldscribe.utilities.ThemedSnackbar;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.tasks.SaveConnectionTask;

public class EditConnectionActivity extends BackButtonActivity {

    private Connection connection;

    private CoordinatorLayout layoutRoot;
    private TextView mainArticleNameText;
    private EditText mainArticleRelationText;
    private TextView otherArticleNameText;
    private EditText otherArticleRelationText;
    private LinearLayout mainLayout;
    private LinearLayout loadingLayout;

    private boolean mainArticleRelationWasEdited = false;
    private boolean otherArticleRelationWasEdited = false;

    private final TaskRunner taskRunner = new TaskRunner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layoutRoot = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mainArticleNameText = (TextView) findViewById(R.id.textCurrentArticleName);
        mainArticleRelationText = (EditText) findViewById(R.id.editCurrentArticleRelation);
        otherArticleNameText = (TextView) findViewById(R.id.textOtherArticleName);
        otherArticleRelationText = (EditText) findViewById(R.id.editOtherArticleRelation);
        mainLayout = (LinearLayout) findViewById(R.id.linearScreen);
        loadingLayout = (LinearLayout) findViewById(R.id.linearLoadingEditConnection);

        connection = (Connection) getIntent().getSerializableExtra(IntentFields.CONNECTION);
        mainArticleNameText.setText(connection.articleName);
        mainArticleRelationText.setText(connection.articleRelation);
        otherArticleNameText.setText(connection.connectedArticleName);
        otherArticleRelationText.setText(connection.connectedArticleRelation);

        mainArticleRelationText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mainArticleRelationWasEdited = true;
            }
        });

        otherArticleRelationText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                otherArticleRelationWasEdited = true;
            }
        });

        setAppBar();
        matchArrowsColorToTheme();
        ActivityUtilities.enableWordWrapOnSingleLineEditText(mainArticleRelationText);
        ActivityUtilities.enableWordWrapOnSingleLineEditText(otherArticleRelationText);
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_edit_connection;
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
    protected void setAppBar() {
        Toolbar appBar = (Toolbar) findViewById(R.id.my_toolbar);
        if (appBar != null) {
            appBar.setTitle(R.string.editConnectionTitle);
            setSupportActionBar(appBar);
        }

        super.setAppBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveEditItem:
                if (relationFieldsAreNotEmpty()) {
                    saveRelationsIfEdited();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Recolors the Connection arrows to the primary color of the current theme.
     */
    private void matchArrowsColorToTheme() {
        ImageView arrowsView = (ImageView) findViewById(R.id.imageConnectionArrows);
        assert arrowsView != null;
        arrowsView.setColorFilter(AttributeGetter.getColorAttribute(this, R.attr.colorPrimary),
                PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * Checks if both relation fields are non-empty.
     * If one or both are, the user will be prompted to fill in both.
     * @return True if both relation fields are non-empty.
     */
    private boolean relationFieldsAreNotEmpty() {
        boolean bothFieldsAreFilled = ((!(mainArticleRelationText.getText().toString().isEmpty()))
                && (!(otherArticleRelationText.getText().toString().isEmpty())));

        if (!(bothFieldsAreFilled)) {
            ThemedSnackbar.showSnackbarMessage(this, layoutRoot,
                    getString(R.string.emptyRelationError));
        }

        return bothFieldsAreFilled;
    }

    /**
     * Saves each Article's relation to file if they were edited.
     * If either or both relations fail to save, an error message is displayed.
     */
    public void saveRelationsIfEdited() {
        if (mainArticleRelationWasEdited || otherArticleRelationWasEdited) {
            connection.articleRelation = mainArticleRelationText.getText().toString();
            connection.connectedArticleRelation = otherArticleRelationText.getText().toString();

            loadingLayout.setVisibility(View.VISIBLE);
            mainLayout.setVisibility(View.GONE);

            final Activity activity = this;
            taskRunner.executeAsync(new SaveConnectionTask(connection),
                    (result) -> { activity.finish(); },
                    this::displayErrorDialog
                    );
        }
    }

    private void displayErrorDialog(Exception exception) {
        mainLayout.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
        ActivityUtilities.buildExceptionDialog(this,
                Log.getStackTraceString(exception), (dialogInterface -> {})).show();
    }

}
