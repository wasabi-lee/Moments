package com.wasabilee.moments.data;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.wasabilee.moments.data.db.AppDatabase;
import com.wasabilee.moments.data.db.JournalDao;
import com.wasabilee.moments.data.models.Journal;

public class JournalLocalDataSource implements JournalDataSource {

    /**
     * Controller for database interaction.
     * This class saves journals and drafts locally through Room database.
     */

    private static final String TAG = JournalLocalDataSource.class.getSimpleName();

    private static JournalDao journalDao;

    private static JournalLocalDataSource INSTANCE;

    private JournalLocalDataSource(Context context) {
        AppDatabase db = AppDatabase.getAppDatabase(context);
        journalDao = db.journalDao();
    }

    public static JournalLocalDataSource getInstance(Application context) {
        if (INSTANCE == null) {
            INSTANCE = new JournalLocalDataSource(context.getApplicationContext());
        }
        return INSTANCE;
    }

    @Override
    public void getJournals(@NonNull String userId, @NonNull LoadJournalsCallback callback) {
        try {
            callback.onJournalsLoaded(journalDao.getAllJournal());
        } catch (Exception e) {
            e.printStackTrace();
            callback.onDataNotAvailable("Error occurred while loading journals from DB");
        }
    }

    @Override
    public void getJournal(@NonNull String journalId, @NonNull GetJournalCallback callback) {
        try {
            callback.onJournalLoaded(journalDao.getJournalById(journalId));
        } catch (Exception e) {
            e.printStackTrace();
            callback.onDataNotAvailable();
        }
    }

    @Override
    public void saveJournal(@NonNull Journal journal, @NonNull JournalSaveCallback callback) {
        try {
            journalDao.insert(journal);
            Journal savedJournal = journalDao.getJournalById(journal.getJournalId());
            callback.onJournalSaved(savedJournal.getJournalId());
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError("Error occurred while saving the journal to DB");
        }
    }

    @Override
    public void refreshJournals() {

    }

    @Override
    public void deleteAllJournals(@NonNull String userId) {

    }

    @Override
    public void deleteJournal(@NonNull String journalId, @NonNull DeleteJournalCallback callback) {
        try {
            journalDao.delete(journalId);
            callback.onJournalDeleted();
        } catch (Exception e) {
            callback.onError();
        }
    }


    public void destroyInstance() {
        INSTANCE = null;
    }


}
