package com.wasabilee.moments.ViewModel;

import android.arch.lifecycle.ViewModel;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;

public class ImageDetailViewModel extends ViewModel {

    public ObservableField<Uri> mImageUri = new ObservableField<>();

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
}
