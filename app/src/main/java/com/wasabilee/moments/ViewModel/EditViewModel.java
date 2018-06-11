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
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.theartofdev.edmodo.cropper.CropImage;
import com.wasabilee.moments.Activity.EditActivity;
import com.wasabilee.moments.Activity.ImageDetailActivity;
import com.wasabilee.moments.Data.ImageData;
import com.wasabilee.moments.Data.ImageUploadManager;
import com.wasabilee.moments.Data.Models.Journal;
import com.wasabilee.moments.Data.JournalDataSource;
import com.wasabilee.moments.Data.JournalRepository;
import com.wasabilee.moments.Fragment.EditDayFragment;
import com.wasabilee.moments.Fragment.EditNightFragment;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.Navigators.ActivityNavigator;
import com.wasabilee.moments.Utils.Navigators.JournalLoadTaskNavigator;
import com.wasabilee.moments.Utils.Navigators.JournalUploadTaskNavigator;
import com.wasabilee.moments.Utils.NetworkChecker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class EditViewModel extends AndroidViewModel implements ImageUploadManager.ImageUploadCallback, NetworkChecker.NetworkCheckerCallback {

    private static final String TAG = EditViewModel.class.getSimpleName();

    private Context mContext;
    private JournalRepository mJournalRepository;

    private FirebaseAuth mAuth;

    private final Calendar mCalendar = Calendar.getInstance();

    private MutableLiveData<JournalLoadTaskNavigator> mJournalLoadCompleted = new MutableLiveData<>();
    private MutableLiveData<JournalUploadTaskNavigator> mJournalUploadCompleted = new MutableLiveData<>();
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


    public Journal mJournal;
    private String mJournalId;
    private boolean mIsNewTask;

    private Uri initialDayImageUri = Uri.EMPTY;
    private Uri initialNightImageUri = Uri.EMPTY;

    private boolean isDayImageChanged = false;
    private boolean isNightImageChanged = false;


    public EditViewModel(Application context, JournalRepository journalRepository) {
        super(context);
        mContext = context.getApplicationContext();
        mJournalRepository = journalRepository;
    }

    public void start(String journalId) {

        mAuth = FirebaseAuth.getInstance();
        mJournal = new Journal(mAuth.getUid());

        onDateSet(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DATE));

        if (journalId != null) {
            loadJournal(journalId);
        }
    }

    public void loadJournal(String journalId) {
        mJournalLoadCompleted.setValue(JournalLoadTaskNavigator.LOAD_IN_PROGRESS);
        mJournalRepository.getJournal(journalId, new JournalDataSource.GetJournalCallback() {
            @Override
            public void onJournalLoaded(Journal journal) {
                mSnackbarTextResource.setValue(R.string.journal_load_completed);
                mJournalLoadCompleted.setValue(JournalLoadTaskNavigator.LOAD_SUCCESSFUL);

                mJournal = journal;

                // Parse the result
                parseLoadedJournal(journal);

            }

            @Override
            public void onDataNotAvailable() {
                mJournalLoadCompleted.setValue(JournalLoadTaskNavigator.LOAD_FAILED);
                mSnackbarTextResource.setValue(R.string.unexpected_error);
            }
        });

    }

    private void parseLoadedJournal(Journal journal) {

        mCalendar.setTime(journal.getUser_designated_timestamp());
        onDateSet(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DATE));

        String dayImageSource = journal.getDay_image_local_uri() == null ?
                journal.getDay_image_url() : journal.getDay_image_local_uri();
        String nightImageSource = journal.getNight_image_local_uri() == null ?
                journal.getNight_image_url() : journal.getNight_image_local_uri();

        parseLoadedImage(dayImageSource, EditDayFragment.JOURNAL_TIME_IDENTIFIER_DAY);
        parseLoadedImage(nightImageSource, EditNightFragment.JOURNAL_TIME_IDENTIFIER_NIGHT);

        mTopic_1_item_1.set(journal.getTopic_1_item_1());
        mTopic_1_item_2.set(journal.getTopic_1_item_2());
        mTopic_1_item_3.set(journal.getTopic_1_item_3());
        mTopic_2_item_1.set(journal.getTopic_2_item_1());
        mTopic_2_item_2.set(journal.getTopic_2_item_2());
        mTopic_2_item_3.set(journal.getTopic_2_item_3());
        mTopic_3_item_1.set(journal.getTopic_3_item_1());
        mTopic_3_item_2.set(journal.getTopic_3_item_2());
        mTopic_3_item_3.set(journal.getTopic_3_item_3());
        mTopic_4_item_1.set(journal.getTopic_4_item_1());
        mTopic_4_item_2.set(journal.getTopic_4_item_2());
        mTopic_4_item_3.set(journal.getTopic_4_item_3());
        mTopic_5_item_1.set(journal.getTopic_5_item_1());
        mTopic_5_item_2.set(journal.getTopic_5_item_2());
        mTopic_5_item_3.set(journal.getTopic_5_item_3());
    }


    private void parseLoadedImage(String url, String identifier) {

        Uri uri = url != null ? Uri.parse(url) : null;

        if (identifier.equals(EditDayFragment.JOURNAL_TIME_IDENTIFIER_DAY)) {
            initialDayImageUri = uri != null ? uri : Uri.EMPTY;
            isDayImageLoaded.set(uri != null);
            mDayImageUri.set(uri);
        } else {
            initialNightImageUri = uri != null ? uri : Uri.EMPTY;
            isNightImageLoaded.set(uri != null);
            mNightImageUri.set(uri);
        }
    }


    private void parseText(List<ObservableField<String>> fields, List<String> topic) {
        for (int i = 0; i < fields.size(); i++) {
            fields.get(i).set(topic.get(i));
        }
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

    public MutableLiveData<JournalLoadTaskNavigator> getJournalLoadCompleted() {
        return mJournalLoadCompleted;
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
                    handleLoadedImage(result.getUri() == null ? null : result.getUri(), identifier);
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
                Log.d(TAG, "handleActivityResult: " + uriStr);
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
            isDayImageChanged = isImageChanged(initialDayImageUri, uri);
            Log.d(TAG, "handleLoadedImage: " + isDayImageChanged);
            mDayImageUri.set(uri);
        } else {
            isNightImageLoaded.set(uri != null);
            isNightImageChanged = isImageChanged(initialNightImageUri, uri);
            mNightImageUri.set(uri);
            Log.d(TAG, "handleLoadedImage: " + isNightImageChanged);
        }
    }

    private boolean isImageChanged(Uri initialUri, Uri newUri) {
        if (initialUri == null) initialUri = Uri.EMPTY;
        if (newUri == null) newUri = Uri.EMPTY;

        return !initialUri.equals(newUri);

    }


    public MutableLiveData<Integer> getSnackbarTextResource() {
        return mSnackbarTextResource;
    }

    public MutableLiveData<String> getSnackbarText() {
        return mSnackbarText;
    }

    public MutableLiveData<JournalUploadTaskNavigator> getJournalUploadCompleted() {
        return mJournalUploadCompleted;
    }

    public void saveJournal() {
        // Show progress dialog
        mJournalUploadCompleted.setValue(JournalUploadTaskNavigator.UPLOAD_IN_PROGRESS);

        NetworkChecker.getInstance().hasActiveInternetConnection(mContext, this);
    }


    private void uploadImages(Uri dayImageUri, Uri nightImageUri) {

        if (!isDayImageChanged && !isNightImageChanged) {
            uploadJournal();
            return;
        }

        //TODO Save image in local storage

        List<ImageData> imageDataList = generateImageDataList(dayImageUri, nightImageUri);
        ImageUploadManager.getInstance().uploadImage(mContext, imageDataList, this);

    }


    private List<ImageData> generateImageDataList(Uri dayImageUri, Uri nightImageUri) {
        List<ImageData> imageDataList = new ArrayList<>();

        if (isDayImageChanged) {
            imageDataList.add(new ImageData(dayImageUri,
                    true, false, mJournal.getDay_image_file_name()));
            imageDataList.add(new ImageData(dayImageUri,
                    true, true, mJournal.getDay_image_thumbnail_file_name()));
        }

        if (isNightImageChanged) {
            imageDataList.add(new ImageData(nightImageUri,
                    false, false, mJournal.getNight_image_file_name()));
            imageDataList.add(new ImageData(nightImageUri,
                    false, true, mJournal.getNight_image_thumbnail_file_name()));
        }

        return imageDataList;

    }


    @Override
    public void onImageUploaded(ImageData result) {
        if (result.isDay()) {
            if (result.isThumb()) {
                //TODO Save uri here
                mJournal.setDay_image_thumbnail_url(result.getDownloadUrl());
                mJournal.setDay_image_thumbnail_file_name(result.getFileName());
            } else {
                mJournal.setDay_image_url(result.getDownloadUrl());
                mJournal.setDay_image_file_name(result.getFileName());
            }
        } else {
            if (result.isThumb()) {
                mJournal.setNight_image_thumbnail_url(result.getDownloadUrl());
                mJournal.setNight_image_thumbnail_file_name(result.getFileName());
            } else {
                mJournal.setNight_image_url(result.getDownloadUrl());
                mJournal.setNight_image_file_name(result.getFileName());
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
        Log.d(TAG, "onImageUploadTasksCompleted: upload journal");
        uploadJournal();
    }

    private void uploadJournal() {
        completeJournalData();
        mJournalRepository.saveJournal(mJournal, new JournalDataSource.UploadJournalCallback() {
            @Override
            public void onJournalUploaded(String journalId) {
                mJournalUploadCompleted.setValue(JournalUploadTaskNavigator.UPLOAD_SUCCESSFUL);
                mSnackbarTextResource.setValue(R.string.journal_update_completed);
            }

            @Override
            public void onError(String message) {
                mJournalUploadCompleted.setValue(JournalUploadTaskNavigator.UPLOAD_FAILED);
                mSnackbarTextResource.setValue(R.string.unexpected_error);
            }
        });
    }

    private void completeJournalData() {
        mJournal.setTimestamp(Calendar.getInstance().getTime());
        mJournal.setUser_designated_timestamp(getDate());

        mJournal.setDay_journal_exists(isDayJournalCreated());
        mJournal.setNight_journal_exists(isNightJournalCreated());

        mJournal.setTopic_1_item_1(mTopic_1_item_1.get());
        mJournal.setTopic_1_item_2(mTopic_1_item_2.get());
        mJournal.setTopic_1_item_3(mTopic_1_item_3.get());
        mJournal.setTopic_2_item_1(mTopic_2_item_1.get());
        mJournal.setTopic_2_item_2(mTopic_2_item_2.get());
        mJournal.setTopic_2_item_3(mTopic_2_item_3.get());
        mJournal.setTopic_3_item_1(mTopic_3_item_1.get());
        mJournal.setTopic_3_item_2(mTopic_3_item_2.get());
        mJournal.setTopic_3_item_3(mTopic_3_item_3.get());
        mJournal.setTopic_4_item_1(mTopic_4_item_1.get());
        mJournal.setTopic_4_item_2(mTopic_4_item_2.get());
        mJournal.setTopic_4_item_3(mTopic_4_item_3.get());
        mJournal.setTopic_5_item_1(mTopic_5_item_1.get());
        mJournal.setTopic_5_item_2(mTopic_5_item_2.get());
        mJournal.setTopic_5_item_3(mTopic_5_item_3.get());
    }

    public boolean isBeingComposed() {
        return isDayJournalCreated() || isNightJournalCreated();
    }

    private boolean isDayJournalCreated() {
        return mDayImageUri.get() != null ||
                !TextUtils.isEmpty(mTopic_1_item_1.get()) || !TextUtils.isEmpty(mTopic_1_item_2.get()) || !TextUtils.isEmpty(mTopic_1_item_3.get()) ||
                !TextUtils.isEmpty(mTopic_2_item_1.get()) || !TextUtils.isEmpty(mTopic_2_item_2.get()) || !TextUtils.isEmpty(mTopic_2_item_3.get()) ||
                !TextUtils.isEmpty(mTopic_3_item_1.get()) || !TextUtils.isEmpty(mTopic_3_item_2.get()) || !TextUtils.isEmpty(mTopic_3_item_3.get());
    }

    private boolean isNightJournalCreated() {
        return mNightImageUri.get() != null ||
                !TextUtils.isEmpty(mTopic_4_item_1.get()) || !TextUtils.isEmpty(mTopic_4_item_2.get()) || !TextUtils.isEmpty(mTopic_4_item_3.get()) ||
                !TextUtils.isEmpty(mTopic_5_item_1.get()) || !TextUtils.isEmpty(mTopic_5_item_2.get()) || !TextUtils.isEmpty(mTopic_5_item_3.get());
    }

    @Override
    public void onNetworkCheckCompleted(boolean isAvailable) {

        // Dismiss the progress dialog / Show snackbar text
        if (!isAvailable) {
            mJournalUploadCompleted.setValue(JournalUploadTaskNavigator.UPLOAD_UNSTABLE_CONNECTION);
            mSnackbarTextResource.setValue(R.string.internet_unstable_upload);

            //TODO Save this journal as draft.

            return;
        }
        // Stable internet connection. Proceed the upload.
        uploadImages(mDayImageUri.get(), mNightImageUri.get());
    }
}
