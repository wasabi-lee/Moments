package com.wasabilee.moments.data;

import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.wasabilee.moments.adapter.JournalAdapter;
import com.wasabilee.moments.data.models.DateData;
import com.wasabilee.moments.data.models.Journal;
import com.wasabilee.moments.data.models.JournalData;

import java.util.Date;
import java.util.List;

import static com.wasabilee.moments.data.models.Journal.IMAGE_IDENTIFIER_DAY;
import static com.wasabilee.moments.data.models.Journal.IMAGE_IDENTIFIER_NIGHT;

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

    @BindingAdapter({"app:thumbnailSource"})
    public static void loadThumbnail(ImageView imageView, String thumbnailSource) {
        Glide.with(imageView.getContext())
                .applyDefaultRequestOptions(RequestOptions.centerCropTransform())
                .load(thumbnailSource)
                .into(imageView);
    }

    @BindingAdapter({"app:imageSource", "app:imageType"})
    public static void loadImage(ImageView imageView, Journal journal, String type) {
        String imageUri = null, imageUrl = null;

        switch (type) {
            case IMAGE_IDENTIFIER_DAY:
                imageUri = journal.getDay_image_local_uri();
                imageUrl = journal.getDay_image_url();
                break;
            case IMAGE_IDENTIFIER_NIGHT:
                imageUri = journal.getNight_image_local_uri();
                imageUrl = journal.getNight_image_url();
                break;
        }

        // Visibility Setting
        if (imageUri == null && imageUrl == null) {
            imageView.setVisibility(View.GONE);
            return;
        }

        // Setting image
        try {
            Glide.with(imageView.getContext())
                    .applyDefaultRequestOptions(RequestOptions.centerCropTransform())
                    .load(imageUri != null ? imageUri : imageUrl)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
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
