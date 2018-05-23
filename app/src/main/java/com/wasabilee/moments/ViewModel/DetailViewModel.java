package com.wasabilee.moments.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.util.Log;

import com.wasabilee.moments.Activity.DetailActivity;
import com.wasabilee.moments.Data.ImageData;
import com.wasabilee.moments.Data.ImageUploadManager;
import com.wasabilee.moments.Data.JournalDataSource;
import com.wasabilee.moments.Data.JournalRepository;
import com.wasabilee.moments.Data.Models.Journal;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.JournalImageOpenListener;
import com.wasabilee.moments.Utils.Navigators.JournalDeletionTaskNavigator;
import com.wasabilee.moments.Utils.Navigators.JournalLoadTaskNavigator;
import com.wasabilee.moments.Utils.Navigators.JournalStateNavigator;

import java.util.ArrayList;
import java.util.List;

public class DetailViewModel extends AndroidViewModel implements JournalDataSource.GetJournalCallback, ImageUploadManager.ImageDeletionCallback, JournalDataSource.DeleteJournalCallback {

    private static final String TAG = DetailActivity.class.getSimpleName();

    private String mJournalId;

    private MutableLiveData<JournalLoadTaskNavigator> mJournalLoadTaskNavigator = new MutableLiveData<>();
    private MutableLiveData<JournalDeletionTaskNavigator> mJournalDeletionTaskNavigator = new MutableLiveData<>();

    private final JournalRepository mRepository;

    private MutableLiveData<String> mSnackbarText = new MutableLiveData<>();
    private MutableLiveData<Integer> mSnackbarTextResource = new MutableLiveData<>();
    public ObservableField<Journal> mJournal = new ObservableField<>();

    private MutableLiveData<Void> mToEditActivityEvent = new MutableLiveData<>();

    private boolean mIsJournalEdited = false;

    private MutableLiveData<String> mJournalImageOpenEvent = new MutableLiveData<>();
    public JournalImageOpenListener mImageOpenListener = url -> mJournalImageOpenEvent.setValue(url);

    public DetailViewModel(Application context, JournalRepository journalRepository) {
        super(context);
        mRepository = journalRepository;
    }

    public MutableLiveData<Void> getToEditActivityEvent() {
        return mToEditActivityEvent;
    }

    public void deleteJournal() {
        mJournalDeletionTaskNavigator.setValue(JournalDeletionTaskNavigator.DELETION_IN_PROGRESS);
        if (mJournal != null && mJournal.get() != null) {
            List<ImageData> imagesToDelete = getImageData(mJournal.get());
            ImageUploadManager.getInstance().deleteFromStorage(imagesToDelete, this);
        } else {
            mRepository.deleteJournal(mJournalId, this);
        }
    }

    public MutableLiveData<String> getmJournalImageOpenEvent() {
        return mJournalImageOpenEvent;
    }

    private List<ImageData> getImageData(@NonNull Journal journal) {

        List<ImageData> imageDataList = new ArrayList<>();

        if (journal.isDay_journal_exists()) {
            if (journal.getDay_image_file_name() != null)
                imageDataList.add(new ImageData(journal.getDay_image_file_name(), false));
            if (journal.getDay_image_thumbnail_file_name() != null)
                imageDataList.add(new ImageData(journal.getDay_image_thumbnail_file_name(), true));
        }

        if (journal.isNight_journal_exists()) {
            if (journal.getNight_image_file_name() != null)
                imageDataList.add(new ImageData(journal.getNight_image_file_name(), false));
            if (journal.getNight_image_thumbnail_file_name() != null)
                imageDataList.add(new ImageData(journal.getNight_image_thumbnail_file_name(), true));
        }

        return imageDataList;
    }

    public boolean getIsJournalEdited() {
        return mIsJournalEdited;
    }

    public void loadJournal(String journalId) {
        this.mJournalId = journalId;
        if (journalId != null) {
            mJournalLoadTaskNavigator.setValue(JournalLoadTaskNavigator.LOAD_IN_PROGRESS);
            mRepository.getJournal(journalId, this);
        }
    }

    public boolean isDataAvailable() {
        return mJournal != null;
    }

    @Override
    public void onJournalLoaded(Journal journal) {
        mJournalLoadTaskNavigator.setValue(JournalLoadTaskNavigator.LOAD_SUCCESSFUL);
        mJournal.set(journal);
    }

    @Override
    public void onDataNotAvailable() {
        mJournalLoadTaskNavigator.setValue(JournalLoadTaskNavigator.LOAD_FAILED);
        mSnackbarTextResource.setValue(R.string.error_journal_load);
        mJournal.set(null);
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DetailActivity.TO_EDIT_ACTIVITY_REQUEST_CODE) {
            switch (resultCode) {
                case JournalStateNavigator.JOURNAL_STATE_EDITED:
                    mSnackbarTextResource.setValue(R.string.journal_update_completed);
                    mIsJournalEdited = true;
                    mRepository.refreshJournals();
                    break;
                case JournalStateNavigator.JOURNAL_STATE_UNCHANGED:
                    /* empty */
                    break;
            }
        }
    }

    public MutableLiveData<JournalLoadTaskNavigator> getJournalLoadTaskNavigator() {
        return mJournalLoadTaskNavigator;
    }

    public MutableLiveData<JournalDeletionTaskNavigator> getJournalDeletionTaskNavigator() {
        return mJournalDeletionTaskNavigator;
    }

    @Override
    public void onImageDeleted(ImageData result) {
        Log.d(TAG, "onImageDeleted: " + result.getUploadedFileName());
    }

    @Override
    public void onError(String message) {
        mSnackbarTextResource.setValue(R.string.unexpected_error);
    }

    @Override
    public void onImageDeletionTaskCompleted() {
        mRepository.deleteJournal(mJournalId, this);
    }

    @Override
    public void onJournalDeleted() {
        mJournalDeletionTaskNavigator.setValue(JournalDeletionTaskNavigator.DELETION_SUCCESSFUL);
        mSnackbarTextResource.setValue(R.string.journal_deletion_completed);
    }

    @Override
    public void onError() {
        mJournalDeletionTaskNavigator.setValue(JournalDeletionTaskNavigator.DELETION_FAILED);
        mSnackbarTextResource.setValue(R.string.unexpected_error);
    }

    public void toEditActivity() {
        mToEditActivityEvent.setValue(null);
    }
}
