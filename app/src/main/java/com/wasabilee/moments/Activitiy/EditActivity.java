package com.wasabilee.moments.Activitiy;

import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.rd.PageIndicatorView;
import com.wasabilee.moments.Fragment.EditDayFragment;
import com.wasabilee.moments.Fragment.EditNightFragment;
import com.wasabilee.moments.Utils.SnackbarUtils;
import com.wasabilee.moments.Utils.ViewModelFactory;
import com.wasabilee.moments.ViewModel.EditViewModel;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Adapter.ViewPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private static final String TAG = EditActivity.class.getSimpleName();

    public static final int IMAGE_DETAIL_REQUEST_CODE = 23;

    public static final String IDENTIFIER_RESULT_EXTRA_KEY = "identifier_result_extra_key";
    public static final String IMAGE_STATE_RESULT_EXTRA_KEY = "image_state_extra_key";
    public static final String IMAGE_URI_RESULT_EXTRA_KEY = "image_uri_extra_key";

    @BindView(R.id.edit_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.edit_view_pager)
    ViewPager mViewPager;
    @BindView(R.id.edit_tab_layout)
    TabLayout mTabLayout;
    ViewPagerAdapter mPagerAdapter;

    private EditViewModel mViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        mViewModel = obtainViewModel(this);

        setupToolbar();
        setupViewPager();
        setupSnackbar();
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
        mPagerAdapter.addFragment(ViewPagerAdapter.FARG_POSITION_DAY, new EditDayFragment(), "DAY");
        mPagerAdapter.addFragment(ViewPagerAdapter.FARG_POSITION_NIGHT, new EditNightFragment(), "NIGHT");
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.addOnTabSelectedListener(this);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void setupSnackbar() {
        mViewModel.getSnackbarTextResource().observe(this, integer -> showSnackbar(getString(integer)));
        mViewModel.getSnackbarText().observe(this, this::showSnackbar);
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
                //TODO Save the journal
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSnackbar(String s) {
        Log.d(TAG, "showSnackbar: ");
        SnackbarUtils.showSnackbar(findViewById(android.R.id.content), s);
    }

}
