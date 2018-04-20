package com.wasabilee.moments.ViewModel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.wasabilee.moments.Utils.ActivityNavigator;

public class MainViewModel extends ViewModel {

    private MutableLiveData<ActivityNavigator> mActivityChangeLiveData = new MutableLiveData<>();

    public MainViewModel() {
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
