package com.wasabilee.moments.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.github.chrisbanes.photoview.PhotoView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wasabilee.moments.R;
import com.wasabilee.moments.utils.SnackbarUtils;
import com.wasabilee.moments.utils.ViewModelFactory;
import com.wasabilee.moments.viewmodel.ImageDetailViewModel;
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

    public static final String IMAGE_SOURCE_EXTRA_KEY = "image_uri_extra_key";
    public static final String IDENTIFIER_EXTRA_KEY = "identifier_extra_key";

    @BindView(R.id.image_detail_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.image_detail_image_view)
    PhotoView mImageView;

    private ImageDetailViewModel mViewModel;
    private String mImageSource;
    private String mIdentifier;

    private boolean areIconsVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityImageDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_image_detail);
        ButterKnife.bind(this);
        mViewModel = obtainViewModel(this);
        binding.setViewmodel(mViewModel);

        mImageSource = unpackImageSourceExtra();
        mIdentifier = unpackIdentifier();

        setupToolbar();
        setupImageView();
        setupImageLoadStateObserver();
        setupActivityTransitionObserver();
        setupSnackbar();
    }

    // ------------------------------------ INITIALIZATION -------------------------------------

    public static ImageDetailViewModel obtainViewModel(FragmentActivity activity) {
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());
        return ViewModelProviders.of(activity, factory).get(ImageDetailViewModel.class);
    }

    private String unpackImageSourceExtra() {
        return getIntent().getStringExtra(IMAGE_SOURCE_EXTRA_KEY);
    }

    private String unpackIdentifier() {
        return getIntent().getStringExtra(IDENTIFIER_EXTRA_KEY);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupImageView() {
        mViewModel.handleLoadedImage(mImageSource);
        mImageView.setOnClickListener(v -> animateToolbar());
    }

    private void setupImageLoadStateObserver() {
        // Get notified when the image is unloaded (deleted), then finish this activity
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
        // Detect any activity change event
        mViewModel.getmActivtyNavigator().observe(this, activityNavigator -> {
            if (activityNavigator != null)
                switch (activityNavigator) {
                    case NEW_IMAGE:
                        toNewImageActivity();
                        break;
                }
        });
    }

    private void setupSnackbar() {
        mViewModel.getmSnackbarText().observe(this, message -> {
            if (message != null) showSnackbar(getString(message));
        });
    }


    // ------------------------------------ ACTIVITY TRANSITION EVENTS -------------------------------------

    private void toNewImageActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mViewModel.handleActivityResult(requestCode, resultCode, data);
        if (data != null) isImageChanged = true;
    }

    @Override
    public void onBackPressed() {
        // Return to prev activity. Set an appropriate result
        Intent returnIntent = createReturnIntent();
        setResult(isImageChanged ? IMAGE_STATE_NEW_IMAGE : IMAGE_STATE_UNCHANGED, returnIntent);
        super.onBackPressed();
    }

    private Intent createReturnIntent() {
        Intent returnIntent = new Intent();
        returnIntent.putExtras(createResultBundle());
        return returnIntent;
    }

    private Bundle createResultBundle() {
        // Creating the returning intent's result data.
        Bundle results = new Bundle();

        // Putting identifier to confirm the fragment that launched this activity (between DAY / NIGHT)
        if (mIdentifier != null)
            results.putString(EditActivity.IDENTIFIER_RESULT_EXTRA_KEY, mIdentifier);

        // Notifying the previous activity that the current photo is deleted
        if (isImageDeleted) {
            return results;
        }

        // Notifying the previous activity that the current photo is changed
        if (isImageChanged) {
            if (mViewModel.getmImageSource().get() != null)
                results.putString(EditActivity.IMAGE_URI_RESULT_EXTRA_KEY, mViewModel.getmImageSource().get());
        }
        return results;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.image_detail_delete:
                mViewModel.deleteImage();
                break;
            case R.id.image_detail_new_image:
                mViewModel.getNewImage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    // ------------------------------------ UI HANDLING -------------------------------------

    private void animateToolbar() {
        if (areIconsVisible) {
            // Hide
            mToolbar.animate().setDuration(150).alpha(0f);
            areIconsVisible = false;
        } else {
            // Show
            mToolbar.animate().setDuration(150).alpha(1f);
            areIconsVisible = true;
        }
    }

    private void showSnackbar(String s) {
        SnackbarUtils.showSnackbar(findViewById(android.R.id.content), s);
    }

}
