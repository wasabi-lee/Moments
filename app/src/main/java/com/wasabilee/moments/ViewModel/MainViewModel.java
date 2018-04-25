package com.wasabilee.moments.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import com.wasabilee.moments.Data.JournalRepository;
import com.wasabilee.moments.Utils.ActivityNavigator;

public class MainViewModel extends AndroidViewModel {

    private Context mContext;
    private JournalRepository mJournalRepository;

    private MutableLiveData<ActivityNavigator> mActivityChangeLiveData = new MutableLiveData<>();

    public MainViewModel(Application context, JournalRepository journalRepository) {
        super(context);
        mContext = context.getApplicationContext();
        mJournalRepository = journalRepository;
    }

    public MutableLiveData<ActivityNavigator> getActivityChangeLiveData() {
        return mActivityChangeLiveData;
    }

    public void addNewJournal() {
        mActivityChangeLiveData.setValue(ActivityNavigator.NEW_JOURNAL);
    }

    public void toSettings() {
        mActivityChangeLiveData.setValue(ActivityNavigator.SETTINGS);
    }

    public void toLogout() {
        mActivityChangeLiveData.setValue(ActivityNavigator.LOGOUT);
    }
}
