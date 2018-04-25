package com.wasabilee.moments.Data;

import android.net.Uri;

public class ImageData {

    private Uri mUri;
    private boolean isDay;
    private boolean isThumb;
    private String downloadUrl;

    public ImageData(Uri mUri, boolean isDay, boolean isThumb) {
        this.mUri = mUri;
        this.isDay = isDay;
        this.isThumb = isThumb;
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
}
