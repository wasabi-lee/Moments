package com.wasabilee.moments.Data;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class JournalRemoteDataSource implements JournalDataSource {

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

    }

    @Override
    public void getJournal(@NonNull String journalId, @NonNull GetJournalCallback callback) {

    }

    @Override
    public void saveJournal(@NonNull Journal journal, @NonNull final UploadJournalCallback callback) {
        mFirebaseFirestore.collection("Journal").add(journal).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onJournalUploaded();
            } else {
                task.getException().printStackTrace();
                callback.onError(task.getException().getMessage());
            }
        });
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
