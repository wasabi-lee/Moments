package com.wasabilee.moments.Data;

import android.support.annotation.NonNull;

import com.wasabilee.moments.Data.Models.Journal;

import java.util.List;

public interface JournalDataSource {

    interface UploadJournalCallback {
        void onJournalUploaded(String journalId);
        void onError(String message);
    }

    interface LoadJournalsCallback {
        void onJournalsLoaded(List<Journal> journals);
        void onDataNotAvailable(String message);
    }

    interface GetJournalCallback {
        void onJournalLoaded(Journal journal);

        void onDataNotAvailable();
    }

    interface DeleteJournalCallback {
        void onJournalDeleted();

        void onError();
    }

    void getJournals(@NonNull String userId, @NonNull LoadJournalsCallback callback);

    void getJournal(@NonNull String journalId, @NonNull GetJournalCallback callback);

    void saveJournal(@NonNull Journal journal, @NonNull UploadJournalCallback callback);

    void refreshJournals();

    void deleteAllJournals(@NonNull String userId);

    void deleteJournal(@NonNull String journalId, @NonNull DeleteJournalCallback callback);
}
