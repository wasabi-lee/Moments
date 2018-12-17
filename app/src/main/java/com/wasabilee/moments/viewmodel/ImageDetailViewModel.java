package com.wasabilee.moments.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.wasabilee.moments.data.JournalRepository;
import com.wasabilee.moments.R;
import com.wasabilee.moments.utils.navigators.ActivityNavigator;

import static android.app.Activity.RESULT_OK;

public class ImageDetailViewModel extends AndroidViewModel {

    private MutableLiveData<ActivityNavigator> mActivtyNavigator = new MutableLiveData<>();
    private MutableLiveData<Integer> mSnackbarText = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsImageLoaded = new MutableLiveData<>();
    public ObservableField<String> mImageSource = new ObservableField<>();

    public ImageDetailViewModel(Application context, JournalRepository journalRepository) {
        super(context);
    }

    public ObservableField<String> getmImageSource() {
        return mImageSource;
    }

    public void setmImageSource(ObservableField<String> mImageSource) {
        this.mImageSource = mImageSource;
    }

    @BindingAdapter({"mImageSource"})
    public static void loadImage(PhotoView view, String imageSource) {
        Glide.with(view.getContext())
                .load(imageSource)
                .apply(RequestOptions.fitCenterTransform())
                .into(view);
    }

    public MutableLiveData<ActivityNavigator> getmActivtyNavigator() {
        return mActivtyNavigator;
    }

    public MutableLiveData<Boolean> getmIsImageLoaded() {
        return mIsImageLoaded;
    }

    public MutableLiveData<Integer> getmSnackbarText() {
        return mSnackbarText;
    }

    public void getNewImage() {
        mActivtyNavigator.setValue(ActivityNavigator.NEW_IMAGE);
    }

    public void handleLoadedImage(String imageSource) {
        mImageSource.set(imageSource);
        mIsImageLoaded.setValue(true);
    }

    public void deleteImage() {
        mImageSource.set(null);
        mIsImageLoaded.setValue(false);
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                handleLoadedImage(result.getUri().toString());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.getError().printStackTrace();
                mSnackbarText.setValue(R.string.unexpected_error);
            }
        }
    }
}
