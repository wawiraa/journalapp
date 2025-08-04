package com.example.emotionandstats;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EmotionsDao {

    @Insert
    void insert(Emotions emotions);

    @Query("SELECT * FROM emotions ORDER BY timestamp DESC")
    LiveData<List<Emotions>> getAllEmotions();

    @Query("SELECT * FROM emotions WHERE timestamp >= :fromTime ORDER BY timestamp ASC")
    LiveData<List<Emotions>> getEmotionsSince(long fromTime);

    @Query("SELECT * FROM emotions ORDER BY timestamp ASC")
    LiveData<List<Emotions>> getAllSortedByTimestamp();

    @Query("SELECT * FROM emotions ORDER BY timestamp ASC")
    LiveData<List<Emotions>> getAll();

    @Query("SELECT * FROM emotions WHERE timestamp >= :startOfDay AND timestamp < :startOfNextDay LIMIT 1")
    Emotions getMoodEntryForDay(long startOfDay, long startOfNextDay);

}
