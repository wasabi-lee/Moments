package com.wasabilee.moments.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wasabilee.moments.R;
import com.wasabilee.moments.utils.SnackbarUtils;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccountSetupActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.account_setup_username_edit)
    EditText mUsernameEdit;
    @BindView(R.id.account_setup_start_button)
    Button mStartButton;
    @BindView(R.id.account_setup_progress_bar)
    ProgressBar mProgressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore mFirebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);
        ButterKnife.bind(this);

        mAuth = mAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        setListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            toLoginActivity();
        }
    }

    private void toLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void toMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private void setListeners() {
        mStartButton.setOnClickListener(this);
    }

    private void saveUsernameRemotely() {

        String username = mUsernameEdit.getText().toString();
        String userId = mAuth.getCurrentUser().getUid();

        if (TextUtils.isEmpty(username)) {
            showSnackbarMessage("Please type your username");
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("username", username);
        mFirebaseFirestore.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showSnackbarMessage("Saved username");
                    toMainActivity();
                } else {
                    showSnackbarMessage(task.getException().getMessage());
                }
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showSnackbarMessage(String message) {
        SnackbarUtils.showSnackbar(findViewById(android.R.id.content), message);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.account_setup_start_button:
                saveUsernameRemotely();
                break;
        }
    }
}
