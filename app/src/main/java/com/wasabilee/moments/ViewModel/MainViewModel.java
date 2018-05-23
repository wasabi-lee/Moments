package com.wasabilee.moments.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableList;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.wasabilee.moments.Activity.MainActivity;
import com.wasabilee.moments.Data.Models.DateData;
import com.wasabilee.moments.Data.Models.Journal;
import com.wasabilee.moments.Data.JournalDataSource;
import com.wasabilee.moments.Data.JournalRepository;
import com.wasabilee.moments.Data.Models.JournalData;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.Navigators.ActivityNavigator;
import com.wasabilee.moments.Utils.Navigators.JournalStateNavigator;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;


public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    private Context mContext;
    private JournalRepository mJournalRepository;
    private String mUid;

    private FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();

    private MutableLiveData<Boolean> mIsDataLoading = new MutableLiveData<>();

    private MutableLiveData<ActivityNavigator> mActivityNavigator = new MutableLiveData<>();
    private MutableLiveData<String> mOpenJournalDetail = new MutableLiveData<>();

    private MutableLiveData<String> mSnackbarText = new MutableLiveData<>();
    private MutableLiveData<Integer> mSnackbarTextResource = new MutableLiveData<>();

    public ObservableList<JournalData> journalDataList = new ObservableArrayList<>();

    public ObservableBoolean noJournals = new ObservableBoolean(false);

    public void setmUid(String mUid) {
        this.mUid = mUid;
    }

    public void start() {
        loadJournals(false);
    }

    public MainViewModel(Application context, JournalRepository journalRepository) {
        super(context);
        mContext = context.getApplicationContext();
        mJournalRepository = journalRepository;
    }

    public MutableLiveData<Boolean> getmIsDataLoading() {
        return mIsDataLoading;
    }

    public MutableLiveData<ActivityNavigator> getActivityNavigator() {
        return mActivityNavigator;
    }

    public MutableLiveData<String> getmOpenJournalDetail() {
        return mOpenJournalDetail;
    }

    public MutableLiveData<String> getSnackbarText() {
        return mSnackbarText;
    }

    public MutableLiveData<Integer> getmSnackbarTextResource() {
        return mSnackbarTextResource;
    }

    public void checkIfUsernameSet() {
        mFirebaseFirestore.collection("Users")
                .document(mUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            mActivityNavigator.setValue(ActivityNavigator.ACCOUNT_SETTING);
                        }
                    } else {
                        mSnackbarText.setValue(task.getException().getMessage());
                    }
                });
    }

    public void loadJournals(boolean forceUpdate) {
        if (forceUpdate) {
            mJournalRepository.refreshJournals();
        }
        mIsDataLoading.setValue(true);
        mJournalRepository.getJournals(mUid, new JournalDataSource.LoadJournalsCallback() {
            @Override
            public void onJournalsLoaded(List<Journal> journals) {
                journalDataList.clear();
                journalDataList.addAll(createJournalListWithDateHeader(journals));
                noJournals.set(journalDataList.size() == 0);
                mIsDataLoading.setValue(false);
            }

            @Override
            public void onDataNotAvailable(String message) {
                mIsDataLoading.setValue(false);
                mSnackbarText.setValue(message);
                Log.d(TAG, "onDataNotAvailable: " + message);
            }
        });
    }

    private ObservableList<JournalData> createJournalListWithDateHeader(List<Journal> journals) {
        return getConsolidatedList(groupDataInHashMap(journals));
    }

    private LinkedHashMap<String, List<Journal>> groupDataInHashMap(List<Journal> journals) {
        LinkedHashMap<String, List<Journal>> groupedJournals = new LinkedHashMap<>();

        for (Journal journal : journals) {
            String hashMapKey = DateData.createFormattedDate(DateData.MONTHLY_FORMAT, journal.getTimestamp());

            if (groupedJournals.containsKey(hashMapKey)) {
                groupedJournals.get(hashMapKey).add(journal);
            } else {
                List<Journal> journalsForThisTimestamp = new ArrayList<>();
                journalsForThisTimestamp.add(journal);
                groupedJournals.put(hashMapKey, journalsForThisTimestamp);
            }
        }

        return groupedJournals;
    }

    private ObservableList<JournalData> getConsolidatedList(LinkedHashMap<String, List<Journal>> journalHashMap) {

        ObservableList<JournalData> consolidateJournals = new ObservableArrayList<>();

        for (String formattedDate : journalHashMap.keySet()) {
            Date timestamp = journalHashMap.get(formattedDate).get(0).getTimestamp();
            consolidateJournals.add(new DateData(timestamp));
            consolidateJournals.addAll(journalHashMap.get(formattedDate));
        }

        return consolidateJournals;

    }

    public void addNewJournal() {
        mActivityNavigator.setValue(ActivityNavigator.NEW_JOURNAL);
    }

    public void toSettings() {
        mActivityNavigator.setValue(ActivityNavigator.SETTINGS);
    }

    public void toLogout() {
        mActivityNavigator.setValue(ActivityNavigator.LOGOUT);
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.EDIT_ACTIVITY_REQUEST_CODE) {
            switch (resultCode) {
                case JournalStateNavigator.JOURNAL_STATE_ADDED_NEW:
                    mSnackbarTextResource.setValue(R.string.journal_saved);
                    loadJournals(true);
                    break;
                case JournalStateNavigator.JOURNAL_STATE_UNCHANGED:
                    /* empty */
                    break;
            }
        } else if (requestCode == MainActivity.DETAIL_ACTIVITY_REQUEST_CODE) {
            switch (resultCode) {
                case JournalStateNavigator.JOURNAL_STATE_EDITED:
                    loadJournals(true);
                    break;
                case JournalStateNavigator.JOURNAL_STATE_DELETED:
                    loadJournals(true);
                    mSnackbarTextResource.setValue(R.string.journal_deletion_completed);
                    break;
                case JournalStateNavigator.JOURNAL_STATE_UNCHANGED:
                    /* empty */
                    break;
            }
        }
    }
}
