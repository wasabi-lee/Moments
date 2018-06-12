package com.wasabilee.moments.Data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;

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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ImageUploadManager {

    private static final String TAG = ImageUploadManager.class.getSimpleName();

    public static final int SAVE_LOCATION_LOCAL = 3;
    public static final int SAVE_LOCATION_CLOUD = 4;
    public static final int SAVE_LOCATION_LOCAL_AND_CLOUD = 5;

    public interface ImageSaveCallback {
        void onImageSaved(ImageData result);

        void onError(String message);

        void onImageSaveTasksCompleted();

        void onImageLocalSaveTasksCompleted();

        void onImageRemoteSaveTasksCompleted();
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
    public void saveImage(Context context, List<ImageData> imageDataList, final ImageSaveCallback imageSaveCallback, int flag) {

        if (imageDataList == null || imageDataList.size() == 0) {
            imageSaveCallback.onImageSaveTasksCompleted();
            return;
        }

        Observable.fromIterable(imageDataList)
                .flatMap(imageData -> getImageSaveObservable(context, imageData, flag))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageSaveCallback::onImageSaved,
                        throwable -> {
                            throwable.printStackTrace();
                            imageSaveCallback.onError(context.getString(R.string.unexpected_error));
                        },
                        () -> {
                            switch (flag) {
                                case SAVE_LOCATION_LOCAL:
                                    imageSaveCallback.onImageLocalSaveTasksCompleted();
                                    break;
                                case SAVE_LOCATION_CLOUD:
                                    imageSaveCallback.onImageRemoteSaveTasksCompleted();
                                    break;
                                default:
                                    imageSaveCallback.onImageSaveTasksCompleted();
                                    break;
                            }
                        });
    }


    private Observable<ImageData> getImageSaveObservable(Context context, ImageData imageData, int flag) {
        return Observable.create(emitter -> makeImageNetworkCall(emitter, context, imageData, flag));
    }

    private Observable<ImageData> getImageDeletionObservable(ImageData imageData) {
        return Observable.create(emitter -> deleteFromStorage(emitter, imageData));
    }


    private void makeImageNetworkCall(ObservableEmitter<ImageData> emitter, Context context, ImageData imageData, int flag) {
        if (imageData.getAction() == ImageData.ACTION_TYPE_DELETE) {
            deleteFromStorage(emitter, imageData);
        } else {
            startImageSave(emitter, context, imageData, flag);
        }
    }

    private void startImageSave(ObservableEmitter<ImageData> emitter, Context context, ImageData imageData, int flag) {
        final File imageToCompress = new File(imageData.getmUri().getPath());

        Bitmap imageToUpload = getCompressedFile(emitter, context, imageToCompress, imageData.isThumb());
        byte[] imageByteArr = getImageData(imageToUpload);
        imageData.setFileName(getFileName(imageData));

        executeSave(emitter, context, imageData, imageByteArr, flag);

    }

    @SuppressLint("CheckResult")
    private void executeSave(ObservableEmitter<ImageData> emitter, Context context, ImageData imageData,
                             byte[] imageByteArr, int flag) {

        Observable<ImageData> observable = getObservableByTargetLocation(flag, context, imageData, imageByteArr);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(emitter::onNext,
                        emitter::onError,
                        emitter::onComplete);

    }

    private Observable<ImageData> getObservableByTargetLocation(int flag, Context context, ImageData imageData, byte[] imageByteArr) {

        Observable<ImageData> uploadObservable =
                Observable.create(inner_emitter ->
                        uploadToCloudStorage(inner_emitter, imageData, imageByteArr));

        Observable<ImageData> saveObservable =
                Observable.create(inner_emitter ->
                        saveToLocalStorage(inner_emitter, imageData, context, imageByteArr));

        Observable<ImageData> combined =
                Observable.zip(uploadObservable, saveObservable,
                        (uploadedImageData, savedImageData) -> {
                            uploadedImageData.setSavedLocalUri(savedImageData.getSavedLocalUri());
                            return uploadedImageData;
                        });


        switch (flag) {
            case SAVE_LOCATION_LOCAL:
                return saveObservable;
            case SAVE_LOCATION_CLOUD:
                return uploadObservable;
            case SAVE_LOCATION_LOCAL_AND_CLOUD:
                return combined;
            default:
                return combined;
        }
    }


    private String getFileName(ImageData imageData) {
        return imageData.getAction() == ImageData.ACTION_TYPE_DELETE_AND_UPLOAD ?
                imageData.getFileName() : UUID.randomUUID().toString();
    }

    private void saveToLocalStorage(ObservableEmitter<ImageData> emitter, ImageData imageData, Context context, byte[] imageByteArr) {

        ImageLocalSaveManager.getInstance()
                .saveImageInternalStorage(context, imageData, imageByteArr,
                        new ImageLocalSaveManager.OnImageLocalSavedListener() {
                            @Override
                            public void onImageSavedLocally(ImageData result) {
                                emitter.onNext(result);
                                emitter.onComplete();
                            }

                            @Override
                            public void onError(int errorMessageResource) {
                                emitter.onError(new Throwable(context.getString(errorMessageResource)));
                            }
                        });


    }

    private static Bitmap getCompressedFile(ObservableEmitter<ImageData> emitter, Context context, File imageToCompress, boolean isThumb) {
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


    private static byte[] getImageData(Bitmap compressedImage) {
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

    private void uploadToCloudStorage(ObservableEmitter<ImageData> emitter, ImageData imageData, byte[] imageByteArr) {
        String fileName = imageData.getFileName();
        String filePath = imageData.isThumb() ? "journal_images/thumbnails" : "journal_images";
        StorageReference ref = mStorageReference.child(filePath).child(fileName + ".png");
        UploadTask uploadTask = ref.putBytes(imageByteArr);

        uploadTask.addOnSuccessListener(taskSnapshot ->
                ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            final String downloadUrl = uri.toString();
                            imageData.setDownloadUrl(downloadUrl);
                            emitter.onNext(imageData);
                            emitter.onComplete();
                        })
                        .addOnFailureListener(Throwable::printStackTrace))
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
