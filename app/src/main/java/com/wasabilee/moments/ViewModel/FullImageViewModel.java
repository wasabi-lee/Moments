package com.wasabilee.moments.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.wasabilee.moments.Data.ImageLocalSaveManager;
import com.wasabilee.moments.Data.JournalRepository;
import com.wasabilee.moments.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class FullImageViewModel extends AndroidViewModel implements ImageLocalSaveManager.OnImageLocalSavedListener {

    private Context context;
    private MutableLiveData<Integer> mSnackbarResourceText = new MutableLiveData<>();
    private ObservableBoolean mIsImageLoaded = new ObservableBoolean();
    public ObservableField<String> mImageSource = new ObservableField<>();


    public FullImageViewModel(@NonNull Application application, JournalRepository journalRepository) {
        super(application);
        this.context = application.getApplicationContext();
    }

    public void start(String url) {
        mImageSource.set(url);
    }

    public MutableLiveData<Integer> getmSnackbarResourceText() {
        return mSnackbarResourceText;
    }

    public ObservableBoolean getmIsImageLoaded() {
        return mIsImageLoaded;
    }

    public ObservableField<String> getmImageSource() {
        return mImageSource;
    }


    @BindingAdapter({"image_url"})
    public static void loadDayImage(ImageView view, String imageUrl) {
        Glide.with(view.getContext())
                .load(imageUrl)
                .apply(RequestOptions.fitCenterTransform())
                .into(view);
    }

    @Override
    public void onImageSavedLocally(String uri) {
        mSnackbarResourceText.setValue(R.string.image_saved);
    }

    @Override
    public void onError(int errorMessageResource) {
        mSnackbarResourceText.setValue(errorMessageResource);
    }

    public void downloadImage() {
        ImageLocalSaveManager.getInstance().saveImageExternalStorage(context, mImageSource.get(), this);
    }

    //
//    public void downloadImage() {
//
//        //TODO Get permission
//
//        if (mImageSource.get() == null)
//            return;
//
//        if (!isExternalStorageWritable()) {
//            mSnackbarResourceText.setValue(R.string.sdcard_not_mounted);
//            return;
//        }
//
//        Glide.with(context)
//                .asBitmap()
//                .load(mImageSource.get())
//                .into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                        saveImage(resource);
//                    }
//                });
//
//
//    }
//
//    private boolean isExternalStorageWritable() {
//        String state = Environment.getExternalStorageState();
//        if (Environment.MEDIA_MOUNTED.equals(state)) {
//            return true;
//        }
//        return false;
//    }
//
//    private void saveImage(Bitmap image) {
//        String savedImagePath = null;
//
//        String imageFileName = Calendar.getInstance().getTimeInMillis() + ".png";
//        File storageDir = new File(Environment.getExternalStoragePublicDirectory
//                (Environment.DIRECTORY_PICTURES) + "/Moments");
//
//        boolean success = true;
//
//        if (!storageDir.exists())
//            success = storageDir.mkdirs();
//        if (success) {
//            File imageFile = new File(storageDir, imageFileName);
//            savedImagePath = imageFile.getAbsolutePath();
//            try {
//                OutputStream fOut = new FileOutputStream(imageFile);
//                image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//                fOut.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//                mSnackbarResourceText.setValue(R.string.unexpected_error);
//                return;
//            }
//
//            executeMediaScan(savedImagePath);
//            mSnackbarResourceText.setValue(R.string.image_saved);
//        }
//    }
//
//    private void executeMediaScan(String imagePath) {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File f = new File(imagePath);
//        Uri contentUri = Uri.fromFile(f);
//        mediaScanIntent.setData(contentUri);
//        context.sendBroadcast(mediaScanIntent);
//    }
}
