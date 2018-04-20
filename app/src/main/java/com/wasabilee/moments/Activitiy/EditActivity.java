package com.wasabilee.moments.Activitiy;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.rd.PageIndicatorView;
import com.wasabilee.moments.Fragment.EditDayFragment;
import com.wasabilee.moments.Fragment.EditNightFragment;
import com.wasabilee.moments.ViewModel.EditViewModel;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Adapter.ViewPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditActivity extends AppCompatActivity {

    @BindView(R.id.edit_page_indicator_view)
    PageIndicatorView mPageIndicatorView;
    @BindView(R.id.edit_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.edit_view_pager)
    ViewPager mViewPager;
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
    }

    public static EditViewModel obtainViewModel(FragmentActivity activity) {
        return ViewModelProviders.of(activity).get(EditViewModel.class);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupViewPager() {
        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(ViewPagerAdapter.FARG_POSITION_DAY, new EditDayFragment());
        mPagerAdapter.addFragment(ViewPagerAdapter.FARG_POSITION_NIGHT, new EditNightFragment());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                /* empty */
            }

            @Override
            public void onPageSelected(int position) {
                mPageIndicatorView.setSelection(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                /* empty */
            }
        });
        mPageIndicatorView.setViewPager(mViewPager);
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
}
