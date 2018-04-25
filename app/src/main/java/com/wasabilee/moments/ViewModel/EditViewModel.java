package com.wasabilee.moments.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.theartofdev.edmodo.cropper.CropImage;
import com.wasabilee.moments.Activitiy.EditActivity;
import com.wasabilee.moments.Activitiy.ImageDetailActivity;
import com.wasabilee.moments.Data.ImageData;
import com.wasabilee.moments.Data.ImageUploadManager;
import com.wasabilee.moments.Data.Journal;
import com.wasabilee.moments.Data.JournalDataSource;
import com.wasabilee.moments.Data.JournalRepository;
import com.wasabilee.moments.Fragment.EditDayFragment;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.ActivityNavigator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class EditViewModel extends AndroidViewModel implements ImageUploadManager.ImageUploadCallback {

    private static final String TAG = EditViewModel.class.getSimpleName();

    private Context mContext;
    private JournalRepository mJournalRepository;

    private FirebaseAuth mAuth;

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

    private MutableLiveData<Integer> mSnackbarTextResource = new MutableLiveData<>();
    private MutableLiveData<String> mSnackbarText = new MutableLiveData<>();


    private Journal mJournal;
    private int mJournalId;
    private boolean mIsNewTask;

    private boolean isDayImageChanged = false;
    private boolean isNightImageChanged = false;


    public EditViewModel(Application context, JournalRepository journalRepository) {
        super(context);
        mContext = context.getApplicationContext();
        mJournalRepository = journalRepository;
    }

    public void start() {

        mAuth = FirebaseAuth.getInstance();
        mJournal = new Journal(mAuth.getUid());
        setDate(formatDate(getDate()));
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
        Glide.with(view.getContext())
                .load(imageUri)
                .apply(RequestOptions.centerCropTransform())
                .into(view);
    }

    @BindingAdapter({"mNightImageUri"})
    public static void loadNightImage(ImageView view, Uri imageUri) {
        Glide.with(view.getContext())
                .load(imageUri)
                .apply(RequestOptions.centerCropTransform())
                .into(view);
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data, String identifier) {
        // Loaded new image from CropImageActivity.class
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            switch (resultCode) {
                case RESULT_OK:
                    handleLoadedImage(result.getUri(), identifier);
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE:
                    result.getError().printStackTrace();
                    mSnackbarTextResource.setValue(R.string.unexpected_error);
                    break;
            }
        }

        // Handling the result of ImageDetailActivity.class.
        if (requestCode == EditActivity.IMAGE_DETAIL_REQUEST_CODE) {

            // Comparing the identifier from intent's result (Identifier that triggered the new activity)
            // and the one from the the fragment that triggered this method.
            // We need to compare the identifiers because onActivityResult() will be called by both fragments.

            String receivedIdentifier = data.getStringExtra(EditActivity.IDENTIFIER_RESULT_EXTRA_KEY);

            // Proceed the following code only the two identifiers are equal.
            if (receivedIdentifier != null && receivedIdentifier.equals(identifier)) {

                String uriStr = data.getStringExtra(EditActivity.IMAGE_URI_RESULT_EXTRA_KEY);

                switch (resultCode) {
                    case ImageDetailActivity.IMAGE_STATE_NEW_IMAGE:
                        handleLoadedImage(uriStr != null ? Uri.parse(uriStr) : null, identifier);
                        break;
                    case ImageDetailActivity.IMAGE_STATE_UNCHANGED:
                        /* empty */
                        break;
                    case ImageDetailActivity.IMAGE_STATE_DELETED:
                        handleLoadedImage(null, identifier);
                        break;
                }
            }
        }
    }

    private void handleLoadedImage(Uri uri, String identifier) {
        if (identifier.equals(EditDayFragment.JOURNAL_TIME_IDENTIFIER_DAY)) {
            isDayImageLoaded.set(uri != null);
            isDayImageChanged = true;
            mDayImageUri.set(uri);
        } else {
            isNightImageLoaded.set(uri != null);
            isNightImageChanged = true;
            mNightImageUri.set(uri);
        }
        mSnackbarTextResource.setValue(uri != null ? R.string.image_load_finished : R.string.image_deletion_finished);
    }

    public MutableLiveData<Integer> getSnackbarTextResource() {
        return mSnackbarTextResource;
    }

    public MutableLiveData<String> getSnackbarText() {
        return mSnackbarText;
    }

    public void saveJournal() {
        uploadImages(mDayImageUri.get(), mNightImageUri.get());
    }

    private void uploadImages(Uri dayImageUri, Uri nightImageUri) {

        if (dayImageUri == null && nightImageUri == null) {
            uploadJouranl();
        }

        List<ImageData> imageData = new ArrayList<>();

        if (isDayImageChanged && dayImageUri != null) {
            imageData.add(new ImageData(dayImageUri, true, false));
            imageData.add(new ImageData(dayImageUri, true, true));
        }

        if (isNightImageChanged && nightImageUri != null) {
            imageData.add(new ImageData(nightImageUri, false, false));
            imageData.add(new ImageData(nightImageUri, false, true));
        }

        ImageUploadManager.getInstance().uploadImage(mContext, imageData, this);

    }

    @Override
    public void onImageUploaded(ImageData result) {
        if (result.isDay()) {
            if (result.isThumb()) {
                mJournal.setDay_image_thumbnail_url(result.getDownloadUrl());
            } else {
                mJournal.setDay_image_url(result.getDownloadUrl());
            }
        } else {
            if (result.isThumb()) {
                mJournal.setNight_image_thumbnail_url(result.getDownloadUrl());
            } else {
                mJournal.setNight_image_url(result.getDownloadUrl());
            }
        }
    }

    @Override
    public void onError(String message) {
        Log.d(TAG, "onError: " + message);
        mSnackbarText.setValue(message);
    }

    @Override
    public void onImageUploadTasksCompleted() {
        uploadJouranl();
    }

    private void uploadJouranl() {

        completeJournalData();
        mJournalRepository.saveJournal(mJournal, new JournalDataSource.UploadJournalCallback() {
            @Override
            public void onJournalUploaded() {
                mSnackbarTextResource.setValue(R.string.journal_saved);
            }

            @Override
            public void onError(String message) {
                mSnackbarText.setValue(message);
            }
        });
    }

    private void completeJournalData() {
        mJournal.setTimestamp(Calendar.getInstance().getTime());
        mJournal.setUser_designated_timestamp(getDate());

        mJournal.setDay_journal_exists(isDayJouranlCreated());
        mJournal.setNight_journal_exists(isNightJouranlCreated());

        mJournal.setTopic_1(new ArrayList<>(Arrays.asList(mTopic_1_item_1.get(), mTopic_1_item_2.get(), mTopic_1_item_3.get())));
        mJournal.setTopic_2(new ArrayList<>(Arrays.asList(mTopic_2_item_1.get(), mTopic_2_item_2.get(), mTopic_2_item_3.get())));
        mJournal.setTopic_3(new ArrayList<>(Arrays.asList(mTopic_3_item_1.get(), mTopic_3_item_2.get(), mTopic_3_item_3.get())));

        mJournal.setTopic_4(new ArrayList<>(Arrays.asList(mTopic_4_item_1.get(), mTopic_4_item_2.get(), mTopic_4_item_3.get())));
        mJournal.setTopic_5(new ArrayList<>(Arrays.asList(mTopic_5_item_1.get(), mTopic_5_item_2.get(), mTopic_5_item_3.get())));
    }

    private boolean isDayJouranlCreated() {
        return mDayImageUri.get() != null ||
                mTopic_1_item_1.get() != null || mTopic_1_item_2.get() != null || mTopic_1_item_3.get() != null ||
                mTopic_2_item_1.get() != null || mTopic_2_item_2.get() != null || mTopic_2_item_3.get() != null ||
                mTopic_3_item_1.get() != null || mTopic_3_item_2.get() != null || mTopic_3_item_3.get() != null;
    }

    private boolean isNightJouranlCreated() {
        return mNightImageUri.get() != null ||
                mTopic_4_item_1.get() != null || mTopic_4_item_2.get() != null || mTopic_4_item_3.get() != null ||
                mTopic_5_item_1.get() != null || mTopic_5_item_2.get() != null || mTopic_5_item_3.get() != null;
    }
}
