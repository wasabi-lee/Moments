package com.wasabilee.moments.data;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.wasabilee.moments.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

public class ImageLocalSaveManager {
    /**
     * A class that manages image saving tasks for internal storage
     */

    public interface OnImageLocalSavedListener {
        void onImageSavedLocally(ImageData imageData);

        void onError(int errorMessageResource);
    }

    private static ImageLocalSaveManager INSTANCE = null;

    public static ImageLocalSaveManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ImageLocalSaveManager();
        }
        return INSTANCE;
    }

    public void saveImageInternalStorage(Context context, ImageData imageData, byte[] data, OnImageLocalSavedListener listener) {

        //TODO Get permission

        if (data == null) {
            listener.onImageSavedLocally(null);
            return;
        }

        String fileName = imageData.getFileName();
        File directory = context.getFilesDir();
        File file = new File(directory, fileName);

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            listener.onError(R.string.unexpected_error);
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                listener.onError(R.string.unexpected_error);
            }
        }

        imageData.setSavedLocalUri(Uri.fromFile(file).toString());
        listener.onImageSavedLocally(imageData);
        context = null;
    }

    public void saveImageExternalStorage(Context context, String data, OnImageLocalSavedListener listener) {

        //TODO Get permission

        if (data == null) {
            listener.onImageSavedLocally(null);
            return;
        }

        if (!isExternalStorageWritable()) {
            listener.onError(R.string.sdcard_not_mounted);
            return;
        }

        Glide.with(context)
                .asBitmap()
                .load(data)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        saveImageExternalStorage(context, resource, listener);
                    }
                });

    }

    private void saveImageExternalStorage(Context context, Bitmap image, OnImageLocalSavedListener listener) {
        String savedImagePath = null;

        String imageFileName = Calendar.getInstance().getTimeInMillis() + ".png";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES) + "/Moments");

        boolean success = true;

        if (!storageDir.exists())
            success = storageDir.mkdirs();
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
                listener.onError(R.string.unexpected_error);
                return;
            }

            executeMediaScan(context, savedImagePath);
            listener.onImageSavedLocally(new ImageData(Uri.fromFile(imageFile).toString()));
        }

        context = null;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void executeMediaScan(Context context, String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


}
