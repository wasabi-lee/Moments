package com.wasabilee.moments.Data.DatabaseCompat;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.wasabilee.moments.Data.Models.Journal;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface JournalDao {

    @Query("SELECT * FROM journal ORDER BY user_designated_timestamp")
    List<Journal> getAllJournal();

    @Query("SELECT * FROM journal WHERE journalId IS (:journalId)")
    Journal getJournalById(String journalId);

    @Query("SELECT COUNT(*) from journal")
    Integer countJournals();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Journal journal);

    @Update
    void update(Journal journal);

    @Query("DELETE FROM journal WHERE journalId = (:journalId)")
    void delete(String journalId);

}
