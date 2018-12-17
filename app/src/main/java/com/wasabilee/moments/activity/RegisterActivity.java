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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.wasabilee.moments.R;
import com.wasabilee.moments.utils.SnackbarUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.register_email_edit)
    EditText mEmailEdit;
    @BindView(R.id.register_password_edit)
    EditText mPasswordEdit;
    @BindView(R.id.register_password_confirm_edit)
    EditText mConfirmPasswordEdit;
    @BindView(R.id.register_button)
    Button mRegisterButton;
    @BindView(R.id.register_to_login_text)
    TextView mToLoginText;
    @BindView(R.id.register_progress_bar)
    ProgressBar mProgressBar;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        setListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_button:
                register();
                break;
            case R.id.register_to_login_text:
                toLoginActivity();
                break;
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

    private void toAccountSetupActivity() {
        Intent intent = new Intent(this, AccountSetupActivity.class);
        startActivity(intent);
        finish();
    }

    private void setListeners() {
        mRegisterButton.setOnClickListener(this);
        mToLoginText.setOnClickListener(this);
    }

    private void register() {
        String email = mEmailEdit.getText().toString();
        String password = mPasswordEdit.getText().toString();
        String confirmPassword = mConfirmPasswordEdit.getText().toString();

        if (!isEveryFieldFilled(email, password, confirmPassword)) {
            SnackbarUtils.showSnackbar(findViewById(android.R.id.content), getString(R.string.error_login_empty_input));
            return;
        }

        if (!doesPasswordMatchConfirmPassword(password, confirmPassword)) {
            SnackbarUtils.showSnackbar(findViewById(android.R.id.content), getString(R.string.error_password_confirm_mismatch));
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            toAccountSetupActivity();
                        } else {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
    }


    private boolean isEveryFieldFilled(String email, String password, String confirmPassword) {
        // Is every field filled?
        return !TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password)
                && !TextUtils.isEmpty(confirmPassword);
    }

    private boolean doesPasswordMatchConfirmPassword(String password, String confirmPassword) {
        // Do the password and password confirmation match?
        return TextUtils.equals(password, confirmPassword);
    }
}
