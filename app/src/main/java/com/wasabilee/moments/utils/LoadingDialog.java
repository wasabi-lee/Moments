package com.wasabilee.moments.utils;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.wasabilee.moments.R;

public class LoadingDialog extends Dialog {


    public LoadingDialog(@NonNull Context context) {
        super(context);
        configDialog();
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected LoadingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    private void configDialog() {
        setContentView(R.layout.dialog_uploading);
        setCancelable(false);
    }

    public void setMessage(String message) {
        TextView messageTextView = this.findViewById(R.id.uploading_dialog_text);
        messageTextView.setText(message);
    }
}
