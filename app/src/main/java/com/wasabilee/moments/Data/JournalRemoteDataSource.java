package com.wasabilee.moments.Data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.wasabilee.moments.Data.Models.Journal;
import com.wasabilee.moments.Data.Models.JournalData;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class JournalRemoteDataSource implements JournalDataSource {

    private static final String TAG = JournalRemoteDataSource.class.getSimpleName();

    private static FirebaseFirestore mFirebaseFirestore;

    private static JournalRemoteDataSource INSTANCE;

    public static JournalRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JournalRemoteDataSource();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private JournalRemoteDataSource() {
        mFirebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void getJournals(@NonNull String userId, @NonNull LoadJournalsCallback callback) {

        mFirebaseFirestore.collection("Journal")
                .orderBy("user_designated_timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("user_id", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Journal> result = getResultsInList(task);
                        callback.onJournalsLoaded(result);
                        for (Journal journal :
                                result) {
                            Log.d(TAG, "getJournals: " + DateFormat.getInstance().format(journal.getTimestamp()));
                        }
                    } else {
                        callback.onDataNotAvailable(task.getException().getMessage());
                    }
                });
    }

    private List<Journal> getResultsInList(Task<QuerySnapshot> task) {
        List<Journal> resultList = new ArrayList<>();
        for (QueryDocumentSnapshot document : task.getResult()) {
            String docId = document.getId();
            Journal journal = document.toObject(Journal.class);
            journal.setJournalId(docId);
            resultList.add(journal);
        }
        return resultList;
    }

    @Override
    public void getJournal(@NonNull String journalId, @NonNull GetJournalCallback callback) {

        mFirebaseFirestore.collection("Journal")
                .document(journalId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            callback.onJournalLoaded(document.toObject(Journal.class));
                        } else {
                            callback.onDataNotAvailable();
                        }
                    } else {
                        Log.d(TAG, "onComplete: " + task.getResult());
                        callback.onDataNotAvailable();
                    }
                });
    }

    @Override
    public void saveJournal(@NonNull Journal journal, @NonNull final UploadJournalCallback callback) {
        if (journal.getJournalId() == null) {

            // Adding new journal
            mFirebaseFirestore.collection("Journal")
                    .add(journal)
                    .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onJournalUploaded(task.getResult().getId());
                } else {
                    task.getException().printStackTrace();
                    callback.onError(task.getException().getMessage());
                }
            });

        } else {

            // Updating existing journal
            mFirebaseFirestore.collection("Journal")
                    .document(journal.getJournalId())
                    .set(journal)
                    .addOnSuccessListener(aVoid -> callback.onJournalUploaded(journal.getJournalId()))
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        callback.onError(e.getMessage());
                    });

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
        mFirebaseFirestore.collection("Journal")
                .document(journalId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onJournalDeleted())
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onError();
                });
    }

}
