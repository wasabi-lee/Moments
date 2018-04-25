package com.wasabilee.moments.Data;

import android.support.annotation.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class JournalRepository implements JournalDataSource {

    private volatile static JournalRepository INSTANCE = null;
    private JournalDataSource mJournalRemoteDataSource;
    private Map<String, Journal> mCachedJournals;
    private boolean mCacheIsDirty = false;

    private JournalRepository(@NonNull JournalRemoteDataSource journalRemoteDataSource) {
        this.mJournalRemoteDataSource = journalRemoteDataSource;
    }

    public static JournalRepository getInstance(JournalRemoteDataSource journalRemoteDataSource) {
        if (INSTANCE == null) {
            synchronized (JournalRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new JournalRepository(journalRemoteDataSource);
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public void getJournals(@NonNull String userId, @NonNull LoadJournalsCallback callback) {

    }

    @Override
    public void getJournal(@NonNull String journalId, @NonNull GetJournalCallback callback) {

    }

    @Override
    public void saveJournal(@NonNull Journal journal, @NonNull final UploadJournalCallback callback) {
        mJournalRemoteDataSource.saveJournal(journal, new UploadJournalCallback() {
            @Override
            public void onJournalUploaded() {
                callback.onJournalUploaded();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });

        if (mCachedJournals == null) {
            mCachedJournals = new LinkedHashMap<>();
        }
        mCachedJournals.put(journal.getUser_id(), journal);
    }

    @Override
    public void refreshJournals() {

    }

    @Override
    public void deleteAllJournals(@NonNull String userId) {

    }

    @Override
    public void deleteJournal(@NonNull String journalId) {

    }
}
