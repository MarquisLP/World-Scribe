package com.averi.worldscribe.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.averi.worldscribe.Membership;
import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.IntentFields;

public class EditMembershipActivity extends AppCompatActivity {

    private Membership membership;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_membership);

        Intent startupIntent = getIntent();
        membership = (Membership) startupIntent.getSerializableExtra(IntentFields.MEMBERSHIP);
    }
}
