package com.wasabilee.moments.Activitiy;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;
import com.wasabilee.moments.R;
import com.wasabilee.moments.ViewModel.ImageDetailViewModel;
import com.wasabilee.moments.databinding.ActivityImageDetailBinding;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageDetailActivity extends AppCompatActivity {

    private static final String TAG = ImageDetailActivity.class.getSimpleName();

    public static final int IMAGE_STATE_UNCHANGED = 0;
    public static final int IMAGE_STATE_NEW_IMAGE = 1;
    public static final int IMAGE_STATE_DELETED = 2;

    public static final String IMAGE_URI_EXTRA_KEY = "image_uri_extra_key";

    @BindView(R.id.image_detail_image_view)
    PhotoView mImageView;
    @BindView(R.id.image_detail_bottom_nav)
    BottomNavigationView mBottomNav;

    private ImageDetailViewModel mViewModel;
    private Uri mUri;

    private boolean isBottomNavVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityImageDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_image_detail);
        ButterKnife.bind(this);
        mViewModel = obtainViewModel(this);
        binding.setViewmodel(mViewModel);
        mUri = unpackExtra();

        setupImageView();
    }


    public static ImageDetailViewModel obtainViewModel(FragmentActivity activity) {
        return ViewModelProviders.of(activity).get(ImageDetailViewModel.class);
    }

    private Uri unpackExtra() {
        String imageUriStr = getIntent().getStringExtra(IMAGE_URI_EXTRA_KEY);
        return imageUriStr != null ? Uri.parse(imageUriStr) : null;
    }

    private void setupImageView() {
        mViewModel.getmImageUri().set(mUri);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateBottomNav();
            }
        });
    }

    private void animateBottomNav() {
        if (isBottomNavVisible) {
            // Hide bottom nav
            mBottomNav.animate().setDuration(150).translationY(mBottomNav.getHeight());
            isBottomNavVisible = false;
        } else {
            // Show bottom nav
            mBottomNav.animate().setDuration(150).translationY(0);
            isBottomNavVisible = true;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.image_detail_new_image:
                //TODO Implement new image
                return true;
            case R.id.image_detail_crop:
                //TODO Implement crop function
                return true;
            case R.id.image_detail_delete:
                //TODO Implement deletion
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
