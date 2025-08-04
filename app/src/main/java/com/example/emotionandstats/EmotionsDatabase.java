package com.example.emotionandstats;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Emotions.class}, version = 1)
public abstract class EmotionsDatabase extends RoomDatabase {

    private static EmotionsDatabase INSTANCE;

    public abstract EmotionsDao emotionsDao();

    public static synchronized EmotionsDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    EmotionsDatabase.class,
                    "emotions_db"
            ).fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }
}
