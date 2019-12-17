package com.averi.worldscribe.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.averi.worldscribe.R;

public class NextcloudLoginActivity extends BackButtonActivity implements View.OnClickListener {

    public static final String SERVER = "server";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    private EditText Username;
    private EditText Password;
    private EditText Server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button login = (Button)findViewById(R.id.loginButton);
        login.setOnClickListener(this);

        Username = (EditText)findViewById(R.id.username);
        Password = (EditText)findViewById(R.id.password);
        Server = (EditText)findViewById(R.id.serverAddr);

        Server.setText(getIntent().getStringExtra(SERVER));
        Username.setText(getIntent().getStringExtra(USERNAME));
    }

    @Override
    protected void setAppBar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        assert myToolbar != null;
        myToolbar.setTitle(R.string.settingsTitle);
        setSupportActionBar(myToolbar);

        super.setAppBar();
    }

    @Override
    protected int getLayoutResourceID() {
        return R.layout.activity_nextcloud_login;
    }

    @Override
    protected ViewGroup getRootLayout() {
        return (ViewGroup) findViewById(R.id.root);
    }

    @Override
    public void onClick(View v) {

        if(Username.getText().toString().isEmpty() ||
           Password.getText().toString().isEmpty() ||
           Server.getText().toString().isEmpty())
        {
            Toast.makeText(this, getString(R.string.emptyFields), Toast.LENGTH_SHORT).show();
        }
        else
        {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(USERNAME, Username.getText().toString());
            resultIntent.putExtra(PASSWORD, Password.getText().toString());
            resultIntent.putExtra(SERVER, Server.getText().toString());


            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }
}
