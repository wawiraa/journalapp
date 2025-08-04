package com.example.emotionandstats;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface JournalDao {

    @Query("SELECT * FROM journalEntry")
    LiveData<List<JournalEntry>> getAll();

    @Query("SELECT * FROM journalEntry WHERE id = :id LIMIT 1")
    LiveData<JournalEntry> getById(int id);

    @Insert
    long add(JournalEntry journalEntry);

    @Update
    void update(JournalEntry journalEntry);

    @Delete
    void delete(JournalEntry journalEntry);
}
