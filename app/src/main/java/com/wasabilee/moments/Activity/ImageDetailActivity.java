package com.wasabilee.moments.Activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.SnackbarUtils;
import com.wasabilee.moments.Utils.ViewModelFactory;
import com.wasabilee.moments.ViewModel.ImageDetailViewModel;
import com.wasabilee.moments.databinding.ActivityImageDetailBinding;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageDetailActivity extends AppCompatActivity {

    private static final String TAG = ImageDetailActivity.class.getSimpleName();

    public static final int IMAGE_STATE_UNCHANGED = 1;
    public static final int IMAGE_STATE_NEW_IMAGE = 2;
    public static final int IMAGE_STATE_DELETED = 3;

    private boolean isImageChanged = false;
    private boolean isImageDeleted = false;

    public static final String IMAGE_URI_EXTRA_KEY = "image_uri_extra_key";
    public static final String IDENTIFIER_EXTRA_KEY = "identifier_extra_key";

    @BindView(R.id.image_detail_image_view)
    PhotoView mImageView;
    @BindView(R.id.image_detail_bottom_nav)
    BottomNavigationView mBottomNav;
    @BindView(R.id.image_detail_back_button)
    ImageView mBackButton;

    private ImageDetailViewModel mViewModel;
    private Uri mUri;
    private String mIdentifier;

    private boolean areIconsVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityImageDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_image_detail);
        ButterKnife.bind(this);
        mViewModel = obtainViewModel(this);
        binding.setViewmodel(mViewModel);

        mUri = unpackUriExtra();
        mIdentifier = unpackIdentifier();

        setupBackButton();
        setupImageView();
        setupBottomNav();
        setupImageLoadStateObserver();
        setupActivityTransitionObserver();
        setupSnackbar();
    }


    public static ImageDetailViewModel obtainViewModel(FragmentActivity activity) {
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());
        return ViewModelProviders.of(activity, factory).get(ImageDetailViewModel.class);
    }


    private Uri unpackUriExtra() {
        String imageUriStr = getIntent().getStringExtra(IMAGE_URI_EXTRA_KEY);
        return imageUriStr != null ? Uri.parse(imageUriStr) : null;
    }

    private String unpackIdentifier() {
        return getIntent().getStringExtra(IDENTIFIER_EXTRA_KEY);
    }

    private void setupBackButton() {
        mBackButton.setOnClickListener(v -> onBackPressed());
    }

    private void setupImageView() {
        mViewModel.handleLoadedImage(mUri);
        mImageView.setOnClickListener(v -> animateIcons());
    }

    private void setupBottomNav() {
        mBottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.image_detail_new_image:
                    //TODO Implement new image
                    mViewModel.getNewImage();
                    return true;
                case R.id.image_detail_crop:
                    //TODO Implement crop function
                    mViewModel.cropImage();
                    return true;
                case R.id.image_detail_delete:
                    //TODO Implement deletion
                    mViewModel.deleteImage();
                    return true;
            }
            return false;
        });
    }

    private void setupImageLoadStateObserver() {
        mViewModel.getmIsImageLoaded().observe(this, isImageLoaded -> {
            if (isImageLoaded != null && !isImageLoaded) {
                isImageDeleted = true;
                // Set result to "IMAGE_DELETED" and finish the activity.
                Intent returnIntent = createReturnIntent();
                setResult(IMAGE_STATE_DELETED, returnIntent);
                finish();
            }
        });
    }

    private void setupActivityTransitionObserver() {
        mViewModel.getmActivtyNavigator().observe(this, activityNavigator -> {
            if (activityNavigator != null)
                switch (activityNavigator) {
                    case NEW_IMAGE:
                        toNewImageActivity();
                        break;
                    case CROP_IMAGE:
                        toCropImageActivity();
                        break;
                }
        });
    }

    private void setupSnackbar() {
        mViewModel.getmSnackbarText().observe(this, message -> {
            if (message != null) showSnackbar(getString(message));
        });
    }

    private void toNewImageActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    private void toCropImageActivity() {
        CropImage.activity(mViewModel.getmImageUri().get())
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mViewModel.handleActivityResult(requestCode, resultCode, data);
        if (data != null) isImageChanged = true;
    }

    private Bundle createResultBundle() {

        Bundle results = new Bundle();

        if (mIdentifier != null)
            results.putString(EditActivity.IDENTIFIER_RESULT_EXTRA_KEY, mIdentifier);

        if (isImageDeleted) {
            return results;
        }

        if (isImageChanged) {
            if (mViewModel.getmImageUri().get() != null)
                results.putString(EditActivity.IMAGE_URI_RESULT_EXTRA_KEY, mViewModel.getmImageUri().get().toString());
        }
        return results;
    }

    private void animateIcons() {
        if (areIconsVisible) {
            // Hide bottom nav
            mBottomNav.animate().setDuration(150).translationY(mBottomNav.getHeight());
            mBackButton.animate().setDuration(150).alpha(0f);
            areIconsVisible = false;
        } else {
            // Show bottom nav
            mBottomNav.animate().setDuration(150).translationY(0);
            mBackButton.animate().setDuration(150).alpha(1f);
            areIconsVisible = true;
        }
    }

    private void showSnackbar(String s) {
        SnackbarUtils.showSnackbar(findViewById(android.R.id.content), s);
    }

    private Intent createReturnIntent() {
        Intent returnIntent = new Intent();
        returnIntent.putExtras(createResultBundle());
        return returnIntent;
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = createReturnIntent();
        if (isImageChanged) {
            setResult(IMAGE_STATE_NEW_IMAGE, returnIntent);
        } else {
            setResult(IMAGE_STATE_UNCHANGED, returnIntent);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_detail, menu);
        return true;
    }
}
