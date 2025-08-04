package com.example.emotionandstats;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "emotions")
public class Emotions {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String mainMood;
    public String subEmotion;
    public long timestamp;
}
