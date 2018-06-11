package com.wasabilee.moments.Activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.wasabilee.moments.Fragment.EditDayFragment;
import com.wasabilee.moments.Fragment.EditNightFragment;
import com.wasabilee.moments.Utils.Navigators.JournalStateNavigator;
import com.wasabilee.moments.Utils.Navigators.JournalUploadTaskNavigator;
import com.wasabilee.moments.Utils.LoadingDialog;
import com.wasabilee.moments.Utils.SnackbarUtils;
import com.wasabilee.moments.Utils.ViewModelFactory;
import com.wasabilee.moments.ViewModel.EditViewModel;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Adapter.ViewPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wasabilee.moments.Utils.Navigators.JournalStateNavigator.JOURNAL_STATE_LOAD_FAILED;
import static com.wasabilee.moments.Utils.Navigators.JournalStateNavigator.JOURNAL_STATE_UNCHANGED;

public class EditActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private static final String TAG = EditActivity.class.getSimpleName();

    public static final int IMAGE_DETAIL_REQUEST_CODE = 23;

    public static final String JOURNAL_ID_EXTRA_KEY = "journal_id_extra_key";

    public static final String IDENTIFIER_RESULT_EXTRA_KEY = "identifier_result_extra_key";
    public static final String IMAGE_URI_RESULT_EXTRA_KEY = "image_uri_extra_key";

    @BindView(R.id.edit_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.edit_view_pager)
    ViewPager mViewPager;
    @BindView(R.id.edit_tab_layout)
    TabLayout mTabLayout;
    private ViewPagerAdapter mPagerAdapter;
    private LoadingDialog mDialog;

    private EditViewModel mViewModel;
    private String mJournalId;

    private boolean mFirstRun = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        mJournalId = getIntent().getStringExtra(JOURNAL_ID_EXTRA_KEY);
        mViewModel = obtainViewModel(this);

        mDialog = new LoadingDialog(this);

        setupToolbar();
        setupViewPager();
        setupSnackbar();
        setupNetworkTaskObservers();
    }

    public static EditViewModel obtainViewModel(FragmentActivity activity) {
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());
        return ViewModelProviders.of(activity, factory).get(EditViewModel.class);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupViewPager() {
        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(ViewPagerAdapter.FARG_POSITION_EDIT_DAY, new EditDayFragment(), "DAY");
        mPagerAdapter.addFragment(ViewPagerAdapter.FARG_POSITION_EDIT_NIGHT, new EditNightFragment(), "NIGHT");
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.addOnTabSelectedListener(this);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void setupSnackbar() {
        mViewModel.getSnackbarTextResource().observe(this, integer -> showSnackbar(getString(integer)));
        mViewModel.getSnackbarText().observe(this, this::showSnackbar);
    }

    private void setupNetworkTaskObservers() {
        mViewModel.getJournalUploadCompleted().observe(this, journalUploadCompleted -> {
            if (journalUploadCompleted != null) {
                mDialog.setMessage(getString(R.string.journal_being_uploaded));
                if (journalUploadCompleted == JournalUploadTaskNavigator.UPLOAD_IN_PROGRESS) {
                    mDialog.show();
                }

                switch (journalUploadCompleted) {
                    case UPLOAD_IN_PROGRESS:
                        mDialog.show();
                        break;
                    case UPLOAD_SUCCESSFUL:
                        hideDialog();
                        backToPreviousActivity(mJournalId == null ?
                                JournalStateNavigator.JOURNAL_STATE_ADDED_NEW :
                                JournalStateNavigator.JOURNAL_STATE_EDITED);
                        break;
                    case UPLOAD_UNSTABLE_CONNECTION:
                        hideDialog();
                        break;
                    case UPLOAD_FAILED:
                        hideDialog();
                        break;
                }
            }
        });

        mViewModel.getJournalLoadCompleted().observe(this, journalLoadTaskNavigator -> {
            if (journalLoadTaskNavigator != null) {
                mDialog.setMessage(getString(R.string.journal_being_loaded));
                switch (journalLoadTaskNavigator) {
                    case LOAD_IN_PROGRESS:
                        mDialog.show();
                        break;
                    case LOAD_SUCCESSFUL:
                        hideDialog();
                        break;
                    case LOAD_FAILED:
                        hideDialog();
                        backToPreviousActivity(JOURNAL_STATE_LOAD_FAILED);
                        break;
                }
            }
        });
    }

    private void hideDialog() {
        if (mDialog.isShowing())
            mDialog.dismiss();
    }

    private void launchWarningDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(getString(R.string.edit_exit_warning_message))
                .setPositiveButton(R.string.dialog_yes, (dialog, which) ->
                        backToPreviousActivity(JournalStateNavigator.JOURNAL_STATE_UNCHANGED))
                .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> dialog.dismiss());
        dialogBuilder.create().show();
    }


    private void backToPreviousActivity(int resultState) {
        Intent returnIntent = new Intent();
        setResult(resultState, returnIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFirstRun) {
            mViewModel.start(mJournalId);
            mFirstRun = false;
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        /* empty */
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        /* empty */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.edit_save:
                mViewModel.saveJournal();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mViewModel.isBeingComposed()) {
            launchWarningDialog();
        } else {
            backToPreviousActivity(JOURNAL_STATE_UNCHANGED);

        }
    }

    private void showSnackbar(String s) {
        SnackbarUtils.showSnackbar(findViewById(android.R.id.content), s);
    }

}
