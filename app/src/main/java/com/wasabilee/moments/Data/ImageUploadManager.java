package com.wasabilee.moments.Data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wasabilee.moments.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import id.zelory.compressor.Compressor;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

public class ImageUploadManager {

    private static final String TAG = ImageUploadCallback.class.getSimpleName();

    public interface ImageUploadCallback {
        void onImageUploaded(ImageData result);

        void onError(String message);

        void onImageUploadTasksCompleted();
    }

    public interface ImageDeletionCallback {
        void onImageDeleted(ImageData result);

        void onError(String message);

        void onImageDeletionTaskCompleted();
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
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.setMaxUploadRetryTimeMillis(20000);
        mStorageReference = storage.getReference();
    }

    @SuppressLint("CheckResult")
    public void uploadImage(Context context, List<ImageData> imageDataList, final ImageUploadCallback imageUploadCallback) {

        if (imageDataList == null || imageDataList.size() == 0) {
            imageUploadCallback.onImageUploadTasksCompleted();
            return;
        }

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


    private Observable<ImageData> getImageUploadObservable(Context context, ImageData imageData) {
        return Observable.create(emitter -> makeImageNetworkCall(emitter, context, imageData));
    }

    private Observable<ImageData> getImageDeletionObservable(ImageData imageData) {
        return Observable.create(emitter -> deleteFromStorage(emitter, imageData));
    }


    @SuppressLint("CheckResult")
    private void makeImageNetworkCall(ObservableEmitter<ImageData> emitter, Context context, ImageData imageData) {
        if (imageData.getAction() == ImageData.ACTION_TYPE_DELETE) {
            deleteFromStorage(emitter, imageData);
        } else {
            compressImage(emitter, context, imageData);
        }
    }

    @SuppressLint("CheckResult")
    private void compressImage(ObservableEmitter<ImageData> emitter, Context context, ImageData imageData) {
        final File imageToCompress = new File(imageData.getmUri().getPath());
        if (imageToCompress == null) {
            emitter.onError(new Throwable("Image Compression Error: Try again later. "));
        }

        Bitmap imageToUpload = getCompressedFile(emitter, context, imageToCompress, imageData.isThumb());
        byte[] imageByteArr = getImageData(imageToUpload);
        imageData.setFileName(getFileName(imageData));

        Observable<ImageData> uploadObservable = Observable.create(inner_emitter ->
                uploadToStorage(inner_emitter, imageData, imageByteArr));
        Observable<String> saveObservable = Observable.create(inner_emitter ->
                downloadImage(inner_emitter, imageData.getFileName(), context, imageByteArr));

        Observable<ImageData> combined = Observable.zip(uploadObservable, saveObservable,
                (uploadedImageData, localUri) -> {
                    uploadedImageData.setSavedLocalUri(localUri);
                    Log.d(TAG, "compressImage: " + localUri);
                    return uploadedImageData;
                });

        combined.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(emitter::onNext,
                        emitter::onError,
                        emitter::onComplete);

//        uploadToStorage(emitter, imageData, imageByteArr);
    }

    private String getFileName(ImageData imageData) {
        return imageData.getAction() == ImageData.ACTION_TYPE_DELETE_AND_UPLOAD ?
                imageData.getFileName() : UUID.randomUUID().toString();
    }

    private void downloadImage(ObservableEmitter<String> emitter, String fileName, Context context, byte[] imageByteArr) {

        ImageLocalSaveManager.getInstance()
                .saveImageInternalStorage(context, fileName, imageByteArr,
                        new ImageLocalSaveManager.OnImageLocalSavedListener() {
                            @Override
                            public void onImageSavedLocally(String uri) {
                                emitter.onNext(uri);
                                emitter.onComplete();
                            }

                            @Override
                            public void onError(int errorMessageResource) {
                                emitter.onError(new Throwable(context.getString(errorMessageResource)));
                            }
                        });


    }

    private Bitmap getCompressedFile(ObservableEmitter<ImageData> emitter, Context context, File imageToCompress, boolean isThumb) {
        final int quality = isThumb ? 5 : 20;
        final int widthHeight = isThumb ? 200 : -1;
        try {
            if (isThumb) {
                return new Compressor(context)
                        .setMaxWidth(widthHeight)
                        .setMaxHeight(widthHeight)
                        .setQuality(quality)
                        .compressToBitmap(imageToCompress);
            } else {
                return new Compressor(context)
                        .setQuality(quality)
                        .compressToBitmap(imageToCompress);
            }
        } catch (IOException e) {
            e.printStackTrace();
            emitter.onError(e);
            return null;
        }
    }


    private byte[] getImageData(Bitmap compressedImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }


    private void deleteFromStorage(ObservableEmitter<ImageData> emitter, ImageData imageData) {
        if (imageData.getmUri() == null && imageData.getFileName() != null) {
            String fileToDelete = (imageData.isThumb() ? "journal_images/thumbnails/" : "journal_images/")
                    + imageData.getFileName() + ".png";

            mStorageReference.child(fileToDelete)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        imageData.setFileName(null);
                        emitter.onNext(imageData);
                        emitter.onComplete();
                    }).addOnFailureListener(e -> {
                emitter.onError(e);
                emitter.onComplete();
            });

        }

    }

    private void uploadToStorage(ObservableEmitter<ImageData> emitter, ImageData imageData, byte[] imageByteArr) {
        String fileName = imageData.getFileName();
        String filePath = imageData.isThumb() ? "journal_images/thumbnails" : "journal_images";
        StorageReference ref = mStorageReference.child(filePath).child(fileName + ".png");
        UploadTask uploadTask = ref.putBytes(imageByteArr);

        uploadTask.addOnSuccessListener(taskSnapshot ->
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    final String downloadUrl = uri.toString();
                    imageData.setDownloadUrl(downloadUrl);
                    emitter.onNext(imageData);
                    emitter.onComplete();
                }).addOnFailureListener(e -> e.printStackTrace()))
                .addOnFailureListener(e -> {
                    final String errorMessage = e.getMessage();
                    emitter.onError(new Throwable(errorMessage));
                });

    }


    /**
     * Explicit method for other classes to delete images from storage.
     *
     * @param imageDataList List of image data to delete
     * @param callback      callback
     */

    @SuppressWarnings("CheckResult")
    public void deleteFromStorage(List<ImageData> imageDataList, ImageDeletionCallback callback) {
        Observable.fromIterable(imageDataList)
                .flatMap(this::getImageDeletionObservable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback::onImageDeleted,
                        throwable -> {
                            throwable.printStackTrace();
                            callback.onError(throwable.getMessage());
                        },
                        callback::onImageDeletionTaskCompleted);
    }


}
