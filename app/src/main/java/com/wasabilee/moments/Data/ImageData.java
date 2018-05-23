package com.wasabilee.moments.Data;

import android.net.Uri;

public class ImageData {


    private Uri mUri;
    private boolean isDay;
    private boolean isThumb;
    private String downloadUrl;
    private String uploadedFileName;

    private int action = -1;

    public static final int ACTION_TYPE_UPLOAD = 0;
    public static final int ACTION_TYPE_DELETE = 1;
    public static final int ACTION_TYPE_DELETE_AND_UPLOAD = 2;

    public ImageData(String uploadedFileName, boolean isThumb) {
        this.uploadedFileName = uploadedFileName;
        this.isThumb = isThumb;
        this.action = getUploadAction();
    }


    public ImageData(Uri mUri, boolean isDay, boolean isThumb, String fileName) {
        this.mUri = mUri;
        this.isDay = isDay;
        this.isThumb = isThumb;
        this.uploadedFileName = fileName;
        this.action = getUploadAction();
    }

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }

    public Uri getmUri() {
        return mUri;
    }

    public void setmUri(Uri mUri) {
        this.mUri = mUri;
    }

    public boolean isDay() {
        return isDay;
    }

    public void setDay(boolean day) {
        isDay = day;
    }

    public boolean isThumb() {
        return isThumb;
    }

    public void setThumb(boolean thumb) {
        isThumb = thumb;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    private int getUploadAction() {
        if (uploadedFileName == null && mUri != null) {
            return ACTION_TYPE_UPLOAD;
        } else if (uploadedFileName != null && mUri == null) {
            return ACTION_TYPE_DELETE;
        } else {
            return ACTION_TYPE_DELETE_AND_UPLOAD;
        }
    }

    public int getAction() {
        return action;
    }
}
