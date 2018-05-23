package com.wasabilee.moments.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.net.Uri;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.wasabilee.moments.Data.JournalRepository;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.Navigators.ActivityNavigator;

import static android.app.Activity.RESULT_OK;

public class ImageDetailViewModel extends AndroidViewModel {

    private Context mContext;
    private JournalRepository mJournalRepository;

    public MutableLiveData<ActivityNavigator> mActivtyNavigator = new MutableLiveData<>();
    public MutableLiveData<Integer> mSnackbarText = new MutableLiveData<>();
    public MutableLiveData<Boolean> mIsImageLoaded = new MutableLiveData<>();
    public ObservableField<Uri> mImageUri = new ObservableField<>();

    public ImageDetailViewModel(Application context, JournalRepository journalRepository) {
        super(context);
        mContext = context.getApplicationContext();
        mJournalRepository = journalRepository;
    }

    public ObservableField<Uri> getmImageUri() {
        return mImageUri;
    }

    public void setmImageUri(ObservableField<Uri> mImageUri) {
        this.mImageUri = mImageUri;
    }

    @BindingAdapter({"mImageUri"})
    public static void loadImage(PhotoView view, Uri uri) {
        Glide.with(view.getContext())
                .load(uri)
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

    public void cropImage() {
        mActivtyNavigator.setValue(ActivityNavigator.CROP_IMAGE);
    }

    public void handleLoadedImage(Uri uri) {
        mImageUri.set(uri);
        mIsImageLoaded.setValue(true);
    }

    public void deleteImage() {
        mImageUri.set(null);
        mIsImageLoaded.setValue(false);
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                handleLoadedImage(result.getUri());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.getError().printStackTrace();
                mSnackbarText.setValue(R.string.unexpected_error);
            }
        }
    }
}
