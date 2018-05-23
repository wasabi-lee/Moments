package com.wasabilee.moments.Activity;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.chrisbanes.photoview.PhotoView;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.SnackbarUtils;
import com.wasabilee.moments.Utils.ViewModelFactory;
import com.wasabilee.moments.ViewModel.FullImageViewModel;
import com.wasabilee.moments.databinding.ActivityFullImageBinding;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullImageActivity extends AppCompatActivity {

    private static final String TAG = FullImageActivity.class.getSimpleName();
    public static final String FULL_IMAGE_URL_EXTRA_KEY = "full_image_url_extra_key";

    @BindView(R.id.full_image_tool_bar)
    Toolbar mToolbar;
    @BindView(R.id.full_image_view)
    PhotoView mPhotoView;

    FullImageViewModel mViewModel;
    boolean isToolbarVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        ActivityFullImageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_full_image);
        ButterKnife.bind(this);
        mViewModel = obtainViewModel(this);
        binding.setViewmodel(mViewModel);

        setupToolbar();
        setupPhotoView();
        setupSnackbar();
        loadImage();

    }

    public static FullImageViewModel obtainViewModel(FragmentActivity activity) {
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());
        return ViewModelProviders.of(activity, factory).get(FullImageViewModel.class);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupPhotoView() {
        mPhotoView.setOnClickListener(v -> {
            animateToolbar();
            Log.d(TAG, "setupPhotoView: isVisible" + isToolbarVisible);
        });
    }

    private void setupSnackbar() {
        mViewModel.getmSnackbarResourceText()
                .observe(this, resourceId
                        -> SnackbarUtils.showSnackbar(findViewById(android.R.id.content), getString(resourceId)));
    }

    private void loadImage() {
        String url = getIntent().getStringExtra(FULL_IMAGE_URL_EXTRA_KEY);
        mViewModel.start(url);
    }


    private void animateToolbar() {
        if (isToolbarVisible) {
            // Hide
            mToolbar.animate().setDuration(150).translationY(-mToolbar.getHeight());
            isToolbarVisible = false;
        } else {
            // Show
            mToolbar.animate().setDuration(150).translationY(0);
            isToolbarVisible = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_full_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_full_image_save:
                mViewModel.downloadImage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
