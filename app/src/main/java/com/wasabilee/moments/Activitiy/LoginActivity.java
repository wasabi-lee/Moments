package com.wasabilee.moments.Activitiy;

import android.content.Intent;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.SnackbarUtils;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String SAVED_INSTANCE_KEY_EMAIL = "saved_instance_key_login_email";
    private static final String SAVED_INSTANCE_KEY_PASSWORD = "saved_instance_key_password_email";
    private static final int RC_SIGN_IN = new Random().nextInt(10000);

    @BindView(R.id.login_email_edit)
    EditText mEmailEdit;
    @BindView(R.id.login_password_edit)
    EditText mPasswordEdit;
    @BindView(R.id.login_to_register_text)
    TextView mToRegisterText;
    @BindView(R.id.login_sign_in_button)
    Button mSignInButton;
    @BindView(R.id.login_google_sign_in_button)
    Button mGoogleSignIinButton;
    @BindView(R.id.login_progress_bar)
    ProgressBar mProgressBar;

    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mProgressBar.setVisibility(View.GONE);

        retainSavedInstance(savedInstanceState);
        setListeners();
        configureGoogleSignIn();

    }

    private void retainSavedInstance(Bundle savedInstance) {
        if (savedInstance == null) return;
        String email = savedInstance.getString(SAVED_INSTANCE_KEY_EMAIL);
        String password = savedInstance.getString(SAVED_INSTANCE_KEY_PASSWORD);
        mEmailEdit.setText(email);
        mPasswordEdit.setText(password);
    }


    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
         mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            toMainActivity();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_to_register_text:
                toRegisterActivity();
                break;
            case R.id.login_sign_in_button:
                login();
                break;
            case R.id.login_google_sign_in_button:
                googleLogin();
                break;
        }
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

    private void toRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void login() {
        String loginEmail = mEmailEdit.getText().toString();
        String loginPassword = mPasswordEdit.getText().toString();

        if (checkIfInputValid(loginEmail, loginPassword)) {
            mProgressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(loginEmail, loginPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                toMainActivity();
                            } else {
                                String errorMessage = task.getException().getMessage();
                                SnackbarUtils.showSnackbar(findViewById(android.R.id.content), errorMessage);
                            }
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });
        } else {
            SnackbarUtils.showSnackbar(findViewById(android.R.id.content), getString(R.string.error_login_empty_input));
        }
    }

    private boolean checkIfInputValid(String email, String password) {
        return !TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password);
    }

    private void googleLogin() {
        mProgressBar.setVisibility(View.VISIBLE);
        Intent loginIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(loginIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                            toMainActivity();
                        } else {
                            Log.d(TAG, "onComplete: failure", task.getException());
                            String errorMessage = task.getException().getMessage();
                            SnackbarUtils.showSnackbar(findViewById(android.R.id.content), errorMessage);
                        }
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void setListeners() {
        mSignInButton.setOnClickListener(this);
        mGoogleSignIinButton.setOnClickListener(this);
        mToRegisterText.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.d(TAG, "onActivityResult: Google sign in failed", e);
                SnackbarUtils.showSnackbar(findViewById(android.R.id.content), getString(R.string.error_signin_failure));
                mProgressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString(SAVED_INSTANCE_KEY_EMAIL, mEmailEdit.getText().toString());
        outState.putString(SAVED_INSTANCE_KEY_PASSWORD, mPasswordEdit.getText().toString());
    }
}
