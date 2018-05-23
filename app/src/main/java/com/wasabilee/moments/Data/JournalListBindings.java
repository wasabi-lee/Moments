package com.wasabilee.moments.Data;

import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.wasabilee.moments.Adapter.JournalAdapter;
import com.wasabilee.moments.Data.Models.DateData;
import com.wasabilee.moments.Data.Models.JournalData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class JournalListBindings {

    private static final String TAG = JournalListBindings.class.getSimpleName();

    @SuppressWarnings("unchecked")
    @BindingAdapter({"app:items"})
    public static void setItems(RecyclerView recyclerView, List<JournalData> items) {
        Log.d(TAG, "setItems: " + (items == null ? "null" : items.size()));
        JournalAdapter adapter = (JournalAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.replaceData(items);
        }
    }

    @BindingAdapter({"app:imageSource"})
    public static void loadImage(ImageView imageView, String imageUrl) {
        if (imageUrl != null) {
            try {
                Glide.with(imageView.getContext())
                        .applyDefaultRequestOptions(RequestOptions.centerCropTransform())
                        .load(imageUrl)
                        .into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @BindingAdapter({"app:dateText"})
    public static void setDate(TextView textView, Date date) {
        if (date != null) {
            String formattedDate = DateData.createFormattedDate(DateData.DAILY_FORMAT, date);
            textView.setText(formattedDate);
        }
    }

    @BindingAdapter({"app:timeText"})
    public static void setTime(TextView textView, Date date) {
        if (date != null) {
            String formattedDate = "Created at " + DateData.createFormattedDate(DateData.TIME_ONLY_FORMAT, date);
            textView.setText(formattedDate);
        }
    }


}
