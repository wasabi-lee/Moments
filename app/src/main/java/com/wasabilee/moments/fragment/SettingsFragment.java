package com.wasabilee.moments.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.wasabilee.moments.preference.TimePreference;
import com.wasabilee.moments.preference.TimePreferenceDialogFragmentCompat;
import com.wasabilee.moments.R;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.app_settings);
        setupNotificationSettingStatus(getPreferenceManager().getSharedPreferences());
    }


    private void setupNotificationSettingStatus(SharedPreferences sharedPreferences) {
        String key = getString(R.string.settings_item_key_enable_notifications);
        if (sharedPreferences.getBoolean(key, true)) {
            toggleNotificationPreferences(true);
        } else {
            toggleNotificationPreferences(false);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.settings_item_key_enable_notifications))) {
            setupNotificationSettingStatus(sharedPreferences);
        }
    }


    private void toggleNotificationPreferences(boolean enabled) {
        togglePreference(getString(R.string.settings_item_key_enable_morning_notification), enabled);
        togglePreference(getString(R.string.settings_item_key_enable_night_notification), enabled);
        // If the notification alarm is set to ON again,
        // check the individual morning/night notification is
        // set ON or OFF again. Enable / Disable the alarm respectively.
        // If the notification alarm is set to OFF,
        // Cancel all existing alarms.

    }

    private void togglePreference(String key, boolean enabled) {
        getPreferenceScreen().findPreference(key).setEnabled(enabled);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {

        DialogFragment dialogFragment = null;
        if (preference instanceof TimePreference) {
            dialogFragment = TimePreferenceDialogFragmentCompat.newInstance(preference.getKey());
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
