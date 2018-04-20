package com.wasabilee.moments.Utils;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SnackbarUtils {

    private static final String TAG = SnackbarUtils.class.getSimpleName();

    public static void showSnackbar(View v, String snackbarText) {
        if (v == null || snackbarText == null) {
            Log.d(TAG, "showSnackbar: " + snackbarText);
            Log.d(TAG, "showSnackbar: view or snackbar text is null");
            return;
        }
        Snackbar snackbar = Snackbar.make(v, snackbarText, Snackbar.LENGTH_LONG);
        int snackBarTextId = android.support.design.R.id.snackbar_text;
        TextView textView = snackbar.getView().findViewById(snackBarTextId);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
        Log.d(TAG, "showSnackbar: snackbar successfully shown" );
    }

    public static void showSnackbar(String s) {
        Log.d(TAG, "showSnackbar: " + s);
    }
}
