package com.wasabilee.moments.Activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.wasabilee.moments.Adapter.JournalAdapter;
import com.wasabilee.moments.Utils.LoadingDialog;
import com.wasabilee.moments.Utils.ViewModelFactory;
import com.wasabilee.moments.ViewModel.MainViewModel;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.SnackbarUtils;
import com.wasabilee.moments.databinding.ActivityMainBinding;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int EDIT_ACTIVITY_REQUEST_CODE = 0;
    public static final int DETAIL_ACTIVITY_REQUEST_CODE = 1;
    public static final String JOURNAL_STATE_RESULT_EXTRA_KEY = "journal_state_result_key";

    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.main_recycler_view)
    RecyclerView mJournalRecyclerView;
    @BindView(R.id.main_first_journal_add_fab)
    FloatingActionButton mAddNewFab;

    private LoadingDialog mDialog;
    private boolean mFirstRun = true;

    private FirebaseAuth mAuth;
    private MainViewModel mViewModel;
    private ActivityMainBinding mBinding;

    private JournalAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mViewModel = obtainViewModel(this);
        mBinding.setViewmodel(mViewModel);
        ButterKnife.bind(this);

        mDialog = new LoadingDialog(this);
        mAuth = FirebaseAuth.getInstance();

        setupToolbar();
        setupFab();
        setupSnackbar();
        setupObservers();
        setupRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            toLoginActivity();
        } else {
            mViewModel.setmUid(mAuth.getCurrentUser().getUid());
            mViewModel.checkIfUsernameSet();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (mFirstRun) {
            mViewModel.start();
            mFirstRun = false;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);
    }

    private void setupFab() {
        mAddNewFab.setOnClickListener(v -> mViewModel.addNewJournal());
    }

    private MainViewModel obtainViewModel(FragmentActivity activity) {
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());
        return ViewModelProviders.of(activity, factory).get(MainViewModel.class);
    }

    private void setupSnackbar() {
        mViewModel.getmSnackbarTextResource().observe(this, stringResource -> {
            if (stringResource != null)
                showSnackbar(getString(stringResource));
        });
        mViewModel.getSnackbarText().observe(this, message -> {
            if (message != null)
                showSnackbar(message);
        });
    }

    private void setupObservers() {
        setupActivityTransitionObserver();
        setupOpenJournalDetailObserver();
        setupLoadingObserver();
    }

    private void setupActivityTransitionObserver() {
        mViewModel.getActivityNavigator().observe(this, activityNavigator -> {
            if (activityNavigator != null) {
                switch (activityNavigator) {
                    case ACCOUNT_SETTING:
                        toAccountSetupActivity();
                    case NEW_JOURNAL:
                        toEditActivityForNewJournal();
                        break;
                    case SETTINGS:
                        toSettingsActivity();
                        break;
                    case LOGOUT:
                        logout();
                        break;
                }
            }
        });
    }

    private void setupOpenJournalDetailObserver() {
        mViewModel.getmOpenJournalDetail().observe(this, journalId -> {
            if (journalId != null) {
                toDetailActivity(journalId);
            }
        });
    }


    private void setupLoadingObserver() {
        mViewModel.getmIsDataLoading().observe(this, isDataLoading -> {
            if (isDataLoading != null) {
                mDialog.setMessage(getString(R.string.journal_being_loaded));
                if (isDataLoading) {
                    mDialog.show();
                } else {
                    if (mDialog.isShowing())
                        mDialog.dismiss();
                }
            }
        });
    }

    private void setupRecyclerView() {
        mAdapter = new JournalAdapter(new ArrayList<>(0), mViewModel);

        mJournalRecyclerView.setHasFixedSize(false);
        mJournalRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mJournalRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mJournalRecyclerView.setAdapter(mAdapter);
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

    private void toEditActivityForNewJournal() {
        Intent intent = new Intent(this, EditActivity.class);
        startActivityForResult(intent, EDIT_ACTIVITY_REQUEST_CODE);
    }

    private void toSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void toDetailActivity(String journalId) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.DETAIL_JOURNAL_ID_EXTRA_KEY, journalId);
        startActivityForResult(intent, DETAIL_ACTIVITY_REQUEST_CODE);
    }

    private void logout() {
        mAuth.signOut();
        toLoginActivity();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mViewModel.handleActivityResult(requestCode, resultCode, data);
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

    private void showSnackbar(String s) {
        SnackbarUtils.showSnackbar(findViewById(android.R.id.content), s);
    }

}
