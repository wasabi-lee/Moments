package com.wasabilee.moments.data;

import android.support.annotation.NonNull;

import com.wasabilee.moments.data.models.Journal;

import java.util.List;

public interface JournalDataSource {

    interface JournalSaveCallback {
        void onJournalSaved(String journalId);

        void onError(String message);
    }

    interface JournalLocalSaveCallback {
        void onJournalSavedLocal(String journalId);

        void onError();
    }

    interface JournalDraftSavedCallback {
        void onJournalDraftSaved();

        void onError();
    }

    interface JournalDraftDeletedCallback {
        void onJournalDraftDeleted();

        void onError();
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

    void saveJournal(@NonNull Journal journal, @NonNull JournalSaveCallback callback);

    void refreshJournals();

    void deleteAllJournals(@NonNull String userId);

    void deleteJournal(@NonNull String journalId, @NonNull DeleteJournalCallback callback);



}

//            try {
//                    draftDao.insert(draft);
//                    callback.onJournalDraftSaved();
//                    } catch (Exception e) {
//                    callback.onError();
//                    }

//        try {
//                draftDao.delete(draft);
//                callback.onJournalDraftDeleted();
//                } catch (Exception e) {
//                callback.onError();
//                }