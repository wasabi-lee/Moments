package com.wasabilee.moments.Data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wasabilee.moments.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;

public class ImageUploadManager {

    // HashMap variable for tracking the upload status.
    // Key: Uri
    // Value: Whether the upload task of this URI is completed or not, regardless of the result of the task.
    HashMap<String, Boolean> mImageUploadChecker = new HashMap<>();

    public interface ImageUploadCallback {
        void onImageUploaded(ImageData result);

        void onError(String message);

        void onImageUploadTasksCompleted();
    }

    private static ImageUploadManager INSTANCE = null;

    private static StorageReference mStorageReference;

    public static ImageUploadManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ImageUploadManager();
        }
        return INSTANCE;
    }

    private ImageUploadManager() {
        mStorageReference = FirebaseStorage.getInstance().getReference();
    }

    @SuppressLint("CheckResult")
    public void uploadImage(Context context, List<ImageData> imageDataList, final ImageUploadCallback imageUploadCallback) {

        if (imageDataList == null || imageDataList.size() == 0)
            return;

        initUploadTaskTracking(imageDataList);

        Observable.fromIterable(imageDataList)
                .flatMap(imageData -> getImageUploadObservable(context, imageData))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageUploadCallback::onImageUploaded,
                        throwable -> {
                            throwable.printStackTrace();
                            imageUploadCallback.onError(context.getString(R.string.unexpected_error));
                        },
                        imageUploadCallback::onImageUploadTasksCompleted);

    }

    private void initUploadTaskTracking(List<ImageData> imageDataList) {
        for (ImageData imageData : imageDataList) {
            mImageUploadChecker.put(imageData.getmUri().toString(), false);
        }
    }

    private Observable<ImageData> getImageUploadObservable(Context context, ImageData imageData) {
        return Observable.create(emitter -> compressImage(emitter, context, imageData));

    }


    @SuppressLint("CheckResult")
    private void compressImage(ObservableEmitter<ImageData> emitter, Context context, ImageData imageData) {
        final File imageToCompress = new File(imageData.getmUri().getPath());
        try {
            getCompressorFlowable(context, imageToCompress, imageData.isThumb())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(compressedImage -> {
                                byte[] imageByeArr = getImageData(compressedImage);
                                uploadToStorage(emitter, imageData, imageByeArr);
                            },
                            throwable -> {
                                throwable.printStackTrace();
                                emitter.onError(new Throwable("Compression Error"));
                            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Flowable<Bitmap> getCompressorFlowable(Context context, File imageToCompress, boolean isThumb) {
        final int quality = isThumb ? 1 : 20;
        final int widthHeight = isThumb ? 200 : -1;
        if (isThumb) {
            return new Compressor(context)
                    .setMaxWidth(widthHeight)
                    .setMaxHeight(widthHeight)
                    .setQuality(quality)
                    .compressToBitmapAsFlowable(imageToCompress);
        } else {
            return new Compressor(context)
                    .setQuality(quality)
                    .compressToBitmapAsFlowable(imageToCompress);
        }

    }


    private byte[] getImageData(Bitmap compressedImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private void uploadToStorage(ObservableEmitter<ImageData> emitter, ImageData imageData, byte[] imageByteArr) {
        final String randomName = UUID.randomUUID().toString();
        String filePath = imageData.isThumb() ? "journal_images/thumbnails" : "journal_images";
        UploadTask uploadTask = mStorageReference.child(filePath)
                .child(randomName + ".png")
                .putBytes(imageByteArr);

        uploadTask.addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                final String downloadUrl = task.getResult().getDownloadUrl().toString();
                imageData.setDownloadUrl(downloadUrl);
                emitter.onNext(imageData);

                mImageUploadChecker.put(imageData.getmUri().toString(), true);
                if (checkIfAllImagesAreUploaed())
                    emitter.onComplete();

            } else {

                final String errorMessage = task.getException().getMessage();
                emitter.onError(new Throwable(errorMessage));

                mImageUploadChecker.put(imageData.getmUri().toString(), true);
                if (checkIfAllImagesAreUploaed())
                    emitter.onComplete();

            }
        });
    }

    private boolean checkIfAllImagesAreUploaed() {
        for (Map.Entry<String, Boolean> entry : mImageUploadChecker.entrySet()) {
            if (!entry.getValue()) return false;
        }
        return true;
    }


}
