package com.wasabilee.moments.Activitiy;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wasabilee.moments.Utils.ActivityNavigator;
import com.wasabilee.moments.Utils.JournalNavigator;
import com.wasabilee.moments.Utils.ViewModelFactory;
import com.wasabilee.moments.ViewModel.MainViewModel;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.SnackbarUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements JournalNavigator {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.main_recycler_view)
    RecyclerView mJournalRecyclerView;
    @BindView(R.id.main_first_journal_add_fab)
    FloatingActionButton mAddNewFab;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private MainViewModel mViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mViewModel = obtainViewModel(this);

        setupToolbar();
        setupFab();
        setupObservers();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            toLoginActivity();
        } else {
            checkIfUsernameSet(mAuth.getCurrentUser().getUid());
        }
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);
    }

    private void setupFab() {
        mAddNewFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.addNewJournal();
            }
        });
    }

    private MainViewModel obtainViewModel(FragmentActivity activity) {
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());
        return ViewModelProviders.of(activity, factory).get(MainViewModel.class);
    }

    private void setupObservers() {
        setupActivityTransitionObserver();
        setupEditJournalObserver();
    }

    private void setupActivityTransitionObserver() {
        mViewModel.getActivityChangeLiveData().observe(this, new Observer<ActivityNavigator>() {
            @Override
            public void onChanged(@Nullable ActivityNavigator activityNavigator) {
                if (activityNavigator != null) {
                    switch (activityNavigator) {
                        case NEW_JOURNAL:
                            toEditActivityForNewJouranl();
                            break;
                        case SETTINGS:
                            toSettingsActivity();
                            break;
                        case LOGOUT:
                            logout();
                            break;
                    }
                }
            }
        });
    }

    private void setupEditJournalObserver() {
        // TODO Transition to EditActivity.class with the Journal Id to edit it.
    }


    private void toLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void toAccountSetupActivity() {
        Intent intent = new Intent(this, AccountSetupActivity.class);
        startActivity(intent);
        finish();
    }

    private void toEditActivityForNewJouranl() {
        Intent intent = new Intent(this, EditActivity.class);
        startActivity(intent);
    }

    private void toSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void checkIfUsernameSet(String userId) {
        mFirebaseFirestore.collection("Users")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().exists()) {
                        toAccountSetupActivity();
                    }
                } else {
                    String errorMessage = task.getException().getMessage();
                    SnackbarUtils.showSnackbar(findViewById(android.R.id.content), errorMessage);
                }
            }
        });
    }

    private void logout() {
        mAuth.signOut();
        toLoginActivity();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_compose:
                mViewModel.addNewJournal();
                return true;
            case R.id.menu_main_logout:
                mViewModel.toLogout();
                return true;
            case R.id.menu_main_settings:
                mViewModel.toSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void addNewJournal() {
        Intent intent = new Intent(this, EditActivity.class);
        startActivity(intent);
    }
}
