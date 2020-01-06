package com.averi.worldscribe.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.ActivityUtilities;
import com.averi.worldscribe.utilities.ExternalReader;
import com.averi.worldscribe.utilities.ExternalWriter;
import com.averi.worldscribe.utilities.ThemedSnackbar;

public class CreateWorldActivity extends BackButtonActivity {

    private SharedPreferences preferences = null;
    private Toolbar appBar;
    private EditText editName;
    private Button createButton;
    private CoordinatorLayout coordinatorLayout;
    private String worldAlreadyExistsMessage;
    private String worldCreationErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("com.averi.worldscribe", MODE_PRIVATE);
        appBar = (Toolbar) findViewById(R.id.my_toolbar);
        editName = (EditText) findViewById(R.id.editName);
        createButton = (Button) findViewById(R.id.createButton);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        worldAlreadyExistsMessage = this.getResources().getString(R.string.worldAlreadyExistsText);
        worldCreationErrorMessage = this.getResources().getString(R.string.worldCreationErrorText);

        setAppBar();
        addTextListener();
        ActivityUtilities.enableWordWrapOnSingleLineEditText(editName);
    }

    @Override
    protected void setAppBar() {
        appBar.setTitle(R.string.createWorldTitle);
        setSupportActionBar(appBar);

        super.setAppBar();
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_create_world;
    }

    @Override
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.coordinatorLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //createExternalFolderOnFirstRun();
    }

//    private void createExternalFolderOnFirstRun() {
//        if (preferences.getBoolean("isFirstRun", true)) {
//            ExternalWriter.createAppDirectory();
//            preferences.edit().putBoolean("isFirstRun", false).apply();
//        }
//    }

    private void addTextListener() {
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                activateCreateButtonWhenNameIsNonempty();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void activateCreateButtonWhenNameIsNonempty() {
        if (nameIsEmpty()) {
            createButton.setEnabled(false);
        } else {
            createButton.setEnabled(true);
        }
    }

    private boolean nameIsEmpty() {
        return (getWorldName().length() == 0);
    }

    private String getWorldName() {
        return editName.getText().toString();
    }

    public void clickCreate(View view) {
        String worldName = getWorldName();

        if (ExternalReader.worldAlreadyExists(this, worldName)) {
            showWorldAlreadyExistsMessage();
        } else {
            if (ExternalWriter.createWorldDirectory(this, worldName) != null) {
                saveNewWorldAsLastOpened(worldName);
                ActivityUtilities.goToWorld(this, worldName);
            } else {
                showWorldCreationErrorMessage();
            }
        }
    }

    private void showWorldAlreadyExistsMessage() {
        ThemedSnackbar.showSnackbarMessage(this,coordinatorLayout, worldAlreadyExistsMessage);
    }

    private void showWorldCreationErrorMessage() {
        ThemedSnackbar.showSnackbarMessage(this, coordinatorLayout, worldCreationErrorMessage);
    }

    private void saveNewWorldAsLastOpened(String worldName) {
        preferences.edit().putString("lastOpenedWorldName", worldName).apply();
    }
}
