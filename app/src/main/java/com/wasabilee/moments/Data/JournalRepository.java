package com.wasabilee.moments.Data;

import android.support.annotation.NonNull;

import com.wasabilee.moments.Data.Models.Journal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        if (mCachedJournals != null && !mCacheIsDirty) {
            callback.onJournalsLoaded(new ArrayList<>(mCachedJournals.values()));
            return;
        }

        getJournalsFromServer(userId, callback);

    }

    private void getJournalsFromServer(String userId, @NonNull LoadJournalsCallback callback) {
        mJournalRemoteDataSource.getJournals(userId, new LoadJournalsCallback() {
            @Override
            public void onJournalsLoaded(List<Journal> journals) {
                refreshCache(journals);
                callback.onJournalsLoaded(journals);
            }

            @Override
            public void onDataNotAvailable(String message) {
                callback.onDataNotAvailable(message);
            }
        });
    }

    private void refreshCache(List<Journal> journals) {
        if (mCachedJournals == null) {
            mCachedJournals = new LinkedHashMap<>();
        }

        mCachedJournals.clear();
        for (Journal journal : journals) {
            mCachedJournals.put(journal.getJournalId(), journal);
        }
        mCacheIsDirty = false;
    }

    private Journal getCachedJournalWithId(@NonNull String journalId) {
        if (mCachedJournals == null || mCachedJournals.isEmpty()) {
            return null;
        } else {
            return mCachedJournals.get(journalId);
        }
    }


    @Override
    public void getJournal(@NonNull String journalId, @NonNull GetJournalCallback callback) {

        if (!mCacheIsDirty) {
            Journal cachedJournal = getCachedJournalWithId(journalId);
            if (cachedJournal != null) {
                callback.onJournalLoaded(cachedJournal);
                return;
            }
        }

        mJournalRemoteDataSource.getJournal(journalId, new GetJournalCallback() {
            @Override
            public void onJournalLoaded(Journal journal) {
                if (mCachedJournals == null) {
                    mCachedJournals = new LinkedHashMap<>();
                }
                mCachedJournals.put(journal.getJournalId(), journal);
                callback.onJournalLoaded(journal);
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();

            }
        });
    }

    @Override
    public void saveJournal(@NonNull Journal journal, @NonNull final UploadJournalCallback callback) {
        mJournalRemoteDataSource.saveJournal(journal, new UploadJournalCallback() {
            @Override
            public void onJournalUploaded(String journalId) {
                callback.onJournalUploaded(journalId);
                if (mCachedJournals == null) {
                    mCachedJournals = new LinkedHashMap<>();
                }
                mCachedJournals.put(journal.getJournalId(), journal);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });

    }


    @Override
    public void refreshJournals() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllJournals(@NonNull String userId) {

    }

    @Override
    public void deleteJournal(@NonNull String journalId, @NonNull DeleteJournalCallback callback) {
        mJournalRemoteDataSource.deleteJournal(journalId, new DeleteJournalCallback() {
            @Override
            public void onJournalDeleted() {
                callback.onJournalDeleted();
                mCachedJournals.remove(journalId);
            }

            @Override
            public void onError() {
                callback.onError();
            }
        });

    }
}
