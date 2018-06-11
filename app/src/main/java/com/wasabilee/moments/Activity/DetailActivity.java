package com.wasabilee.moments.Activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.wasabilee.moments.Adapter.ViewPagerAdapter;
import com.wasabilee.moments.Fragment.DetailFragment;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.LoadingDialog;
import com.wasabilee.moments.Utils.Navigators.JournalStateNavigator;
import com.wasabilee.moments.Utils.ViewModelFactory;
import com.wasabilee.moments.ViewModel.DetailViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private static final String TAG = DetailActivity.class.getSimpleName();

    public static final String DETAIL_JOURNAL_ID_EXTRA_KEY = "detail_journal_id_extra_key";
    public static final int TO_EDIT_ACTIVITY_REQUEST_CODE = 27;

    @BindView(R.id.detail_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.detail_tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.detail_view_pager)
    ViewPager mDetailViewPager;

    private String mJournalId;
    private DetailViewModel mViewModel;

    private LoadingDialog mDialog;

    private boolean mFirstRun = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        mJournalId = getIntent().getStringExtra(DETAIL_JOURNAL_ID_EXTRA_KEY);
        mViewModel = obtainViewModel(this);
        mDialog = new LoadingDialog(this);

        setupToolbar();
        setupViewPager();
        setupNavigatorObserver();
        setupTaskLoadingObserver();
    }

    public static DetailViewModel obtainViewModel(FragmentActivity activity) {
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());
        return ViewModelProviders.of(activity, factory).get(DetailViewModel.class);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupViewPager() {
        DetailFragment dayFrag = getFragment(R.layout.fragment_day_detail);
        DetailFragment nightFrag = getFragment(R.layout.fragment_night_detail);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(ViewPagerAdapter.FARG_POSITION_DETAIL_DAY, dayFrag, "DAY");
        adapter.addFragment(ViewPagerAdapter.FARG_POSITION_DETAIL_NIGHT, nightFrag, "NIGHT");
        mDetailViewPager.setAdapter(adapter);
        mTabLayout.addOnTabSelectedListener(this);
        mTabLayout.setupWithViewPager(mDetailViewPager);

    }


    private DetailFragment getFragment(int layoutResource) {
        DetailFragment frag = new DetailFragment();
        Bundle args = new Bundle();
        args.putInt(DetailFragment.DETAIL_FRAG_LAYOUT_RESOURCE_ARG_KEY, layoutResource);
        frag.setArguments(args);
        return frag;
    }

    private void setupNavigatorObserver() {
        mViewModel.getToEditActivityEvent().observe(this, aVoid -> toEditActivity());
        mViewModel.getmJournalImageOpenEvent().observe(this, this::toFullImageActivity);
    }

    private void setupTaskLoadingObserver() {
        mViewModel.getJournalLoadTaskNavigator().observe(this, journalLoadTaskNavigator -> {
            if (journalLoadTaskNavigator != null) {
                switch (journalLoadTaskNavigator) {
                    case LOAD_IN_PROGRESS:
                        showDialog(getString(R.string.journal_loading));
                        break;
                    case LOAD_FAILED:
                        hideDialog();
                        break;
                    case LOAD_SUCCESSFUL:
                        hideDialog();
                        break;
                }
            }
        });

        mViewModel.getJournalDeletionTaskNavigator().observe(this, journalDeletionTaskNavigator -> {
            if (journalDeletionTaskNavigator != null) {
                switch (journalDeletionTaskNavigator) {
                    case DELETION_IN_PROGRESS:
                        showDialog(getString(R.string.journal_deleting));
                        break;
                    case DELETION_FAILED:
                        hideDialog();
                        break;
                    case DELETION_SUCCESSFUL:
                        hideDialog();
                        backToPreviousActivity(JournalStateNavigator.JOURNAL_STATE_DELETED);
                }
            }
        });

    }

    private void showDialog(String message) {
        mDialog.setMessage(message);
        mDialog.show();
    }

    private void hideDialog() {
        if (mDialog.isShowing())
            mDialog.dismiss();
    }


    private void toEditActivity() {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(EditActivity.JOURNAL_ID_EXTRA_KEY, mJournalId);
        startActivityForResult(intent, TO_EDIT_ACTIVITY_REQUEST_CODE);
    }

    private void toFullImageActivity(String imageSource) {
        Intent intent = new Intent(this, FullImageActivity.class);
        intent.putExtra(FullImageActivity.FULL_IMAGE_URL_EXTRA_KEY, imageSource);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mViewModel.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewModel.loadJournal(mJournalId);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mDetailViewPager.setCurrentItem(tab.getPosition());
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
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    private void backToPreviousActivity() {
        Intent returnIntent = new Intent();
        int resultCode = mViewModel.getIsJournalEdited() ?
                JournalStateNavigator.JOURNAL_STATE_EDITED :
                JournalStateNavigator.JOURNAL_STATE_UNCHANGED;
        setResult(resultCode, returnIntent);
        finish();
    }

    private void backToPreviousActivity(int resultCode) {
        Intent returnIntent = new Intent();
        setResult(resultCode, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        backToPreviousActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDialog = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_detail_edit:
                mViewModel.toEditActivity();
                break;
            case R.id.menu_detail_delete:
                mViewModel.deleteJournal();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
