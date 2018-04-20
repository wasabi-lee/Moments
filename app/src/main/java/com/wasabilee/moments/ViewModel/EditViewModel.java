package com.wasabilee.moments.ViewModel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.wasabilee.moments.Fragment.EditDayFragment;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.ActivityNavigator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class EditViewModel extends ViewModel {

    private static final String TAG = EditViewModel.class.getSimpleName();
    private final Calendar mCalendar = Calendar.getInstance();

    private MutableLiveData<ActivityNavigator> mDayActivityChangeEvent = new MutableLiveData<>();
    private MutableLiveData<ActivityNavigator> mNightActivityChangeEvent = new MutableLiveData<>();

    private MutableLiveData<Uri> mDayImageDetailOpeningEvent = new MutableLiveData<>();
    private MutableLiveData<Uri> mNightImageDetailOpeningEvent = new MutableLiveData<>();

    public ObservableBoolean isDayImageLoaded = new ObservableBoolean(false);
    public ObservableBoolean isNightImageLoaded = new ObservableBoolean(false);

    public ObservableField<String> mDate = new ObservableField<>();
    public ObservableField<Uri> mDayImageUri = new ObservableField<>();
    public ObservableField<Uri> mNightImageUri = new ObservableField<>();

    public ObservableField<String> mTopic_1_item_1 = new ObservableField<>("");
    public ObservableField<String> mTopic_1_item_2 = new ObservableField<>("");
    public ObservableField<String> mTopic_1_item_3 = new ObservableField<>("");

    public ObservableField<String> mTopic_2_item_1 = new ObservableField<>("");
    public ObservableField<String> mTopic_2_item_2 = new ObservableField<>("");
    public ObservableField<String> mTopic_2_item_3 = new ObservableField<>("");

    public ObservableField<String> mTopic_3_item_1 = new ObservableField<>("");
    public ObservableField<String> mTopic_3_item_2 = new ObservableField<>("");
    public ObservableField<String> mTopic_3_item_3 = new ObservableField<>("");

    public ObservableField<String> mTopic_4_item_1 = new ObservableField<>("");
    public ObservableField<String> mTopic_4_item_2 = new ObservableField<>("");
    public ObservableField<String> mTopic_4_item_3 = new ObservableField<>("");

    public ObservableField<String> mTopic_5_item_1 = new ObservableField<>("");
    public ObservableField<String> mTopic_5_item_2 = new ObservableField<>("");
    public ObservableField<String> mTopic_5_item_3 = new ObservableField<>("");

    private MutableLiveData<Integer> mSnackbarText = new MutableLiveData<>();


    private int mJournalId;
    private boolean mIsNewTask;


    public EditViewModel() {
    }

    public void start() {

        // Passing ApplicationContext !

        setDate(formatDate(mCalendar.getTime()));
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("MMM d, yyyy").format(date);
    }

    public void onDateSet(int year, int month, int dayOfMonth) {
        mCalendar.set(year, month, dayOfMonth);
        Date selectedDate = mCalendar.getTime();
        setDate(formatDate(selectedDate));
    }

    public void setDate(String date) {
        mDate.set(date);
    }

    public Date getDate() {
        return mCalendar.getTime();
    }

    public ObservableBoolean getIsDayImageLoaded() {
        return isDayImageLoaded;
    }

    public ObservableField<Uri> getDayImageUri() {
        return mDayImageUri;
    }

    public ObservableBoolean getIsNightImageLoaded() {
        return isNightImageLoaded;
    }

    public ObservableField<Uri> getNightImageUri() {
        return mNightImageUri;
    }

    public MutableLiveData<ActivityNavigator> getDayActivityChangeLiveData() {
        return mDayActivityChangeEvent;
    }

    public MutableLiveData<ActivityNavigator> getNightActivityChangeLiveData() {
        return mNightActivityChangeEvent;
    }

    public MutableLiveData<Uri> getDayImageDetailOpeningEvent() {
        return mDayImageDetailOpeningEvent;
    }

    public MutableLiveData<Uri> getNightImageDetailOpeningEvent() {
        return mNightImageDetailOpeningEvent;
    }

    public void toCropImageActivity(String identifier) {
        if (identifier.equals(EditDayFragment.JOURNAL_TIME_IDENTIFIER_DAY)) {
            mDayActivityChangeEvent.setValue(ActivityNavigator.NEW_IMAGE);
        } else {
            mNightActivityChangeEvent.setValue(ActivityNavigator.NEW_IMAGE);
        }
    }

    public void toImageDetailActivity(String identifier) {
        if (identifier.equals(EditDayFragment.JOURNAL_TIME_IDENTIFIER_DAY)) {
            mDayImageDetailOpeningEvent.setValue(mDayImageUri.get());
        } else {
            mNightImageDetailOpeningEvent.setValue(mNightImageUri.get());
        }
    }

    @BindingAdapter({"mDayImageUri"})
    public static void loadDayImage(ImageView view, Uri imageUri) {
        Log.d(TAG, "loadDayImage: ");
        Glide.with(view.getContext())
                .load(imageUri)
                .apply(RequestOptions.centerCropTransform())
                .into(view);
   }
    @BindingAdapter({"mNightImageUri"})
    public static void loadNightImage(ImageView view, Uri imageUri) {
        Log.d(TAG, "loadNightImage: ");
        Glide.with(view.getContext())
                .load(imageUri)
                .apply(RequestOptions.centerCropTransform())
                .into(view);
   }

   public void handleActivityResult(int requestCode, int resultCode, Intent data, String identifier) {
       if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
           CropImage.ActivityResult result = CropImage.getActivityResult(data);
           switch (resultCode) {
               case RESULT_OK:
                   handleLoadedImage(result.getUri(), identifier);
                   break;
               case CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE:
                   result.getError().printStackTrace();
                   mSnackbarText.setValue(R.string.unexpected_error);
                   break;
           }
       }
   }

   private void handleLoadedImage(Uri uri, String identifier) {
        if (identifier.equals(EditDayFragment.JOURNAL_TIME_IDENTIFIER_DAY)) {
            isDayImageLoaded.set(true);
            mDayImageUri.set(uri);
        } else {
            isNightImageLoaded.set(true);
            mNightImageUri.set(uri);
        }
       mSnackbarText.setValue(R.string.image_load_finished);
   }

    public MutableLiveData<Integer> getSnackbarText() {
        return mSnackbarText;
    }

    public void saveJournal() {
        String _1 = mTopic_1_item_1.get();
        String _2 = mTopic_1_item_2.get();
        String _3 = mTopic_1_item_3.get();
        String _4 = mTopic_2_item_1.get();
        String _5 = mTopic_2_item_2.get();
        String _6 = mTopic_2_item_3.get();
        String _7 = mTopic_3_item_1.get();
        String _8 = mTopic_3_item_2.get();
        String _9 = mTopic_3_item_3.get();
        String _10 = mTopic_4_item_1.get();
        String _11 = mTopic_4_item_2.get();
        String _12 = mTopic_4_item_3.get();
        String _13 = mTopic_5_item_1.get();
        String _14 = mTopic_5_item_2.get();
        String _15 = mTopic_5_item_3.get();

        Log.d(TAG, "saveJournal: \n" +
        _1 + "\n" +
        _2 + "\n" +
        _3 + "\n" +
        _4 + "\n" +
        _5 + "\n" +
        _6 + "\n" +
        _7 + "\n" +
        _8 + "\n" +
        _9 + "\n" +
        _10 + "\n" +
        _11 + "\n" +
        _12 + "\n" +
        _13 + "\n" +
        _14 + "\n" +
        _15 + "\n"
        );

    }



}
