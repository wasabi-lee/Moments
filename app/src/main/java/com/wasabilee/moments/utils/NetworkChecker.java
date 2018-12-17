package com.wasabilee.moments.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NetworkChecker {

    public interface NetworkCheckerCallback {
        void onNetworkCheckCompleted(boolean isAvailable);
    }

    private static final String TAG = NetworkChecker.class.getSimpleName();
    private static NetworkChecker instance = null;

    private NetworkChecker() {
    }

    public static NetworkChecker getInstance() {
        if (instance == null)
            instance = new NetworkChecker();

        return instance;
    }


    @SuppressLint("CheckResult")
    public void hasActiveInternetConnection(Context context, NetworkCheckerCallback callback) {
        Observable.fromCallable(() -> isInternetConnectionStable(context))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback::onNetworkCheckCompleted);
    }

    private boolean isInternetConnectionStable(Context context) {
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
            }
        } else {
            Log.d(TAG, "Network not available");
        }
        return false;
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
